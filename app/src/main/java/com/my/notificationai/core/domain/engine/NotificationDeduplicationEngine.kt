package com.my.notificationai.core.domain.engine

import android.app.Notification
import android.service.notification.StatusBarNotification
import com.my.notificationai.core.database.dao.NotificationDao
import com.my.notificationai.core.database.entities.NotificationEvent
import com.my.notificationai.core.database.entities.NotificationUpdate
import com.my.notificationai.core.domain.models.RuleEvaluationResult
import java.util.concurrent.ConcurrentHashMap

class NotificationDeduplicationEngine(
    private val notificationDao: NotificationDao
) {

    // Thread-safe active event index: notificationKey -> Event ID
    private val activeEvents = ConcurrentHashMap<String, Long>()

    // Throttle tracking: notificationKey -> lastSnapshotTimeMs
    private val lastSnapshotTime = ConcurrentHashMap<String, Long>()

    suspend fun processPostedNotification(
        sbn: StatusBarNotification,
        appLabel: String,
        ruleResult: RuleEvaluationResult
    ): Long {
        val key = sbn.key
        val packageName = sbn.packageName
        val notification = sbn.notification
        val extras = notification?.extras
        val title = extras?.getCharSequence("android.title")?.toString() ?: ""
        val text = extras?.getCharSequence("android.text")?.toString() ?: ""
        val bigText = extras?.getCharSequence("android.bigText")?.toString()
        val subText = extras?.getCharSequence("android.subText")?.toString()
        val channelId = notification?.channelId ?: ""
        val flags = notification?.flags ?: 0
        val isOngoing = (flags and Notification.FLAG_ONGOING_EVENT) != 0
        val isClearable = (flags and Notification.FLAG_NO_CLEAR) == 0
        val progress = extras?.getInt(Notification.EXTRA_PROGRESS, 0) ?: 0
        val maxProgress = extras?.getInt(Notification.EXTRA_PROGRESS_MAX, 0) ?: 0
        val isProgress = maxProgress > 0
        val now = System.currentTimeMillis()

        val activeEventId = activeEvents[key]
        if (activeEventId != null) {
            val existing = notificationDao.getEventById(activeEventId)
            if (existing != null) {
                // Update existing event
                val updated = existing.copy(
                    latestTitle = title,
                    latestText = text,
                    bigText = bigText ?: existing.bigText,
                    subText = subText ?: existing.subText,
                    isOngoing = isOngoing,
                    isClearable = isClearable,
                    isProgress = isProgress,
                    progress = progress,
                    maxProgress = maxProgress,
                    lastUpdatedAt = now,
                    updateCount = existing.updateCount + 1,
                    wasBlocked = ruleResult.shouldBlock,
                    blockReason = ruleResult.reason,
                    responsibleRuleId = ruleResult.ruleId ?: existing.responsibleRuleId,
                    matchingRuleName = ruleResult.ruleName ?: existing.matchingRuleName
                )
                notificationDao.updateEvent(updated)

                // Throttle snapshot to max 1 per 500ms
                val lastSnapshot = lastSnapshotTime[key] ?: 0L
                if (now - lastSnapshot >= 500L) {
                    notificationDao.insertUpdate(
                        NotificationUpdate(
                            eventId = existing.id,
                            title = title,
                            text = text,
                            progress = progress,
                            timestamp = now
                        )
                    )
                    lastSnapshotTime[key] = now
                }
                return existing.id
            }
        }

        // New logical event
        val newEvent = NotificationEvent(
            notificationKey = key,
            packageName = packageName,
            appLabel = appLabel,
            initialTitle = title,
            latestTitle = title,
            initialText = text,
            latestText = text,
            bigText = bigText,
            subText = subText,
            channelId = channelId,
            category = notification?.category,
            flags = flags,
            importance = 0,
            isOngoing = isOngoing,
            isClearable = isClearable,
            isProgress = isProgress,
            progress = progress,
            maxProgress = maxProgress,
            isOtp = ruleResult.isOtp,
            otpCode = ruleResult.otpCode,
            isFinancial = ruleResult.isFinancial,
            isPromotional = ruleResult.isPromotional,
            firstSeenAt = now,
            lastUpdatedAt = now,
            removedAt = null,
            durationMs = 0L,
            updateCount = 1,
            wasBlocked = ruleResult.shouldBlock,
            blockReason = ruleResult.reason,
            responsibleRuleId = ruleResult.ruleId,
            matchingRuleName = ruleResult.ruleName,
            isRead = false
        )
        val eventId = notificationDao.insertEvent(newEvent)
        activeEvents[key] = eventId
        lastSnapshotTime[key] = now
        return eventId
    }

    suspend fun processRemovedNotification(key: String): Long? {
        val activeEventId = activeEvents.remove(key)
        lastSnapshotTime.remove(key)
        val now = System.currentTimeMillis()

        if (activeEventId != null) {
            val event = notificationDao.getEventById(activeEventId)
            if (event != null) {
                val duration = (now - event.firstSeenAt).coerceAtLeast(0L)
                val updated = event.copy(
                    removedAt = now,
                    durationMs = duration
                )
                notificationDao.updateEvent(updated)
                return duration
            }
        }
        return null
    }

    fun clearActiveKeys() {
        activeEvents.clear()
        lastSnapshotTime.clear()
    }
}
