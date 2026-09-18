package com.my.notificationai

import com.my.notificationai.core.database.entities.BlockedApp
import com.my.notificationai.core.database.entities.BlockingRule
import com.my.notificationai.core.database.entities.NotificationCategory
import com.my.notificationai.core.database.entities.RuleCondition
import com.my.notificationai.core.database.entities.Schedule
import com.my.notificationai.core.database.entities.WhitelistedApp
import com.my.notificationai.core.database.entities.WhitelistedKeyword
import com.my.notificationai.core.domain.engine.FinancialDetector
import com.my.notificationai.core.domain.engine.NotificationRuleEngine
import com.my.notificationai.core.domain.engine.OtpDetector
import com.my.notificationai.core.domain.engine.PromotionalFilter
import com.my.notificationai.core.domain.engine.ScheduleEngine
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Calendar

class NotificationRuleEngineTest {

    private lateinit var ruleDao: FakeRuleDao
    private lateinit var settingsProvider: FakeSettingsProvider
    private lateinit var otpDetector: OtpDetector
    private lateinit var financialDetector: FinancialDetector
    private lateinit var promotionalFilter: PromotionalFilter
    private lateinit var scheduleEngine: ScheduleEngine
    private lateinit var ruleEngine: NotificationRuleEngine

    @Before
    fun setup() {
        ruleDao = FakeRuleDao()
        settingsProvider = FakeSettingsProvider()
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

        // Seed basic categories
        runBlocking {
            ruleDao.insertCategory(
                NotificationCategory(
                    categoryId = "social",
                    name = "Social Media",
                    packageNames = "com.instagram.android,com.facebook.katana,com.zhiliaoapp.musically",
                    isEditable = true
                )
            )
            ruleDao.insertRules(
                listOf(
                    BlockingRule(id = 1, name = "Block Social Media", description = "", ruleType = "SOCIAL_MEDIA", action = "BLOCK", isEnabled = true, priority = 50, createdAt = 0, updatedAt = 0),
                    BlockingRule(id = 2, name = "Block Selected Apps", description = "", ruleType = "SELECTED_APPS", action = "BLOCK", isEnabled = false, priority = 40, createdAt = 0, updatedAt = 0),
                    BlockingRule(id = 3, name = "Allow Important Keywords", description = "", ruleType = "ALLOW_IMPORTANT_KEYWORDS", action = "ALLOW", isEnabled = true, priority = 90, createdAt = 0, updatedAt = 0),
                    BlockingRule(id = 4, name = "Schedule Blocking", description = "", ruleType = "SCHEDULE_BLOCKING", action = "BLOCK", isEnabled = false, priority = 60, createdAt = 0, updatedAt = 0),
                    BlockingRule(id = 5, name = "Block Everything", description = "", ruleType = "BLOCK_EVERYTHING", action = "BLOCK", isEnabled = false, priority = 10, createdAt = 0, updatedAt = 0)
                )
            )
        }
    }

    @Test
    fun `emergency and phone calls bypass all blockers`() = runBlocking {
        // Block everything mode active
        settingsProvider.blockingMode.value = "BLOCK_ALL"

        val result = ruleEngine.evaluate(
            packageName = "com.android.dialer",
            title = "Incoming Call",
            text = "Mom is calling...",
            category = "call"
        )

        assertFalse(result.shouldBlock)
        assertEquals("Emergency / Call Bypass", result.reason)
    }

    @Test
    fun `OTP verification code bypasses blocking`() = runBlocking {
        settingsProvider.blockingMode.value = "BLOCK_ALL"

        val result = ruleEngine.evaluate(
            packageName = "com.instagram.android",
            title = "Security",
            text = "Your verification code is 849201"
        )

        assertFalse(result.shouldBlock)
        assertTrue(result.isOtp)
        assertEquals("849201", result.otpCode)
        assertEquals("Protected OTP", result.reason)
    }

    @Test
    fun `trusted financial notification bypasses blocking`() = runBlocking {
        settingsProvider.blockingMode.value = "BLOCK_ALL"

        val result = ruleEngine.evaluate(
            packageName = "com.bKash.customerapp",
            title = "Statement",
            text = "Your account has received Tk. 1,000"
        )

        assertFalse(result.shouldBlock)
        assertTrue(result.isFinancial)
        assertEquals("Protected Financial", result.reason)
    }

    @Test
    fun `whitelisted app bypasses blocking rules`() = runBlocking {
        ruleDao.insertWhitelistedApp(
            WhitelistedApp(packageName = "com.instagram.android", appLabel = "Instagram", addedAt = 0)
        )

        val result = ruleEngine.evaluate(
            packageName = "com.instagram.android",
            title = "Instagram",
            text = "New story update"
        )

        assertFalse(result.shouldBlock)
        assertEquals("Whitelisted App", result.reason)
    }

    @Test
    fun `whitelisted keyword bypasses blocking when rule is enabled`() = runBlocking {
        ruleDao.insertWhitelistedKeyword(
            WhitelistedKeyword(keyword = "urgent", category = "Custom", addedAt = 0)
        )

        val result = ruleEngine.evaluate(
            packageName = "com.instagram.android",
            title = "Direct Message",
            text = "Hey, this is urgent, please call back"
        )

        assertFalse(result.shouldBlock)
        assertTrue(result.reason.contains("Whitelisted Keyword"))
    }

    @Test
    fun `whitelisted keyword is ignored when Allow Important Keywords is disabled`() = runBlocking {
        ruleDao.insertWhitelistedKeyword(
            WhitelistedKeyword(keyword = "urgent", category = "Custom", addedAt = 0)
        )
        // Disable keyword whitelist master rule
        ruleDao.setRuleEnabled(3, false)

        val result = ruleEngine.evaluate(
            packageName = "com.instagram.android",
            title = "Direct Message",
            text = "Hey, this is urgent, please call back"
        )

        // Should be blocked by Social Media rule
        assertTrue(result.shouldBlock)
        assertEquals("Block Social Media", result.reason)
    }

    @Test
    fun `quick pause prevents blocking`() = runBlocking {
        val now = System.currentTimeMillis()
        settingsProvider.quickPause.value = now + 60000L

        val result = ruleEngine.evaluate(
            packageName = "com.instagram.android",
            title = "Instagram",
            text = "New post",
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
            text = "New post"
        )

        assertFalse(result.shouldBlock)
        assertEquals("Blocker Disabled", result.reason)
    }

    @Test
    fun `promotional SMS spam is blocked`() = runBlocking {
        val result = ruleEngine.evaluate(
            packageName = "com.google.android.apps.messaging",
            title = "Promo",
            text = "Recharge 50 tk and get 5 GB for 3 days dial *121*50#"
        )

        assertTrue(result.shouldBlock)
        assertTrue(result.isPromotional)
        assertEquals("Promotional SMS Filter", result.reason)
    }

    @Test
    fun `schedule blocking blocks when active`() = runBlocking {
        // Enable schedule master rule
        ruleDao.setRuleEnabled(4, true)
        ruleDao.insertSchedule(
            Schedule(
                id = 10,
                title = "Sleep Mode",
                startHour = 22,
                startMinute = 0,
                endHour = 7,
                endMinute = 0,
                repeatDays = "ALL",
                action = "BLOCK_ALL",
                isEnabled = true,
                createdAt = 0
            )
        )

        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 0)
        }

        val result = ruleEngine.evaluate(
            packageName = "com.example.randomapp",
            title = "Random",
            text = "Hello world",
            currentTimeMillis = cal.timeInMillis
        )

        assertTrue(result.shouldBlock)
        assertEquals("Schedule: Sleep Mode", result.reason)
    }

    @Test
    fun `social media mode blocks social media package but allows normal app`() = runBlocking {
        val socialResult = ruleEngine.evaluate(
            packageName = "com.instagram.android",
            title = "Instagram",
            text = "Check out this reel"
        )
        assertTrue(socialResult.shouldBlock)
        assertEquals("Block Social Media", socialResult.reason)

        val normalResult = ruleEngine.evaluate(
            packageName = "com.work.notes",
            title = "Notes",
            text = "Meeting at 3 PM"
        )
        assertFalse(normalResult.shouldBlock)
        assertEquals("Default Policy", normalResult.reason)
    }

    @Test
    fun `selected apps mode blocks selected apps`() = runBlocking {
        settingsProvider.blockingMode.value = "SELECTED_APPS"
        ruleDao.setRuleEnabled(1, false) // Disable social media
        ruleDao.setRuleEnabled(2, true)  // Enable selected apps
        ruleDao.insertBlockedApp(
            BlockedApp(packageName = "com.distracting.game", appLabel = "Game", isBlocked = true, createdAt = 0, updatedAt = 0)
        )

        val blockedResult = ruleEngine.evaluate(
            packageName = "com.distracting.game",
            title = "Game",
            text = "Your energy is full!"
        )
        assertTrue(blockedResult.shouldBlock)
        assertEquals("Block Selected Apps", blockedResult.reason)

        val unblockedResult = ruleEngine.evaluate(
            packageName = "com.productive.app",
            title = "Productive",
            text = "Task completed"
        )
        assertFalse(unblockedResult.shouldBlock)
        assertEquals("Default Policy", unblockedResult.reason)
    }

    @Test
    fun `multiple custom rules are evaluated in priority order`() = runBlocking {
        settingsProvider.blockingMode.value = "CUSTOM"
        ruleDao.setRuleEnabled(1, false) // Disable social media

        // Rule 1: Priority 20 -> Block if text contains "spam"
        val rule1Id = ruleDao.insertRule(
            BlockingRule(id = 20, name = "Low Priority Spam", description = "", ruleType = "CUSTOM", action = "BLOCK", isEnabled = true, priority = 20, createdAt = 0, updatedAt = 0)
        )
        ruleDao.insertCondition(
            RuleCondition(id = 101, ruleId = rule1Id, conditionType = "TEXT", operator = "CONTAINS", value = "spam")
        )

        // Rule 2: Priority 80 -> Allow if title equals "VIP Boss"
        val rule2Id = ruleDao.insertRule(
            BlockingRule(id = 21, name = "High Priority Boss", description = "", ruleType = "CUSTOM", action = "ALLOW", isEnabled = true, priority = 80, createdAt = 0, updatedAt = 0)
        )
        ruleDao.insertCondition(
            RuleCondition(id = 102, ruleId = rule2Id, conditionType = "TITLE", operator = "EQUALS", value = "VIP Boss")
        )

        // When notification has Title="VIP Boss" and Text="This is spam":
        // Higher priority rule (Priority 80) matches first and ALLOWS it!
        val result = ruleEngine.evaluate(
            packageName = "com.work.email",
            title = "VIP Boss",
            text = "This is spam"
        )

        assertFalse(result.shouldBlock)
        assertEquals("Custom Rule: High Priority Boss", result.reason)
    }
}
