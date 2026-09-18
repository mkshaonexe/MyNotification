package com.my.notificationai.core.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "schedules")
data class Schedule(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "title")
    val title: String,
    @ColumnInfo(name = "start_hour")
    val startHour: Int,
    @ColumnInfo(name = "start_minute")
    val startMinute: Int,
    @ColumnInfo(name = "end_hour")
    val endHour: Int,
    @ColumnInfo(name = "end_minute")
    val endMinute: Int,
    @ColumnInfo(name = "repeat_days")
    val repeatDays: String = "ALL", // "ALL" or comma-separated "MON,TUE,WED,THU,FRI,SAT,SUN"
    @ColumnInfo(name = "action")
    val action: String = "BLOCK_ALL",
    @ColumnInfo(name = "is_enabled")
    val isEnabled: Boolean = true,
    @ColumnInfo(name = "exceptions")
    val exceptions: String = "OTP,Financial,Calls",
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
