package com.my.notificationai.core.domain.engine

import com.my.notificationai.core.database.dao.RuleDao
import com.my.notificationai.core.database.entities.BlockedApp
import com.my.notificationai.core.database.entities.BlockingRule
import com.my.notificationai.core.database.entities.NotificationCategory
import com.my.notificationai.core.database.entities.Schedule
import com.my.notificationai.core.database.entities.WhitelistedApp
import com.my.notificationai.core.database.entities.WhitelistedKeyword
import org.json.JSONArray
import org.json.JSONObject

class BackupManager(
    private val ruleDao: RuleDao
) {

    suspend fun exportBackupJson(): String {
        val root = JSONObject()
        root.put("version", 1)
        root.put("timestamp", System.currentTimeMillis())

        // Rules
        val rules = ruleDao.getAllRulesSync()
        val rulesArray = JSONArray()
        for (r in rules) {
            val rObj = JSONObject().apply {
                put("name", r.name)
                put("description", r.description)
                put("rule_type", r.ruleType)
                put("action", r.action)
                put("is_enabled", r.isEnabled)
                put("priority", r.priority)
            }
            rulesArray.put(rObj)
        }
        root.put("rules", rulesArray)

        // Whitelisted Apps
        val whitelistedApps = ruleDao.getAllWhitelistedAppsSync()
        val wlAppsArray = JSONArray()
        for (w in whitelistedApps) {
            val wObj = JSONObject().apply {
                put("package_name", w.packageName)
                put("app_label", w.appLabel)
                put("reason", w.reason)
            }
            wlAppsArray.put(wObj)
        }
        root.put("whitelisted_apps", wlAppsArray)

        // Whitelisted Keywords
        val keywords = ruleDao.getAllWhitelistedKeywordsSync()
        val kwArray = JSONArray()
        for (k in keywords) {
            val kObj = JSONObject().apply {
                put("keyword", k.keyword)
                put("category", k.category)
            }
            kwArray.put(kObj)
        }
        root.put("whitelisted_keywords", kwArray)

        // Blocked Apps
        val blockedApps = ruleDao.getAllBlockedAppsSync()
        val bAppsArray = JSONArray()
        for (b in blockedApps) {
            val bObj = JSONObject().apply {
                put("package_name", b.packageName)
                put("app_label", b.appLabel)
                put("is_blocked", b.isBlocked)
                put("priority", b.priority)
                put("category", b.category)
            }
            bAppsArray.put(bObj)
        }
        root.put("blocked_apps", bAppsArray)

        // Schedules
        val schedules = ruleDao.getAllSchedulesSync()
        val schedArray = JSONArray()
        for (s in schedules) {
            val sObj = JSONObject().apply {
                put("title", s.title)
                put("start_hour", s.startHour)
                put("start_minute", s.startMinute)
                put("end_hour", s.endHour)
                put("end_minute", s.endMinute)
                put("repeat_days", s.repeatDays)
                put("action", s.action)
                put("is_enabled", s.isEnabled)
                put("exceptions", s.exceptions)
            }
            schedArray.put(sObj)
        }
        root.put("schedules", schedArray)

        // Categories
        val categories = ruleDao.getAllCategoriesSync()
        val catArray = JSONArray()
        for (c in categories) {
            val cObj = JSONObject().apply {
                put("category_id", c.categoryId)
                put("name", c.name)
                put("package_names", c.packageNames)
                put("is_editable", c.isEditable)
            }
            catArray.put(cObj)
        }
        root.put("categories", catArray)

        return root.toString(2)
    }

    suspend fun importBackupJson(jsonString: String): Boolean {
        return try {
            val root = JSONObject(jsonString)

            if (root.has("whitelisted_apps")) {
                val wlAppsArray = root.getJSONArray("whitelisted_apps")
                for (i in 0 until wlAppsArray.length()) {
                    val obj = wlAppsArray.getJSONObject(i)
                    ruleDao.insertWhitelistedApp(
                        WhitelistedApp(
                            packageName = obj.getString("package_name"),
                            appLabel = obj.getString("app_label"),
                            reason = obj.optString("reason", "Always allowed")
                        )
                    )
                }
            }

            if (root.has("whitelisted_keywords")) {
                val kwArray = root.getJSONArray("whitelisted_keywords")
                for (i in 0 until kwArray.length()) {
                    val obj = kwArray.getJSONObject(i)
                    ruleDao.insertWhitelistedKeyword(
                        WhitelistedKeyword(
                            keyword = obj.getString("keyword"),
                            category = obj.optString("category", "OTP")
                        )
                    )
                }
            }

            if (root.has("blocked_apps")) {
                val bAppsArray = root.getJSONArray("blocked_apps")
                for (i in 0 until bAppsArray.length()) {
                    val obj = bAppsArray.getJSONObject(i)
                    ruleDao.insertBlockedApp(
                        BlockedApp(
                            packageName = obj.getString("package_name"),
                            appLabel = obj.getString("app_label"),
                            isBlocked = obj.optBoolean("is_blocked", true),
                            priority = obj.optString("priority", "Normal"),
                            category = obj.optString("category", "Selected")
                        )
                    )
                }
            }

            if (root.has("schedules")) {
                val schedArray = root.getJSONArray("schedules")
                for (i in 0 until schedArray.length()) {
                    val obj = schedArray.getJSONObject(i)
                    ruleDao.insertSchedule(
                        Schedule(
                            title = obj.getString("title"),
                            startHour = obj.getInt("start_hour"),
                            startMinute = obj.getInt("start_minute"),
                            endHour = obj.getInt("end_hour"),
                            endMinute = obj.getInt("end_minute"),
                            repeatDays = obj.optString("repeat_days", "ALL"),
                            action = obj.optString("action", "BLOCK_ALL"),
                            isEnabled = obj.optBoolean("is_enabled", true),
                            exceptions = obj.optString("exceptions", "OTP,Financial,Calls")
                        )
                    )
                }
            }

            if (root.has("categories")) {
                val catArray = root.getJSONArray("categories")
                for (i in 0 until catArray.length()) {
                    val obj = catArray.getJSONObject(i)
                    ruleDao.insertCategory(
                        NotificationCategory(
                            categoryId = obj.getString("category_id"),
                            name = obj.getString("name"),
                            packageNames = obj.getString("package_names"),
                            isEditable = obj.optBoolean("is_editable", true)
                        )
                    )
                }
            }

            true
        } catch (e: Exception) {
            false
        }
    }
}
