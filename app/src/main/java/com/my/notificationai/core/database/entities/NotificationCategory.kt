package com.my.notificationai.core.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notification_categories")
data class NotificationCategory(
    @PrimaryKey
    @ColumnInfo(name = "category_id")
    val categoryId: String,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "package_names")
    val packageNames: String, // Comma-separated package names
    @ColumnInfo(name = "is_editable")
    val isEditable: Boolean = true
)
