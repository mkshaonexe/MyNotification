package com.my.notificationai.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
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

@Database(
    entities = [
        NotificationEvent::class,
        NotificationUpdate::class,
        BlockingRule::class,
        RuleCondition::class,
        WhitelistedApp::class,
        WhitelistedKeyword::class,
        BlockedApp::class,
        Schedule::class,
        NotificationCategory::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun notificationDao(): NotificationDao
    abstract fun ruleDao(): RuleDao
}
