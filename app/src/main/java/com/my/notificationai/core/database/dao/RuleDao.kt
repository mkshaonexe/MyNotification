package com.my.notificationai.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.my.notificationai.core.database.entities.BlockedApp
import com.my.notificationai.core.database.entities.BlockingRule
import com.my.notificationai.core.database.entities.NotificationCategory
import com.my.notificationai.core.database.entities.RuleCondition
import com.my.notificationai.core.database.entities.Schedule
import com.my.notificationai.core.database.entities.WhitelistedApp
import com.my.notificationai.core.database.entities.WhitelistedKeyword
import kotlinx.coroutines.flow.Flow

@Dao
interface RuleDao {

    // --- Blocking Rules ---

    @Query("SELECT * FROM blocking_rules ORDER BY priority DESC")
    fun getAllRulesFlow(): Flow<List<BlockingRule>>

    @Query("SELECT * FROM blocking_rules ORDER BY priority DESC")
    suspend fun getAllRulesSync(): List<BlockingRule>

    @Query("SELECT * FROM blocking_rules WHERE id = :id LIMIT 1")
    suspend fun getRuleById(id: Long): BlockingRule?

    @Query("SELECT * FROM blocking_rules WHERE rule_type = :type LIMIT 1")
    suspend fun getRuleByType(type: String): BlockingRule?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRule(rule: BlockingRule): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRules(rules: List<BlockingRule>)

    @Update
    suspend fun updateRule(rule: BlockingRule)

    @Delete
    suspend fun deleteRule(rule: BlockingRule)

    @Query("DELETE FROM blocking_rules WHERE id = :id")
    suspend fun deleteRuleById(id: Long)

    @Query("UPDATE blocking_rules SET is_enabled = :isEnabled, updated_at = :updatedAt WHERE id = :id")
    suspend fun setRuleEnabled(id: Long, isEnabled: Boolean, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE blocking_rules SET is_enabled = :isEnabled, updated_at = :updatedAt WHERE rule_type = :ruleType")
    suspend fun setRuleEnabledByType(ruleType: String, isEnabled: Boolean, updatedAt: Long = System.currentTimeMillis())

    @Query("SELECT COUNT(*) FROM blocking_rules")
    suspend fun getRulesCount(): Int

    // --- Rule Conditions ---

    @Query("SELECT * FROM rule_conditions WHERE rule_id = :ruleId")
    fun getConditionsForRule(ruleId: Long): Flow<List<RuleCondition>>

    @Query("SELECT * FROM rule_conditions WHERE rule_id = :ruleId")
    suspend fun getConditionsForRuleSync(ruleId: Long): List<RuleCondition>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCondition(condition: RuleCondition): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConditions(conditions: List<RuleCondition>)

    @Query("DELETE FROM rule_conditions WHERE id = :id")
    suspend fun deleteCondition(id: Long)

    @Query("DELETE FROM rule_conditions WHERE rule_id = :ruleId")
    suspend fun deleteConditionsForRule(ruleId: Long)

    // --- Whitelisted Apps ---

    @Query("SELECT * FROM whitelisted_apps ORDER BY app_label ASC")
    fun getAllWhitelistedApps(): Flow<List<WhitelistedApp>>

    @Query("SELECT * FROM whitelisted_apps")
    suspend fun getAllWhitelistedAppsSync(): List<WhitelistedApp>

    @Query("SELECT EXISTS(SELECT 1 FROM whitelisted_apps WHERE package_name = :packageName)")
    suspend fun isAppWhitelisted(packageName: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWhitelistedApp(app: WhitelistedApp)

    @Query("DELETE FROM whitelisted_apps WHERE package_name = :packageName")
    suspend fun deleteWhitelistedApp(packageName: String)

    @Query("SELECT COUNT(*) FROM whitelisted_apps")
    fun getWhitelistedAppsCount(): Flow<Int>

    // --- Whitelisted Keywords ---

    @Query("SELECT * FROM whitelisted_keywords ORDER BY keyword ASC")
    fun getAllWhitelistedKeywords(): Flow<List<WhitelistedKeyword>>

    @Query("SELECT * FROM whitelisted_keywords")
    suspend fun getAllWhitelistedKeywordsSync(): List<WhitelistedKeyword>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWhitelistedKeyword(keyword: WhitelistedKeyword)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWhitelistedKeywords(keywords: List<WhitelistedKeyword>)

    @Query("DELETE FROM whitelisted_keywords WHERE keyword = :keyword")
    suspend fun deleteWhitelistedKeyword(keyword: String)

    @Query("SELECT COUNT(*) FROM whitelisted_keywords")
    fun getWhitelistedKeywordsCount(): Flow<Int>

    // --- Blocked Apps (Selected Apps) ---

    @Query("SELECT * FROM blocked_apps ORDER BY app_label ASC")
    fun getAllBlockedApps(): Flow<List<BlockedApp>>

    @Query("SELECT * FROM blocked_apps")
    suspend fun getAllBlockedAppsSync(): List<BlockedApp>

    @Query("SELECT * FROM blocked_apps WHERE package_name = :packageName LIMIT 1")
    suspend fun getBlockedApp(packageName: String): BlockedApp?

    @Query("SELECT EXISTS(SELECT 1 FROM blocked_apps WHERE package_name = :packageName AND is_blocked = 1)")
    suspend fun isAppBlocked(packageName: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlockedApp(app: BlockedApp)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlockedApps(apps: List<BlockedApp>)

    @Update
    suspend fun updateBlockedApp(app: BlockedApp)

    @Query("DELETE FROM blocked_apps WHERE package_name = :packageName")
    suspend fun deleteBlockedApp(packageName: String)

    @Query("UPDATE blocked_apps SET is_blocked = :isBlocked, updated_at = :updatedAt WHERE package_name = :packageName")
    suspend fun setAppBlocked(packageName: String, isBlocked: Boolean, updatedAt: Long = System.currentTimeMillis())

    @Query("SELECT COUNT(*) FROM blocked_apps WHERE is_blocked = 1")
    fun getBlockedAppsCount(): Flow<Int>

    // --- Schedules ---

    @Query("SELECT * FROM schedules ORDER BY start_hour ASC, start_minute ASC")
    fun getAllSchedules(): Flow<List<Schedule>>

    @Query("SELECT * FROM schedules")
    suspend fun getAllSchedulesSync(): List<Schedule>

    @Query("SELECT * FROM schedules WHERE id = :id LIMIT 1")
    suspend fun getScheduleById(id: Long): Schedule?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: Schedule): Long

    @Update
    suspend fun updateSchedule(schedule: Schedule)

    @Query("DELETE FROM schedules WHERE id = :id")
    suspend fun deleteSchedule(id: Long)

    @Query("UPDATE schedules SET is_enabled = :isEnabled WHERE id = :id")
    suspend fun setScheduleEnabled(id: Long, isEnabled: Boolean)

    @Query("SELECT COUNT(*) FROM schedules WHERE is_enabled = 1")
    fun getActiveSchedulesCount(): Flow<Int>

    // --- Categories ---

    @Query("SELECT * FROM notification_categories ORDER BY name ASC")
    fun getAllCategories(): Flow<List<NotificationCategory>>

    @Query("SELECT * FROM notification_categories")
    suspend fun getAllCategoriesSync(): List<NotificationCategory>

    @Query("SELECT * FROM notification_categories WHERE category_id = :categoryId LIMIT 1")
    suspend fun getCategory(categoryId: String): NotificationCategory?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: NotificationCategory)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<NotificationCategory>)

    @Update
    suspend fun updateCategory(category: NotificationCategory)

    @Query("DELETE FROM notification_categories WHERE category_id = :categoryId")
    suspend fun deleteCategory(categoryId: String)

    @Query("SELECT COUNT(*) FROM notification_categories")
    suspend fun getCategoriesCount(): Int
}
