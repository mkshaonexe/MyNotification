package com.my.notificationai.core.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blocking_rules")
data class BlockingRule(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "description")
    val description: String,
    @ColumnInfo(name = "rule_type")
    val ruleType: String, // GLOBAL_BLOCK, SOCIAL_MEDIA, SELECTED_APPS, ALLOW_IMPORTANT_KEYWORDS, SCHEDULE_BLOCKING, BLOCK_EVERYTHING, CUSTOM
    @ColumnInfo(name = "action")
    val action: String = "BLOCK", // BLOCK, ALLOW
    @ColumnInfo(name = "is_enabled")
    val isEnabled: Boolean = true,
    @ColumnInfo(name = "priority")
    val priority: Int = 0,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)
