package com.my.notificationai.core.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blocked_apps")
data class BlockedApp(
    @PrimaryKey
    @ColumnInfo(name = "package_name")
    val packageName: String,
    @ColumnInfo(name = "app_label")
    val appLabel: String,
    @ColumnInfo(name = "is_blocked")
    val isBlocked: Boolean = true,
    @ColumnInfo(name = "priority")
    val priority: String = "Normal",
    @ColumnInfo(name = "category")
    val category: String = "Selected",
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)
