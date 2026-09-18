package com.my.notificationai.core.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "notification_events",
    indices = [
        Index(value = ["notification_key"], name = "index_events_key"),
        Index(value = ["package_name"], name = "index_events_pkg"),
        Index(value = ["last_updated_at"], name = "index_events_updated"),
        Index(value = ["was_blocked"], name = "index_events_blocked")
    ]
)
data class NotificationEvent(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "notification_key")
    val notificationKey: String,
    @ColumnInfo(name = "package_name")
    val packageName: String,
    @ColumnInfo(name = "app_label")
    val appLabel: String,
    @ColumnInfo(name = "initial_title")
    val initialTitle: String,
    @ColumnInfo(name = "latest_title")
    val latestTitle: String,
    @ColumnInfo(name = "initial_text")
    val initialText: String,
    @ColumnInfo(name = "latest_text")
    val latestText: String,
    @ColumnInfo(name = "big_text")
    val bigText: String? = null,
    @ColumnInfo(name = "sub_text")
    val subText: String? = null,
    @ColumnInfo(name = "channel_id")
    val channelId: String,
    @ColumnInfo(name = "channel_name")
    val channelName: String? = null,
    @ColumnInfo(name = "category")
    val category: String? = null,
    @ColumnInfo(name = "flags")
    val flags: Int = 0,
    @ColumnInfo(name = "importance")
    val importance: Int = 0,
    @ColumnInfo(name = "is_ongoing")
    val isOngoing: Boolean = false,
    @ColumnInfo(name = "is_clearable")
    val isClearable: Boolean = true,
    @ColumnInfo(name = "is_progress")
    val isProgress: Boolean = false,
    @ColumnInfo(name = "progress")
    val progress: Int = 0,
    @ColumnInfo(name = "max_progress")
    val maxProgress: Int = 0,
    @ColumnInfo(name = "is_otp")
    val isOtp: Boolean = false,
    @ColumnInfo(name = "otp_code")
    val otpCode: String? = null,
    @ColumnInfo(name = "is_financial")
    val isFinancial: Boolean = false,
    @ColumnInfo(name = "is_promotional")
    val isPromotional: Boolean = false,
    @ColumnInfo(name = "first_seen_at")
    val firstSeenAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "last_updated_at")
    val lastUpdatedAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "removed_at")
    val removedAt: Long? = null,
    @ColumnInfo(name = "duration_ms")
    val durationMs: Long = 0L,
    @ColumnInfo(name = "update_count")
    val updateCount: Int = 1,
    @ColumnInfo(name = "was_blocked")
    val wasBlocked: Boolean = false,
    @ColumnInfo(name = "block_reason")
    val blockReason: String? = null,
    @ColumnInfo(name = "responsible_rule_id")
    val responsibleRuleId: Long? = null,
    @ColumnInfo(name = "matching_rule_name")
    val matchingRuleName: String? = null,
    @ColumnInfo(name = "is_read")
    val isRead: Boolean = false
)
