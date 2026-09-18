package com.my.notificationai.data

import com.my.notificationai.core.database.dao.AppNotificationCount
import com.my.notificationai.core.database.dao.NotificationDao
import com.my.notificationai.core.database.dao.RuleDao
import com.my.notificationai.core.database.entities.BlockedApp
import com.my.notificationai.core.database.entities.BlockingRule
import com.my.notificationai.core.database.entities.NotificationCategory
import com.my.notificationai.core.database.entities.NotificationEvent
import com.my.notificationai.core.database.entities.NotificationUpdate
import com.my.notificationai.core.database.entities.RuleCondition
import com.my.notificationai.core.database.entities.Schedule
import com.my.notificationai.core.database.entities.WhitelistedApp
import com.my.notificationai.core.database.entities.WhitelistedKeyword
import com.my.notificationai.core.datastore.SettingsDataStore
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppRepository @Inject constructor(
    private val notificationDao: NotificationDao,
    private val ruleDao: RuleDao,
    private val settingsDataStore: SettingsDataStore
) {
    // --- Notification Events ---
    val allEvents: Flow<List<NotificationEvent>> = notificationDao.getAllEventsFlow()
    val totalEventsCount: Flow<Int> = notificationDao.getTotalEventsCount()
    val blockedEventsCount: Flow<Int> = notificationDao.getBlockedEventsCount()
    val allowedEventsCount: Flow<Int> = notificationDao.getAllowedEventsCount()
    val importantEventsCount: Flow<Int> = notificationDao.getImportantEventsCount()

    fun getFilteredEvents(query: String, filter: String): Flow<List<NotificationEvent>> {
        return notificationDao.getFilteredEventsFlow(query, filter)
    }

    suspend fun getEventById(id: Long): NotificationEvent? = notificationDao.getEventById(id)
    fun getEventByIdFlow(id: Long): Flow<NotificationEvent?> = notificationDao.getEventByIdFlow(id)

    suspend fun insertEvent(event: NotificationEvent): Long = notificationDao.insertEvent(event)
    suspend fun updateEvent(event: NotificationEvent) = notificationDao.updateEvent(event)
    suspend fun deleteEvent(event: NotificationEvent) = notificationDao.deleteEvent(event)
    suspend fun deleteEventById(id: Long) = notificationDao.deleteEventById(id)
    suspend fun deleteAllEvents() = notificationDao.deleteAllEvents()
    suspend fun markEventAsRead(id: Long) = notificationDao.markEventAsRead(id)
    suspend fun markAllEventsAsRead() = notificationDao.markAllEventsAsRead()

    fun getUpdatesForEvent(eventId: Long): Flow<List<NotificationUpdate>> = notificationDao.getUpdatesForEvent(eventId)

    // --- Analytics ---
    fun getEventsBetween(startTime: Long, endTime: Long): Flow<List<NotificationEvent>> =
        notificationDao.getEventsBetween(startTime, endTime)

    fun getEventsCountBetween(startTime: Long, endTime: Long): Flow<Int> =
        notificationDao.getEventsCountBetween(startTime, endTime)

    fun getBlockedEventsCountBetween(startTime: Long, endTime: Long): Flow<Int> =
        notificationDao.getBlockedEventsCountBetween(startTime, endTime)

    fun getAllowedEventsCountBetween(startTime: Long, endTime: Long): Flow<Int> =
        notificationDao.getAllowedEventsCountBetween(startTime, endTime)

    fun getImportantEventsCountBetween(startTime: Long, endTime: Long): Flow<Int> =
        notificationDao.getImportantEventsCountBetween(startTime, endTime)

    fun getTopAppsBetween(startTime: Long, endTime: Long, limit: Int = 10): Flow<List<AppNotificationCount>> =
        notificationDao.getTopAppsBetween(startTime, endTime, limit)

    // --- Blocking Rules ---
    val allRules: Flow<List<BlockingRule>> = ruleDao.getAllRulesFlow()
    suspend fun getAllRulesSync(): List<BlockingRule> = ruleDao.getAllRulesSync()
    suspend fun getRuleById(id: Long): BlockingRule? = ruleDao.getRuleById(id)
    suspend fun getRuleByType(type: String): BlockingRule? = ruleDao.getRuleByType(type)
    suspend fun insertRule(rule: BlockingRule): Long = ruleDao.insertRule(rule)
    suspend fun updateRule(rule: BlockingRule) = ruleDao.updateRule(rule)
    suspend fun deleteRule(rule: BlockingRule) = ruleDao.deleteRule(rule)
    suspend fun deleteRuleById(id: Long) = ruleDao.deleteRuleById(id)
    suspend fun setRuleEnabled(id: Long, isEnabled: Boolean) = ruleDao.setRuleEnabled(id, isEnabled)
    suspend fun setRuleEnabledByType(ruleType: String, isEnabled: Boolean) = ruleDao.setRuleEnabledByType(ruleType, isEnabled)

    // --- Rule Conditions ---
    fun getConditionsForRule(ruleId: Long): Flow<List<RuleCondition>> = ruleDao.getConditionsForRule(ruleId)
    suspend fun getConditionsForRuleSync(ruleId: Long): List<RuleCondition> = ruleDao.getConditionsForRuleSync(ruleId)
    suspend fun insertCondition(condition: RuleCondition): Long = ruleDao.insertCondition(condition)
    suspend fun deleteCondition(id: Long) = ruleDao.deleteCondition(id)
    suspend fun deleteConditionsForRule(ruleId: Long) = ruleDao.deleteConditionsForRule(ruleId)

    // --- Whitelisted Apps ---
    val allWhitelistedApps: Flow<List<WhitelistedApp>> = ruleDao.getAllWhitelistedApps()
    val whitelistedAppsCount: Flow<Int> = ruleDao.getWhitelistedAppsCount()
    suspend fun isAppWhitelisted(packageName: String): Boolean = ruleDao.isAppWhitelisted(packageName)
    suspend fun insertWhitelistedApp(app: WhitelistedApp) = ruleDao.insertWhitelistedApp(app)
    suspend fun deleteWhitelistedApp(packageName: String) = ruleDao.deleteWhitelistedApp(packageName)

    // --- Whitelisted Keywords ---
    val allWhitelistedKeywords: Flow<List<WhitelistedKeyword>> = ruleDao.getAllWhitelistedKeywords()
    val whitelistedKeywordsCount: Flow<Int> = ruleDao.getWhitelistedKeywordsCount()
    suspend fun insertWhitelistedKeyword(keyword: WhitelistedKeyword) = ruleDao.insertWhitelistedKeyword(keyword)
    suspend fun deleteWhitelistedKeyword(keyword: String) = ruleDao.deleteWhitelistedKeyword(keyword)

    // --- Blocked Apps ---
    val allBlockedApps: Flow<List<BlockedApp>> = ruleDao.getAllBlockedApps()
    val blockedAppsCount: Flow<Int> = ruleDao.getBlockedAppsCount()
    suspend fun getBlockedApp(packageName: String): BlockedApp? = ruleDao.getBlockedApp(packageName)
    suspend fun isAppBlocked(packageName: String): Boolean = ruleDao.isAppBlocked(packageName)
    suspend fun insertBlockedApp(app: BlockedApp) = ruleDao.insertBlockedApp(app)
    suspend fun updateBlockedApp(app: BlockedApp) = ruleDao.updateBlockedApp(app)
    suspend fun deleteBlockedApp(packageName: String) = ruleDao.deleteBlockedApp(packageName)
    suspend fun setAppBlocked(packageName: String, isBlocked: Boolean) = ruleDao.setAppBlocked(packageName, isBlocked)

    // --- Schedules ---
    val allSchedules: Flow<List<Schedule>> = ruleDao.getAllSchedules()
    val activeSchedulesCount: Flow<Int> = ruleDao.getActiveSchedulesCount()
    suspend fun getScheduleById(id: Long): Schedule? = ruleDao.getScheduleById(id)
    suspend fun insertSchedule(schedule: Schedule): Long = ruleDao.insertSchedule(schedule)
    suspend fun updateSchedule(schedule: Schedule) = ruleDao.updateSchedule(schedule)
    suspend fun deleteSchedule(id: Long) = ruleDao.deleteSchedule(id)
    suspend fun setScheduleEnabled(id: Long, isEnabled: Boolean) = ruleDao.setScheduleEnabled(id, isEnabled)

    // --- Categories ---
    val allCategories: Flow<List<NotificationCategory>> = ruleDao.getAllCategories()
    suspend fun getCategory(categoryId: String): NotificationCategory? = ruleDao.getCategory(categoryId)
    suspend fun insertCategory(category: NotificationCategory) = ruleDao.insertCategory(category)
    suspend fun updateCategory(category: NotificationCategory) = ruleDao.updateCategory(category)
    suspend fun deleteCategory(categoryId: String) = ruleDao.deleteCategory(categoryId)

    // --- Settings DataStore ---
    val isMasterBlockerEnabled: Flow<Boolean> = settingsDataStore.isMasterBlockerEnabled
    val isBlockAllEnabled: Flow<Boolean> = settingsDataStore.isBlockAllEnabled
    val activeBlockingMode: Flow<String> = settingsDataStore.activeBlockingMode
    val quickPauseUntil: Flow<Long> = settingsDataStore.quickPauseUntil
    val themePreference: Flow<String> = settingsDataStore.themePreference
    val isOtpProtectionEnabled: Flow<Boolean> = settingsDataStore.isOtpProtectionEnabled
    val isFinancialProtectionEnabled: Flow<Boolean> = settingsDataStore.isFinancialProtectionEnabled
    val isPromotionalSmsFilterEnabled: Flow<Boolean> = settingsDataStore.isPromotionalSmsFilterEnabled
    val isEmergencyBypassEnabled: Flow<Boolean> = settingsDataStore.isEmergencyBypassEnabled
    val dataRetentionDays: Flow<Int> = settingsDataStore.dataRetentionDays
    val isOnboardingCompleted: Flow<Boolean> = settingsDataStore.isOnboardingCompleted

    suspend fun setMasterBlockerEnabled(enabled: Boolean) = settingsDataStore.setMasterBlockerEnabled(enabled)
    suspend fun setBlockAllEnabled(enabled: Boolean) = settingsDataStore.setBlockAllEnabled(enabled)
    suspend fun setActiveBlockingMode(mode: String) = settingsDataStore.setActiveBlockingMode(mode)
    suspend fun setQuickPauseUntil(timestamp: Long) = settingsDataStore.setQuickPauseUntil(timestamp)
    suspend fun setThemePreference(theme: String) = settingsDataStore.setThemePreference(theme)
    suspend fun setOtpProtectionEnabled(enabled: Boolean) = settingsDataStore.setOtpProtectionEnabled(enabled)
    suspend fun setFinancialProtectionEnabled(enabled: Boolean) = settingsDataStore.setFinancialProtectionEnabled(enabled)
    suspend fun setPromotionalSmsFilterEnabled(enabled: Boolean) = settingsDataStore.setPromotionalSmsFilterEnabled(enabled)
    suspend fun setEmergencyBypassEnabled(enabled: Boolean) = settingsDataStore.setEmergencyBypassEnabled(enabled)
    suspend fun setDataRetentionDays(days: Int) = settingsDataStore.setDataRetentionDays(days)
    suspend fun setOnboardingCompleted(completed: Boolean) = settingsDataStore.setOnboardingCompleted(completed)
}
