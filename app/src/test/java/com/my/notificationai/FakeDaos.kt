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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeNotificationDao : NotificationDao {
    val events = mutableMapOf<Long, NotificationEvent>()
    val updates = mutableListOf<NotificationUpdate>()
    private var nextId = 1L

    override suspend fun insertEvent(event: NotificationEvent): Long {
        val id = nextId++
        val saved = event.copy(id = id)
        events[id] = saved
        return id
    }

    override suspend fun updateEvent(event: NotificationEvent) {
        events[event.id] = event
    }

    override suspend fun deleteEvent(event: NotificationEvent) {
        events.remove(event.id)
    }

    override suspend fun deleteEventById(id: Long) {
        events.remove(id)
    }

    override suspend fun deleteAllEvents() {
        events.clear()
        updates.clear()
    }

    override suspend fun getEventById(id: Long): NotificationEvent? = events[id]

    override fun getEventByIdFlow(id: Long): Flow<NotificationEvent?> = MutableStateFlow(events[id])

    override suspend fun getEventByKey(key: String): NotificationEvent? = events.values.find { it.notificationKey == key }

    override suspend fun getActiveEventByKey(key: String): NotificationEvent? =
        events.values.filter { it.notificationKey == key && it.removedAt == null }.maxByOrNull { it.id }

    override fun getAllEventsFlow(): Flow<List<NotificationEvent>> = MutableStateFlow(events.values.toList())

    override fun getFilteredEventsFlow(query: String, filter: String): Flow<List<NotificationEvent>> =
        MutableStateFlow(events.values.toList())

    override fun getTotalEventsCount(): Flow<Int> = MutableStateFlow(events.size)
    override fun getBlockedEventsCount(): Flow<Int> = MutableStateFlow(events.values.count { it.wasBlocked })
    override fun getAllowedEventsCount(): Flow<Int> = MutableStateFlow(events.values.count { !it.wasBlocked })
    override fun getImportantEventsCount(): Flow<Int> = MutableStateFlow(events.values.count { it.isOtp || it.isFinancial })

    override fun getEventsBetween(startTime: Long, endTime: Long): Flow<List<NotificationEvent>> =
        MutableStateFlow(events.values.filter { it.lastUpdatedAt in startTime..endTime })

    override fun getEventsCountBetween(startTime: Long, endTime: Long): Flow<Int> =
        MutableStateFlow(events.values.count { it.lastUpdatedAt in startTime..endTime })

    override fun getBlockedEventsCountBetween(startTime: Long, endTime: Long): Flow<Int> =
        MutableStateFlow(events.values.count { it.wasBlocked && it.lastUpdatedAt in startTime..endTime })

    override fun getAllowedEventsCountBetween(startTime: Long, endTime: Long): Flow<Int> =
        MutableStateFlow(events.values.count { !it.wasBlocked && it.lastUpdatedAt in startTime..endTime })

    override fun getImportantEventsCountBetween(startTime: Long, endTime: Long): Flow<Int> =
        MutableStateFlow(events.values.count { (it.isOtp || it.isFinancial) && it.lastUpdatedAt in startTime..endTime })

    override fun getTopAppsBetween(startTime: Long, endTime: Long, limit: Int): Flow<List<AppNotificationCount>> {
        val grouped = events.values
            .filter { it.lastUpdatedAt in startTime..endTime }
            .groupBy { it.packageName to it.appLabel }
            .map { (k, v) ->
                val blockedCount = v.count { it.wasBlocked }
                AppNotificationCount(
                    packageName = k.first,
                    appLabel = k.second,
                    totalCount = v.size,
                    blockedCount = blockedCount
                )
            }
            .sortedByDescending { it.totalCount }
            .take(limit)
        return MutableStateFlow(grouped)
    }

    override suspend fun deleteEventsOlderThan(cutoffTimestamp: Long): Int {
        val toRemove = events.values.filter { it.lastUpdatedAt < cutoffTimestamp }.map { it.id }
        toRemove.forEach { events.remove(it) }
        return toRemove.size
    }

    override suspend fun markEventAsRead(id: Long) {
        val e = events[id]
        if (e != null) events[id] = e.copy(isRead = true)
    }

    override suspend fun markAllEventsAsRead() {
        events.keys.forEach { id ->
            val e = events[id]
            if (e != null) events[id] = e.copy(isRead = true)
        }
    }

    override suspend fun updateBlockReason(id: Long, blockReason: String) {
        val e = events[id]
        if (e != null) events[id] = e.copy(blockReason = blockReason)
    }

    override suspend fun insertUpdate(update: NotificationUpdate): Long {
        updates.add(update)
        return updates.size.toLong()
    }

    override fun getUpdatesForEvent(eventId: Long): Flow<List<NotificationUpdate>> =
        MutableStateFlow(updates.filter { it.eventId == eventId })

    override suspend fun getLatestUpdateForEvent(eventId: Long): NotificationUpdate? =
        updates.filter { it.eventId == eventId }.maxByOrNull { it.timestamp }

    override suspend fun deleteUpdatesForEvent(eventId: Long) {
        updates.removeIf { it.eventId == eventId }
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

    override fun getAllRulesFlow(): Flow<List<BlockingRule>> = MutableStateFlow(rules)
    override suspend fun getAllRulesSync(): List<BlockingRule> = rules
    override suspend fun getRuleById(id: Long): BlockingRule? = rules.find { it.id == id }
    override suspend fun getRuleByType(type: String): BlockingRule? = rules.find { it.ruleType == type }
    override suspend fun insertRule(rule: BlockingRule): Long {
        rules.add(rule)
        return rule.id
    }
    override suspend fun insertRules(rules: List<BlockingRule>) { this.rules.addAll(rules) }
    override suspend fun updateRule(rule: BlockingRule) {
        val index = rules.indexOfFirst { it.id == rule.id }
        if (index != -1) rules[index] = rule
    }
    override suspend fun deleteRule(rule: BlockingRule) { rules.removeIf { it.id == rule.id } }
    override suspend fun deleteRuleById(id: Long) { rules.removeIf { it.id == id } }
    override suspend fun setRuleEnabled(id: Long, isEnabled: Boolean, updatedAt: Long) {
        val r = rules.find { it.id == id }
        if (r != null) updateRule(r.copy(isEnabled = isEnabled, updatedAt = updatedAt))
    }
    override suspend fun setRuleEnabledByType(ruleType: String, isEnabled: Boolean, updatedAt: Long) {
        val r = rules.find { it.ruleType == ruleType }
        if (r != null) updateRule(r.copy(isEnabled = isEnabled, updatedAt = updatedAt))
    }
    override suspend fun getRulesCount(): Int = rules.size

    override fun getConditionsForRule(ruleId: Long): Flow<List<RuleCondition>> = MutableStateFlow(conditions[ruleId] ?: emptyList())
    override suspend fun getConditionsForRuleSync(ruleId: Long): List<RuleCondition> = conditions[ruleId] ?: emptyList()
    override suspend fun insertCondition(condition: RuleCondition): Long {
        conditions.getOrPut(condition.ruleId) { mutableListOf() }.add(condition)
        return condition.id
    }
    override suspend fun insertConditions(conditions: List<RuleCondition>) {
        conditions.forEach { insertCondition(it) }
    }
    override suspend fun deleteCondition(id: Long) {
        this.conditions.values.forEach { it.removeIf { c -> c.id == id } }
    }
    override suspend fun deleteConditionsForRule(ruleId: Long) { this.conditions.remove(ruleId) }

    override fun getAllWhitelistedApps(): Flow<List<WhitelistedApp>> = MutableStateFlow(whitelistedApps.values.toList())
    override suspend fun getAllWhitelistedAppsSync(): List<WhitelistedApp> = whitelistedApps.values.toList()
    override suspend fun isAppWhitelisted(packageName: String): Boolean = whitelistedApps.containsKey(packageName)
    override suspend fun insertWhitelistedApp(app: WhitelistedApp) { whitelistedApps[app.packageName] = app }
    override suspend fun deleteWhitelistedApp(packageName: String) { whitelistedApps.remove(packageName) }
    override fun getWhitelistedAppsCount(): Flow<Int> = MutableStateFlow(whitelistedApps.size)

    override fun getAllWhitelistedKeywords(): Flow<List<WhitelistedKeyword>> = MutableStateFlow(whitelistedKeywords)
    override suspend fun getAllWhitelistedKeywordsSync(): List<WhitelistedKeyword> = whitelistedKeywords
    override suspend fun insertWhitelistedKeyword(keyword: WhitelistedKeyword) { whitelistedKeywords.add(keyword) }
    override suspend fun insertWhitelistedKeywords(keywords: List<WhitelistedKeyword>) { whitelistedKeywords.addAll(keywords) }
    override suspend fun deleteWhitelistedKeyword(keyword: String) { whitelistedKeywords.removeIf { it.keyword == keyword } }
    override fun getWhitelistedKeywordsCount(): Flow<Int> = MutableStateFlow(whitelistedKeywords.size)

    override fun getAllBlockedApps(): Flow<List<BlockedApp>> = MutableStateFlow(blockedApps.values.toList())
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
    override fun getBlockedAppsCount(): Flow<Int> = MutableStateFlow(blockedApps.values.count { it.isBlocked })

    override fun getAllSchedules(): Flow<List<Schedule>> = MutableStateFlow(schedules)
    override suspend fun getAllSchedulesSync(): List<Schedule> = schedules
    override suspend fun getScheduleById(id: Long): Schedule? = schedules.find { it.id == id }
    override suspend fun insertSchedule(schedule: Schedule): Long {
        schedules.add(schedule)
        return schedule.id
    }
    override suspend fun updateSchedule(schedule: Schedule) {
        val i = schedules.indexOfFirst { it.id == schedule.id }
        if (i != -1) schedules[i] = schedule
    }
    override suspend fun deleteSchedule(id: Long) { schedules.removeIf { it.id == id } }
    override suspend fun setScheduleEnabled(id: Long, isEnabled: Boolean) {
        val s = schedules.find { it.id == id }
        if (s != null) updateSchedule(s.copy(isEnabled = isEnabled))
    }
    override fun getActiveSchedulesCount(): Flow<Int> = MutableStateFlow(schedules.count { it.isEnabled })

    override fun getAllCategories(): Flow<List<NotificationCategory>> = MutableStateFlow(categories.values.toList())
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
