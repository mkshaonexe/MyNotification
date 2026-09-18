package com.my.notificationai

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
import com.my.notificationai.core.datastore.SettingsProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class FakeSettingsProvider : SettingsProvider {
    val masterBlocker = MutableStateFlow(true)
    val blockingMode = MutableStateFlow("SOCIAL_MEDIA")
    val quickPause = MutableStateFlow(0L)
    val theme = MutableStateFlow("DARK")
    val otpProtection = MutableStateFlow(true)
    val financialProtection = MutableStateFlow(true)
    val promotionalSmsFilter = MutableStateFlow(true)
    val emergencyBypass = MutableStateFlow(true)
    val retentionDays = MutableStateFlow(90)
    val onboardingCompleted = MutableStateFlow(true)

    override val isMasterBlockerEnabled: Flow<Boolean> = masterBlocker.asStateFlow()
    override val isBlockAllEnabled: Flow<Boolean> = masterBlocker.asStateFlow()
    override val activeBlockingMode: Flow<String> = blockingMode.asStateFlow()
    override val quickPauseUntil: Flow<Long> = quickPause.asStateFlow()
    override val themePreference: Flow<String> = theme.asStateFlow()
    override val isOtpProtectionEnabled: Flow<Boolean> = otpProtection.asStateFlow()
    override val isFinancialProtectionEnabled: Flow<Boolean> = financialProtection.asStateFlow()
    override val isPromotionalSmsFilterEnabled: Flow<Boolean> = promotionalSmsFilter.asStateFlow()
    override val isEmergencyBypassEnabled: Flow<Boolean> = emergencyBypass.asStateFlow()
    override val dataRetentionDays: Flow<Int> = retentionDays.asStateFlow()
    override val isOnboardingCompleted: Flow<Boolean> = onboardingCompleted.asStateFlow()
}

class FakeNotificationDao : NotificationDao {
    private val eventsMap = LinkedHashMap<Long, NotificationEvent>()
    val events: List<NotificationEvent>
        get() = ArrayList(eventsMap.values)

    val updates = mutableListOf<NotificationUpdate>()
    private var nextId = 1L
    private var nextUpdateId = 1L

    private val _eventsFlow = MutableStateFlow<List<NotificationEvent>>(emptyList())
    private fun notifyChanged() {
        _eventsFlow.value = ArrayList(eventsMap.values)
    }

    override suspend fun insertEvent(event: NotificationEvent): Long {
        val id = if (event.id == 0L) nextId++ else event.id
        val saved = event.copy(id = id)
        eventsMap[id] = saved
        notifyChanged()
        return id
    }

    override suspend fun updateEvent(event: NotificationEvent) {
        eventsMap[event.id] = event
        notifyChanged()
    }

    override suspend fun deleteEvent(event: NotificationEvent) {
        eventsMap.remove(event.id)
        notifyChanged()
    }

    override suspend fun deleteEventById(id: Long) {
        eventsMap.remove(id)
        notifyChanged()
    }

    override suspend fun deleteAllEvents() {
        eventsMap.clear()
        updates.clear()
        notifyChanged()
    }

    override suspend fun getEventById(id: Long): NotificationEvent? = eventsMap[id]

    override fun getEventByIdFlow(id: Long): Flow<NotificationEvent?> =
        _eventsFlow.map { list -> list.find { it.id == id } }

    override suspend fun getEventByKey(key: String): NotificationEvent? =
        eventsMap.values.find { it.notificationKey == key }

    override suspend fun getActiveEventByKey(key: String): NotificationEvent? =
        eventsMap.values.filter { it.notificationKey == key && it.removedAt == null }.maxByOrNull { it.id }

    override fun getAllEventsFlow(): Flow<List<NotificationEvent>> = _eventsFlow

    override fun getFilteredEventsFlow(query: String, filter: String): Flow<List<NotificationEvent>> =
        _eventsFlow.map { list ->
            list.filter { ev ->
                val matchesQuery = query.isBlank() ||
                        ev.appLabel.contains(query, ignoreCase = true) ||
                        ev.latestTitle.contains(query, ignoreCase = true) ||
                        ev.latestText.contains(query, ignoreCase = true)
                val matchesFilter = when (filter) {
                    "ALLOWED" -> !ev.wasBlocked
                    "BLOCKED" -> ev.wasBlocked
                    "IMPORTANT" -> ev.isOtp || ev.isFinancial
                    else -> true
                }
                matchesQuery && matchesFilter
            }
        }

    override fun getTotalEventsCount(): Flow<Int> = _eventsFlow.map { it.size }
    override fun getBlockedEventsCount(): Flow<Int> = _eventsFlow.map { list -> list.count { it.wasBlocked } }
    override fun getAllowedEventsCount(): Flow<Int> = _eventsFlow.map { list -> list.count { !it.wasBlocked } }
    override fun getImportantEventsCount(): Flow<Int> = _eventsFlow.map { list -> list.count { it.isOtp || it.isFinancial } }

    override fun getEventsBetween(startTime: Long, endTime: Long): Flow<List<NotificationEvent>> =
        _eventsFlow.map { list -> list.filter { it.lastUpdatedAt in startTime..endTime } }

    override fun getEventsCountBetween(startTime: Long, endTime: Long): Flow<Int> =
        _eventsFlow.map { list -> list.count { it.lastUpdatedAt in startTime..endTime } }

    override fun getBlockedEventsCountBetween(startTime: Long, endTime: Long): Flow<Int> =
        _eventsFlow.map { list -> list.count { it.wasBlocked && it.lastUpdatedAt in startTime..endTime } }

    override fun getAllowedEventsCountBetween(startTime: Long, endTime: Long): Flow<Int> =
        _eventsFlow.map { list -> list.count { !it.wasBlocked && it.lastUpdatedAt in startTime..endTime } }

    override fun getImportantEventsCountBetween(startTime: Long, endTime: Long): Flow<Int> =
        _eventsFlow.map { list -> list.count { (it.isOtp || it.isFinancial) && it.lastUpdatedAt in startTime..endTime } }

    override fun getTopAppsBetween(startTime: Long, endTime: Long, limit: Int): Flow<List<AppNotificationCount>> =
        _eventsFlow.map { list ->
            list.filter { it.lastUpdatedAt in startTime..endTime }
                .groupBy { it.packageName to it.appLabel }
                .map { (k, v) ->
                    AppNotificationCount(
                        packageName = k.first,
                        appLabel = k.second,
                        totalCount = v.size,
                        blockedCount = v.count { it.wasBlocked }
                    )
                }
                .sortedByDescending { it.totalCount }
                .take(limit)
        }

    override suspend fun deleteEventsOlderThan(cutoffTimestamp: Long): Int {
        val toRemove = eventsMap.values.filter { it.lastUpdatedAt < cutoffTimestamp }.map { it.id }
        toRemove.forEach { eventsMap.remove(it) }
        notifyChanged()
        return toRemove.size
    }

    override suspend fun markEventAsRead(id: Long) {
        val e = eventsMap[id]
        if (e != null) {
            eventsMap[id] = e.copy(isRead = true)
            notifyChanged()
        }
    }

    override suspend fun markAllEventsAsRead() {
        eventsMap.keys.forEach { id ->
            val e = eventsMap[id]
            if (e != null) eventsMap[id] = e.copy(isRead = true)
        }
        notifyChanged()
    }

    override suspend fun updateBlockReason(id: Long, blockReason: String) {
        val e = eventsMap[id]
        if (e != null) {
            eventsMap[id] = e.copy(blockReason = blockReason)
            notifyChanged()
        }
    }

    override suspend fun insertUpdate(update: NotificationUpdate): Long {
        val id = if (update.id == 0L) nextUpdateId++ else update.id
        val saved = update.copy(id = id)
        updates.add(saved)
        return id
    }

    override fun getUpdatesForEvent(eventId: Long): Flow<List<NotificationUpdate>> =
        flowOf(updates.filter { it.eventId == eventId })

    override suspend fun getLatestUpdateForEvent(eventId: Long): NotificationUpdate? =
        updates.filter { it.eventId == eventId }.maxByOrNull { it.timestamp }

    override suspend fun deleteUpdatesForEvent(eventId: Long) {
        updates.removeAll { it.eventId == eventId }
    }
}

class FakeRuleDao : RuleDao {
    val rules = mutableListOf<BlockingRule>()
    val whitelistedApps = mutableMapOf<String, WhitelistedApp>()
    val whitelistedKeywords = mutableListOf<WhitelistedKeyword>()
    val blockedApps = mutableMapOf<String, BlockedApp>()
    val schedules = mutableListOf<Schedule>()
    val categories = mutableMapOf<String, NotificationCategory>()
    val conditions = mutableMapOf<Long, MutableList<RuleCondition>>()
    private var nextRuleId = 1L
    private var nextConditionId = 1L
    private var nextScheduleId = 1L

    override fun getAllRulesFlow(): Flow<List<BlockingRule>> = flowOf(rules)
    override suspend fun getAllRulesSync(): List<BlockingRule> = rules.toList()
    override suspend fun getRuleById(id: Long): BlockingRule? = rules.find { it.id == id }
    override suspend fun getRuleByType(type: String): BlockingRule? = rules.find { it.ruleType == type }
    override suspend fun insertRule(rule: BlockingRule): Long {
        val id = if (rule.id == 0L) nextRuleId++ else rule.id
        val saved = rule.copy(id = id)
        rules.removeAll { it.id == id }
        rules.add(saved)
        return id
    }
    override suspend fun insertRules(rules: List<BlockingRule>) {
        rules.forEach { insertRule(it) }
    }
    override suspend fun updateRule(rule: BlockingRule) {
        val index = rules.indexOfFirst { it.id == rule.id }
        if (index != -1) rules[index] = rule
    }
    override suspend fun deleteRule(rule: BlockingRule) { rules.removeAll { it.id == rule.id } }
    override suspend fun deleteRuleById(id: Long) { rules.removeAll { it.id == id } }
    override suspend fun setRuleEnabled(id: Long, isEnabled: Boolean, updatedAt: Long) {
        val r = rules.find { it.id == id }
        if (r != null) updateRule(r.copy(isEnabled = isEnabled, updatedAt = updatedAt))
    }
    override suspend fun setRuleEnabledByType(ruleType: String, isEnabled: Boolean, updatedAt: Long) {
        val r = rules.find { it.ruleType == ruleType }
        if (r != null) updateRule(r.copy(isEnabled = isEnabled, updatedAt = updatedAt))
    }
    override suspend fun getRulesCount(): Int = rules.size

    override fun getConditionsForRule(ruleId: Long): Flow<List<RuleCondition>> = flowOf(conditions[ruleId] ?: emptyList())
    override suspend fun getConditionsForRuleSync(ruleId: Long): List<RuleCondition> = conditions[ruleId] ?: emptyList()
    override suspend fun insertCondition(condition: RuleCondition): Long {
        val id = if (condition.id == 0L) nextConditionId++ else condition.id
        val saved = condition.copy(id = id)
        conditions.getOrPut(condition.ruleId) { mutableListOf() }.add(saved)
        return id
    }
    override suspend fun insertConditions(conditions: List<RuleCondition>) {
        conditions.forEach { insertCondition(it) }
    }
    override suspend fun deleteCondition(id: Long) {
        this.conditions.values.forEach { it.removeAll { c -> c.id == id } }
    }
    override suspend fun deleteConditionsForRule(ruleId: Long) { this.conditions.remove(ruleId) }

    override fun getAllWhitelistedApps(): Flow<List<WhitelistedApp>> = flowOf(whitelistedApps.values.toList())
    override suspend fun getAllWhitelistedAppsSync(): List<WhitelistedApp> = whitelistedApps.values.toList()
    override suspend fun isAppWhitelisted(packageName: String): Boolean = whitelistedApps.containsKey(packageName)
    override suspend fun insertWhitelistedApp(app: WhitelistedApp) { whitelistedApps[app.packageName] = app }
    override suspend fun deleteWhitelistedApp(packageName: String) { whitelistedApps.remove(packageName) }
    override fun getWhitelistedAppsCount(): Flow<Int> = flowOf(whitelistedApps.size)

    override fun getAllWhitelistedKeywords(): Flow<List<WhitelistedKeyword>> = flowOf(whitelistedKeywords.toList())
    override suspend fun getAllWhitelistedKeywordsSync(): List<WhitelistedKeyword> = whitelistedKeywords.toList()
    override suspend fun insertWhitelistedKeyword(keyword: WhitelistedKeyword) {
        whitelistedKeywords.removeAll { it.keyword.equals(keyword.keyword, ignoreCase = true) }
        whitelistedKeywords.add(keyword)
    }
    override suspend fun insertWhitelistedKeywords(keywords: List<WhitelistedKeyword>) {
        keywords.forEach { insertWhitelistedKeyword(it) }
    }
    override suspend fun deleteWhitelistedKeyword(keyword: String) {
        whitelistedKeywords.removeAll { it.keyword.equals(keyword, ignoreCase = true) }
    }
    override fun getWhitelistedKeywordsCount(): Flow<Int> = flowOf(whitelistedKeywords.size)

    override fun getAllBlockedApps(): Flow<List<BlockedApp>> = flowOf(blockedApps.values.toList())
    override suspend fun getAllBlockedAppsSync(): List<BlockedApp> = blockedApps.values.toList()
    override suspend fun getBlockedApp(packageName: String): BlockedApp? = blockedApps[packageName]
    override suspend fun isAppBlocked(packageName: String): Boolean = blockedApps[packageName]?.isBlocked == true
    override suspend fun insertBlockedApp(app: BlockedApp) { blockedApps[app.packageName] = app }
    override suspend fun insertBlockedApps(apps: List<BlockedApp>) { apps.forEach { blockedApps[it.packageName] = it } }
    override suspend fun updateBlockedApp(app: BlockedApp) { blockedApps[app.packageName] = app }
    override suspend fun deleteBlockedApp(packageName: String) { blockedApps.remove(packageName) }
    override suspend fun setAppBlocked(packageName: String, isBlocked: Boolean, updatedAt: Long) {
        val app = blockedApps[packageName]
        if (app != null) blockedApps[packageName] = app.copy(isBlocked = isBlocked, updatedAt = updatedAt)
    }
    override fun getBlockedAppsCount(): Flow<Int> = flowOf(blockedApps.values.count { it.isBlocked })

    override fun getAllSchedules(): Flow<List<Schedule>> = flowOf(schedules.toList())
    override suspend fun getAllSchedulesSync(): List<Schedule> = schedules.toList()
    override suspend fun getScheduleById(id: Long): Schedule? = schedules.find { it.id == id }
    override suspend fun insertSchedule(schedule: Schedule): Long {
        val id = if (schedule.id == 0L) nextScheduleId++ else schedule.id
        val saved = schedule.copy(id = id)
        schedules.removeAll { it.id == id }
        schedules.add(saved)
        return id
    }
    override suspend fun updateSchedule(schedule: Schedule) {
        val i = schedules.indexOfFirst { it.id == schedule.id }
        if (i != -1) schedules[i] = schedule
    }
    override suspend fun deleteSchedule(id: Long) { schedules.removeAll { it.id == id } }
    override suspend fun setScheduleEnabled(id: Long, isEnabled: Boolean) {
        val s = schedules.find { it.id == id }
        if (s != null) updateSchedule(s.copy(isEnabled = isEnabled))
    }
    override fun getActiveSchedulesCount(): Flow<Int> = flowOf(schedules.count { it.isEnabled })

    override fun getAllCategories(): Flow<List<NotificationCategory>> = flowOf(categories.values.toList())
    override suspend fun getAllCategoriesSync(): List<NotificationCategory> = categories.values.toList()
    override suspend fun getCategory(categoryId: String): NotificationCategory? = categories[categoryId]
    override suspend fun insertCategory(category: NotificationCategory) { categories[category.categoryId] = category }
    override suspend fun insertCategories(categories: List<NotificationCategory>) {
        categories.forEach { this.categories[it.categoryId] = it }
    }
    override suspend fun updateCategory(category: NotificationCategory) { categories[category.categoryId] = category }
    override suspend fun deleteCategory(categoryId: String) { categories.remove(categoryId) }
    override suspend fun getCategoriesCount(): Int = categories.size
}
