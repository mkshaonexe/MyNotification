package com.my.notificationai

import com.my.notificationai.core.database.entities.BlockedApp
import com.my.notificationai.core.database.entities.BlockingRule
import com.my.notificationai.core.database.entities.WhitelistedApp
import com.my.notificationai.core.database.entities.WhitelistedKeyword
import com.my.notificationai.core.datastore.SettingsProvider
import com.my.notificationai.core.domain.engine.FinancialDetector
import com.my.notificationai.core.domain.engine.NotificationRuleEngine
import com.my.notificationai.core.domain.engine.OtpDetector
import com.my.notificationai.core.domain.engine.PromotionalFilter
import com.my.notificationai.core.domain.engine.ScheduleEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FakeSettingsProvider : SettingsProvider {
    val masterBlocker = MutableStateFlow(true)
    val blockingMode = MutableStateFlow("SOCIAL_MEDIA")
    val quickPause = MutableStateFlow(0L)
    val theme = MutableStateFlow("DARK")
    val otpProtection = MutableStateFlow(true)
    val financialProtection = MutableStateFlow(true)
    val promoFilter = MutableStateFlow(true)
    val emergencyBypass = MutableStateFlow(true)
    val retention = MutableStateFlow(30)
    val onboarding = MutableStateFlow(true)

    override val isMasterBlockerEnabled: Flow<Boolean> get() = masterBlocker
    override val isBlockAllEnabled: Flow<Boolean> get() = masterBlocker
    override val activeBlockingMode: Flow<String> get() = blockingMode
    override val quickPauseUntil: Flow<Long> get() = quickPause
    override val themePreference: Flow<String> get() = theme
    override val isOtpProtectionEnabled: Flow<Boolean> get() = otpProtection
    override val isFinancialProtectionEnabled: Flow<Boolean> get() = financialProtection
    override val isPromotionalSmsFilterEnabled: Flow<Boolean> get() = promoFilter
    override val isEmergencyBypassEnabled: Flow<Boolean> get() = emergencyBypass
    override val dataRetentionDays: Flow<Int> get() = retention
    override val isOnboardingCompleted: Flow<Boolean> get() = onboarding
}

class NotificationRuleEngineTest {

    private lateinit var settingsProvider: FakeSettingsProvider
    private lateinit var ruleDao: FakeRuleDao
    private lateinit var otpDetector: OtpDetector
    private lateinit var financialDetector: FinancialDetector
    private lateinit var promotionalFilter: PromotionalFilter
    private lateinit var scheduleEngine: ScheduleEngine
    private lateinit var ruleEngine: NotificationRuleEngine

    @Before
    fun setup() {
        settingsProvider = FakeSettingsProvider()
        ruleDao = FakeRuleDao()
        otpDetector = OtpDetector()
        financialDetector = FinancialDetector()
        promotionalFilter = PromotionalFilter()
        scheduleEngine = ScheduleEngine()
        ruleEngine = NotificationRuleEngine(
            ruleDao = ruleDao,
            settingsDataStore = settingsProvider,
            otpDetector = otpDetector,
            financialDetector = financialDetector,
            promotionalFilter = promotionalFilter,
            scheduleEngine = scheduleEngine
        )
    }

    @Test
    fun `emergency call bypass takes highest precedence and allows notification`() = runBlocking {
        settingsProvider.masterBlocker.value = true
        settingsProvider.emergencyBypass.value = true

        val result = ruleEngine.evaluate(
            packageName = "com.google.android.dialer",
            title = "Incoming Call",
            text = "Mom is calling...",
            category = "call"
        )

        assertFalse(result.shouldBlock)
        assertEquals("Emergency / Call Bypass", result.reason)
    }

    @Test
    fun `protected OTP is allowed even in block everything mode`() = runBlocking {
        settingsProvider.masterBlocker.value = true
        settingsProvider.blockingMode.value = "BLOCK_ALL"
        settingsProvider.otpProtection.value = true

        val result = ruleEngine.evaluate(
            packageName = "com.google.android.apps.messaging",
            title = "Verification Code",
            text = "Your verification code is 584920. Do not share it."
        )

        assertFalse(result.shouldBlock)
        assertTrue(result.isOtp)
        assertEquals("584920", result.otpCode)
        assertEquals("Protected OTP", result.reason)
    }

    @Test
    fun `financial notification is allowed when financial protection is enabled`() = runBlocking {
        settingsProvider.masterBlocker.value = true
        settingsProvider.blockingMode.value = "BLOCK_ALL"
        settingsProvider.financialProtection.value = true

        val result = ruleEngine.evaluate(
            packageName = "com.bKash.customerapp",
            title = "Cash In",
            text = "You have received Tk 2,000. Balance Tk 4,500."
        )

        assertFalse(result.shouldBlock)
        assertTrue(result.isFinancial)
        assertEquals("Protected Financial", result.reason)
    }

    @Test
    fun `whitelisted app bypasses master blocker`() = runBlocking {
        settingsProvider.masterBlocker.value = true
        settingsProvider.blockingMode.value = "BLOCK_ALL"
        ruleDao.insertWhitelistedApp(WhitelistedApp("com.slack", "Slack", "Work", 1L))

        val result = ruleEngine.evaluate(
            packageName = "com.slack",
            title = "Team Channel",
            text = "Deployment completed successfully."
        )

        assertFalse(result.shouldBlock)
        assertEquals("Whitelisted App", result.reason)
    }

    @Test
    fun `whitelisted keyword bypasses master blocker`() = runBlocking {
        settingsProvider.masterBlocker.value = true
        settingsProvider.blockingMode.value = "BLOCK_ALL"
        ruleDao.whitelistedKeywords.add(WhitelistedKeyword("urgent", "Priority", 1L))

        val result = ruleEngine.evaluate(
            packageName = "com.random.app",
            title = "Server Alert",
            text = "This is an urgent server outage notice."
        )

        assertFalse(result.shouldBlock)
        assertEquals("Whitelisted Keyword: urgent", result.reason)
    }

    @Test
    fun `quick pause active allows all notifications temporarily`() = runBlocking {
        val now = 1000000L
        settingsProvider.quickPause.value = now + 60000L // Active for next 60s
        settingsProvider.masterBlocker.value = true
        settingsProvider.blockingMode.value = "BLOCK_ALL"

        val result = ruleEngine.evaluate(
            packageName = "com.instagram.android",
            title = "Instagram",
            text = "Someone liked your reel",
            currentTimeMillis = now
        )

        assertFalse(result.shouldBlock)
        assertEquals("Quick Pause Active", result.reason)
    }

    @Test
    fun `master blocker disabled allows all notifications`() = runBlocking {
        settingsProvider.masterBlocker.value = false

        val result = ruleEngine.evaluate(
            packageName = "com.instagram.android",
            title = "Instagram",
            text = "New follower"
        )

        assertFalse(result.shouldBlock)
        assertEquals("Blocker Disabled", result.reason)
    }

    @Test
    fun `promotional SMS spam is blocked when promo filter is enabled`() = runBlocking {
        settingsProvider.masterBlocker.value = true
        settingsProvider.promoFilter.value = true

        val result = ruleEngine.evaluate(
            packageName = "com.google.android.apps.messaging",
            title = "GP Internet",
            text = "Get 10 GB for 7 days recharge offer dial *121*123#"
        )

        assertTrue(result.shouldBlock)
        assertTrue(result.isPromotional)
        assertEquals("Promotional SMS Filter", result.reason)
    }

    @Test
    fun `social media mode blocks social apps and allows productivity apps`() = runBlocking {
        settingsProvider.masterBlocker.value = true
        settingsProvider.blockingMode.value = "SOCIAL_MEDIA"
        ruleDao.rules.add(
            BlockingRule(
                id = 1,
                name = "Block Social Media",
                description = "Social apps",
                ruleType = "SOCIAL_MEDIA",
                action = "BLOCK",
                isEnabled = true,
                priority = 50,
                createdAt = 1L,
                updatedAt = 1L
            )
        )

        val socialResult = ruleEngine.evaluate(
            packageName = "com.instagram.android",
            title = "Instagram",
            text = "New story update"
        )
        assertTrue(socialResult.shouldBlock)
        assertEquals("Block Social Media", socialResult.reason)

        val workResult = ruleEngine.evaluate(
            packageName = "com.google.android.gm",
            title = "Gmail",
            text = "Meeting summary"
        )
        assertFalse(workResult.shouldBlock)
        assertEquals("Default Policy", workResult.reason)
    }

    @Test
    fun `selected apps mode blocks only selected apps`() = runBlocking {
        settingsProvider.masterBlocker.value = true
        settingsProvider.blockingMode.value = "SELECTED_APPS"
        ruleDao.rules.add(
            BlockingRule(
                id = 2,
                name = "Block Selected Apps",
                description = "Selected apps",
                ruleType = "SELECTED_APPS",
                action = "BLOCK",
                isEnabled = true,
                priority = 40,
                createdAt = 1L,
                updatedAt = 1L
            )
        )
        ruleDao.insertBlockedApp(BlockedApp("com.shopping.app", "Shopping", true, "High", "Shopping", 1L, 1L))

        val blockedResult = ruleEngine.evaluate(
            packageName = "com.shopping.app",
            title = "Sale",
            text = "Items in cart"
        )
        assertTrue(blockedResult.shouldBlock)
        assertEquals("Block Selected Apps", blockedResult.reason)

        val otherResult = ruleEngine.evaluate(
            packageName = "com.news.reader",
            title = "Daily News",
            text = "Morning edition"
        )
        assertFalse(otherResult.shouldBlock)
        assertEquals("Default Policy", otherResult.reason)
    }
}
