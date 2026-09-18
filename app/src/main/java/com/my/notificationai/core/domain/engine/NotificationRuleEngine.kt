package com.my.notificationai.core.domain.engine

import android.app.Notification
import android.service.notification.StatusBarNotification
import com.my.notificationai.core.database.dao.RuleDao
import com.my.notificationai.core.datastore.SettingsProvider
import com.my.notificationai.core.domain.models.RuleEvaluationResult
import kotlinx.coroutines.flow.first

class NotificationRuleEngine(
    private val ruleDao: RuleDao,
    private val settingsDataStore: SettingsProvider,
    private val otpDetector: OtpDetector,
    private val financialDetector: FinancialDetector,
    private val promotionalFilter: PromotionalFilter,
    private val scheduleEngine: ScheduleEngine
) {

    suspend fun evaluate(sbn: StatusBarNotification): RuleEvaluationResult {
        val packageName = sbn.packageName
        val notification = sbn.notification
        val extras = notification?.extras
        val title = extras?.getCharSequence("android.title")?.toString() ?: ""
        val text = extras?.getCharSequence("android.text")?.toString() ?: ""
        val channelId = notification?.channelId ?: ""
        val category = notification?.category
        val flags = notification?.flags ?: 0
        return evaluate(
            packageName = packageName,
            title = title,
            text = text,
            channelId = channelId,
            category = category,
            flags = flags
        )
    }

    suspend fun evaluate(
        packageName: String,
        title: String,
        text: String,
        channelId: String = "",
        category: String? = null,
        flags: Int = 0,
        currentTimeMillis: Long = System.currentTimeMillis()
    ): RuleEvaluationResult {
        val combinedText = "$title $text"

        val isEmergencyBypassEnabled = settingsDataStore.isEmergencyBypassEnabled.first()
        val isOtpProtectionEnabled = settingsDataStore.isOtpProtectionEnabled.first()
        val isFinancialProtectionEnabled = settingsDataStore.isFinancialProtectionEnabled.first()
        val isPromotionalSmsFilterEnabled = settingsDataStore.isPromotionalSmsFilterEnabled.first()
        val isMasterBlockerEnabled = settingsDataStore.isMasterBlockerEnabled.first()
        val activeBlockingMode = settingsDataStore.activeBlockingMode.first()
        val quickPauseUntil = settingsDataStore.quickPauseUntil.first()

        // 1. Emergency & Phone Call Bypass
        val isCall = packageName.contains("dialer") || packageName.contains("telephony") || packageName.contains("phone")
        val isAlarm = packageName.contains("clock") || packageName.contains("alarm")
        val isCallCategory = category == "call" || 
                             category == "alarm" ||
                             category == "missed_call" ||
                             category == Notification.CATEGORY_CALL || 
                             category == Notification.CATEGORY_ALARM ||
                             category == Notification.CATEGORY_MISSED_CALL

        if (isEmergencyBypassEnabled && (isCall || isAlarm || isCallCategory)) {
            return RuleEvaluationResult(
                shouldBlock = false,
                reason = "Emergency / Call Bypass"
            )
        }

        // 2. OTP & Security Protection
        val (isOtp, otpCode) = otpDetector.detect(title, text)
        if (isOtp) {
            if (isOtpProtectionEnabled) {
                return RuleEvaluationResult(
                    shouldBlock = false,
                    reason = "Protected OTP",
                    isOtp = true,
                    otpCode = otpCode
                )
            }
        }

        // 3. Financial Protection
        val isFinancial = financialDetector.isFinancial(packageName, title, text)
        if (isFinancial) {
            if (isFinancialProtectionEnabled) {
                return RuleEvaluationResult(
                    shouldBlock = false,
                    reason = "Protected Financial",
                    isFinancial = true,
                    isOtp = isOtp,
                    otpCode = otpCode
                )
            }
        }

        // 4. App Whitelist
        val isWhitelisted = ruleDao.isAppWhitelisted(packageName)
        if (isWhitelisted) {
            return RuleEvaluationResult(
                shouldBlock = false,
                reason = "Whitelisted App",
                isOtp = isOtp,
                otpCode = otpCode,
                isFinancial = isFinancial
            )
        }

        // 5. Keyword Whitelist
        val allRules = ruleDao.getAllRulesSync()
        val masterRules = allRules.filter { it.ruleType != "CUSTOM" }.associateBy { it.ruleType }
        val customRules = allRules.filter { it.ruleType == "CUSTOM" && it.isEnabled }.sortedByDescending { it.priority }

        val keywordsRule = masterRules["ALLOW_IMPORTANT_KEYWORDS"]
        val isKeywordsRuleActive = (keywordsRule?.isEnabled == true) || (keywordsRule == null)
        if (isKeywordsRuleActive) {
            val whitelistedKeywords = ruleDao.getAllWhitelistedKeywordsSync()
            for (kw in whitelistedKeywords) {
                if (combinedText.contains(kw.keyword, ignoreCase = true)) {
                    return RuleEvaluationResult(
                        shouldBlock = false,
                        reason = "Whitelisted Keyword: ${kw.keyword}",
                        ruleId = keywordsRule?.id,
                        ruleName = keywordsRule?.name ?: "Allow Important Keywords",
                        isOtp = isOtp,
                        otpCode = otpCode,
                        isFinancial = isFinancial
                    )
                }
            }
        }

        // 6. Quick Pause Check
        val now = currentTimeMillis
        if (now < quickPauseUntil) {
            return RuleEvaluationResult(
                shouldBlock = false,
                reason = "Quick Pause Active",
                isOtp = isOtp,
                otpCode = otpCode,
                isFinancial = isFinancial
            )
        }

        // 7. Check if Master Blocker is OFF
        if (!isMasterBlockerEnabled) {
            return RuleEvaluationResult(
                shouldBlock = false,
                reason = "Blocker Disabled",
                isOtp = isOtp,
                otpCode = otpCode,
                isFinancial = isFinancial
            )
        }

        // 8. Promotional SMS Filter
        val isSmsApp = packageName.contains("messaging") || packageName.contains("sms") || packageName.contains("mms")
        val isPromotional = isSmsApp && promotionalFilter.isPromotional(title, text)
        if (isPromotional && isPromotionalSmsFilterEnabled) {
            return RuleEvaluationResult(
                shouldBlock = true,
                reason = "Promotional SMS Filter",
                isPromotional = true,
                isOtp = isOtp,
                otpCode = otpCode,
                isFinancial = isFinancial
            )
        }

        // 9. Schedule Evaluation
        val scheduleRule = masterRules["SCHEDULE_BLOCKING"]
        val isScheduleRuleActive = (scheduleRule?.isEnabled == true) || activeBlockingMode == "SCHEDULE_BLOCKING"
        if (isScheduleRuleActive) {
            val schedules = ruleDao.getAllSchedulesSync()
            for (schedule in schedules) {
                if (scheduleEngine.isScheduleActive(schedule, now)) {
                    if (schedule.action == "BLOCK_ALL") {
                        return RuleEvaluationResult(
                            shouldBlock = true,
                            reason = "Schedule: ${schedule.title}",
                            ruleId = scheduleRule?.id ?: schedule.id,
                            ruleName = "Schedule: ${schedule.title}",
                            isOtp = isOtp,
                            otpCode = otpCode,
                            isFinancial = isFinancial
                        )
                    } else if (schedule.action == "BLOCK_SOCIAL") {
                        val socialCategory = ruleDao.getCategory("social")
                        val socialPackages = socialCategory?.packageNames?.split(",")?.map { it.trim() } ?: listOf(
                            "com.instagram.android", "com.facebook.katana", "com.facebook.orca",
                            "com.zhiliaoapp.musically", "com.twitter.android", "com.snapchat.android",
                            "com.google.android.youtube", "org.telegram.messenger"
                        )
                        if (socialPackages.contains(packageName)) {
                            return RuleEvaluationResult(
                                shouldBlock = true,
                                reason = "Schedule (Social): ${schedule.title}",
                                ruleId = scheduleRule?.id ?: schedule.id,
                                ruleName = "Schedule: ${schedule.title}",
                                isOtp = isOtp,
                                otpCode = otpCode,
                                isFinancial = isFinancial
                            )
                        }
                    }
                }
            }
        }

        // 10. Master Blocker Mode Evaluation
        // Check if "Block Everything" rule is enabled
        val blockEverythingRule = masterRules["BLOCK_EVERYTHING"]
        if (blockEverythingRule?.isEnabled == true || activeBlockingMode == "BLOCK_ALL") {
            return RuleEvaluationResult(
                shouldBlock = true,
                reason = "Block Everything Mode",
                ruleId = blockEverythingRule?.id,
                ruleName = "Block Everything",
                isOtp = isOtp,
                otpCode = otpCode,
                isFinancial = isFinancial
            )
        }

        // Check Social Media Mode
        val socialRule = masterRules["SOCIAL_MEDIA"]
        val isSocialRuleActive = (socialRule?.isEnabled == true) || activeBlockingMode == "SOCIAL_MEDIA"
        if (isSocialRuleActive) {
            val socialCategory = ruleDao.getCategory("social")
            val socialPackages = socialCategory?.packageNames?.split(",")?.map { it.trim() } ?: listOf(
                "com.instagram.android", "com.facebook.katana", "com.facebook.orca",
                "com.zhiliaoapp.musically", "com.twitter.android", "com.snapchat.android",
                "com.google.android.youtube", "org.telegram.messenger"
            )
            if (socialPackages.contains(packageName)) {
                return RuleEvaluationResult(
                    shouldBlock = true,
                    reason = "Block Social Media",
                    ruleId = socialRule?.id,
                    ruleName = "Block Social Media",
                    isOtp = isOtp,
                    otpCode = otpCode,
                    isFinancial = isFinancial
                )
            }
        }

        // Check Selected Apps Mode
        val selectedAppsRule = masterRules["SELECTED_APPS"]
        val isSelectedAppsActive = (selectedAppsRule?.isEnabled == true) || activeBlockingMode == "SELECTED_APPS"
        if (isSelectedAppsActive) {
            val isAppBlocked = ruleDao.isAppBlocked(packageName)
            if (isAppBlocked) {
                return RuleEvaluationResult(
                    shouldBlock = true,
                    reason = "Block Selected Apps",
                    ruleId = selectedAppsRule?.id,
                    ruleName = "Block Selected Apps",
                    isOtp = isOtp,
                    otpCode = otpCode,
                    isFinancial = isFinancial
                )
            }
        }

        // Check Custom Rules (all enabled custom rules evaluated in descending priority order)
        for (customRule in customRules) {
            val conditions = ruleDao.getConditionsForRuleSync(customRule.id)
            if (conditions.isNotEmpty()) {
                var allMatch = true
                for (condition in conditions) {
                    val matches = when (condition.conditionType) {
                        "APP" -> packageName.equals(condition.value, ignoreCase = true)
                        "TITLE" -> when (condition.operator) {
                            "EQUALS" -> title.equals(condition.value, ignoreCase = true)
                            "CONTAINS" -> title.contains(condition.value, ignoreCase = true)
                            "NOT_CONTAINS" -> !title.contains(condition.value, ignoreCase = true)
                            "STARTS_WITH" -> title.startsWith(condition.value, ignoreCase = true)
                            "ENDS_WITH" -> title.endsWith(condition.value, ignoreCase = true)
                            else -> false
                        }
                        "TEXT" -> when (condition.operator) {
                            "EQUALS" -> text.equals(condition.value, ignoreCase = true)
                            "CONTAINS" -> text.contains(condition.value, ignoreCase = true)
                            "NOT_CONTAINS" -> !text.contains(condition.value, ignoreCase = true)
                            "STARTS_WITH" -> text.startsWith(condition.value, ignoreCase = true)
                            "ENDS_WITH" -> text.endsWith(condition.value, ignoreCase = true)
                            else -> false
                        }
                        "KEYWORD" -> when (condition.operator) {
                            "EQUALS" -> combinedText.equals(condition.value, ignoreCase = true)
                            "CONTAINS" -> combinedText.contains(condition.value, ignoreCase = true)
                            "NOT_CONTAINS" -> !combinedText.contains(condition.value, ignoreCase = true)
                            else -> false
                        }
                        "CHANNEL" -> channelId.equals(condition.value, ignoreCase = true)
                        else -> true
                    }
                    if (!matches) {
                        allMatch = false
                        break
                    }
                }
                if (allMatch) {
                    val shouldBlock = customRule.action == "BLOCK"
                    return RuleEvaluationResult(
                        shouldBlock = shouldBlock,
                        reason = "Custom Rule: ${customRule.name}",
                        ruleId = customRule.id,
                        ruleName = customRule.name,
                        isOtp = isOtp,
                        otpCode = otpCode,
                        isFinancial = isFinancial
                    )
                }
            }
        }

        // 11. Default Policy: Allow
        return RuleEvaluationResult(
            shouldBlock = false,
            reason = "Default Policy",
            isOtp = isOtp,
            otpCode = otpCode,
            isFinancial = isFinancial
        )
    }
}
