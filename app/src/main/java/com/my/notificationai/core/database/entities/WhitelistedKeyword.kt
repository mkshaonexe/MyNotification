package com.my.notificationai.core.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "whitelisted_keywords")
data class WhitelistedKeyword(
    @PrimaryKey
    @ColumnInfo(name = "keyword")
    val keyword: String,
    @ColumnInfo(name = "category")
    val category: String = "OTP",
    @ColumnInfo(name = "added_at")
    val addedAt: Long = System.currentTimeMillis()
)
