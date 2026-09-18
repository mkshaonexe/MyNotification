package com.my.notificationai.core.database.dao

import androidx.room.ColumnInfo

data class AppNotificationCount(
    @ColumnInfo(name = "package_name") val packageName: String,
    @ColumnInfo(name = "app_label") val appLabel: String,
    @ColumnInfo(name = "total_count") val totalCount: Int,
    @ColumnInfo(name = "blocked_count") val blockedCount: Int
)
