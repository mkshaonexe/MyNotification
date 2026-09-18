package com.my.notificationai

import com.my.notificationai.core.database.entities.NotificationEvent
import com.my.notificationai.core.domain.engine.NotificationDeduplicationEngine
import com.my.notificationai.core.domain.models.RuleEvaluationResult
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class AnalyticsCalculationTest {

    private lateinit var notificationDao: FakeNotificationDao
    private lateinit var deduplicationEngine: NotificationDeduplicationEngine

    @Before
    fun setup() {
        notificationDao = FakeNotificationDao()
        deduplicationEngine = NotificationDeduplicationEngine(notificationDao)
    }

    @Test
    fun `high-frequency progress updates do not inflate analytics counts`() = runBlocking {
        val downloadKey = "0|com.example.download|100|null|10001"

        // Simulate 100 updates of a single downloading file
        for (p in 1..100) {
            deduplicationEngine.processPostedNotification(
                key = downloadKey,
                packageName = "com.example.download",
                appLabel = "Downloader",
                title = "Downloading file.zip",
                text = "$p% downloaded",
                isOngoing = p < 100,
                progress = p,
                maxProgress = 100,
                ruleResult = RuleEvaluationResult(shouldBlock = false, reason = "Allowed"),
                currentTimeMillis = 1000L + (p * 50L)
            )
        }

        // Check total events count
        val totalCount = notificationDao.getTotalEventsCount().first()
        val allowedCount = notificationDao.getAllowedEventsCount().first()
        val blockedCount = notificationDao.getBlockedEventsCount().first()

        assertEquals("Total count must be 1 logical event, not 100", 1, totalCount)
        assertEquals(1, allowedCount)
        assertEquals(0, blockedCount)
    }

    @Test
    fun `time window aggregation accurately filters events`() = runBlocking {
        val now = 1_000_000L
        val oneHourAgo = now - 3600_000L
        val twoHoursAgo = now - 7200_000L
        val threeHoursAgo = now - 10800_000L

        // Insert events across time
        notificationDao.insertEvent(
            NotificationEvent(
                id = 1, notificationKey = "k1", packageName = "com.app1", appLabel = "App 1",
                initialTitle = "T1", latestTitle = "T1", initialText = "M1", latestText = "M1",
                channelId = "c1", wasBlocked = true, isOtp = false, isFinancial = false,
                firstSeenAt = threeHoursAgo, lastUpdatedAt = threeHoursAgo
            )
        )
        notificationDao.insertEvent(
            NotificationEvent(
                id = 2, notificationKey = "k2", packageName = "com.app2", appLabel = "App 2",
                initialTitle = "T2", latestTitle = "T2", initialText = "M2", latestText = "M2",
                channelId = "c1", wasBlocked = false, isOtp = true, isFinancial = false,
                firstSeenAt = oneHourAgo, lastUpdatedAt = oneHourAgo
            )
        )
        notificationDao.insertEvent(
            NotificationEvent(
                id = 3, notificationKey = "k3", packageName = "com.app2", appLabel = "App 2",
                initialTitle = "T3", latestTitle = "T3", initialText = "M3", latestText = "M3",
                channelId = "c1", wasBlocked = true, isOtp = false, isFinancial = false,
                firstSeenAt = now, lastUpdatedAt = now
            )
        )

        // Query last 2 hours (should include event 2 and 3, but exclude event 1)
        val startTime = twoHoursAgo
        val endTime = now + 1000L

        val countInWindow = notificationDao.getEventsCountBetween(startTime, endTime).first()
        val blockedInWindow = notificationDao.getBlockedEventsCountBetween(startTime, endTime).first()
        val allowedInWindow = notificationDao.getAllowedEventsCountBetween(startTime, endTime).first()
        val importantInWindow = notificationDao.getImportantEventsCountBetween(startTime, endTime).first()

        assertEquals(2, countInWindow)
        assertEquals(1, blockedInWindow)
        assertEquals(1, allowedInWindow)
        assertEquals(1, importantInWindow)
    }

    @Test
    fun `top apps aggregation groups and ranks correctly`() = runBlocking {
        val now = System.currentTimeMillis()

        // 3 events for Instagram (2 blocked, 1 allowed)
        for (i in 1..3) {
            notificationDao.insertEvent(
                NotificationEvent(
                    id = 0, notificationKey = "insta_$i", packageName = "com.instagram.android", appLabel = "Instagram",
                    initialTitle = "Post", latestTitle = "Post", initialText = "Msg", latestText = "Msg",
                    channelId = "c", wasBlocked = i <= 2, isOtp = false, isFinancial = false,
                    firstSeenAt = now, lastUpdatedAt = now
                )
            )
        }

        // 5 events for WhatsApp (0 blocked, 5 allowed)
        for (i in 1..5) {
            notificationDao.insertEvent(
                NotificationEvent(
                    id = 0, notificationKey = "wa_$i", packageName = "com.whatsapp", appLabel = "WhatsApp",
                    initialTitle = "Chat", latestTitle = "Chat", initialText = "Msg", latestText = "Msg",
                    channelId = "c", wasBlocked = false, isOtp = false, isFinancial = false,
                    firstSeenAt = now, lastUpdatedAt = now
                )
            )
        }

        // 1 event for YouTube (1 blocked)
        notificationDao.insertEvent(
            NotificationEvent(
                id = 0, notificationKey = "yt_1", packageName = "com.google.android.youtube", appLabel = "YouTube",
                initialTitle = "Video", latestTitle = "Video", initialText = "Msg", latestText = "Msg",
                channelId = "c", wasBlocked = true, isOtp = false, isFinancial = false,
                firstSeenAt = now, lastUpdatedAt = now
            )
        )

        val topApps = notificationDao.getTopAppsBetween(now - 10000L, now + 10000L, limit = 5).first()

        assertEquals(3, topApps.size)
        // 1st: WhatsApp (5 total, 0 blocked)
        assertEquals("com.whatsapp", topApps[0].packageName)
        assertEquals(5, topApps[0].totalCount)
        assertEquals(0, topApps[0].blockedCount)

        // 2nd: Instagram (3 total, 2 blocked)
        assertEquals("com.instagram.android", topApps[1].packageName)
        assertEquals(3, topApps[1].totalCount)
        assertEquals(2, topApps[1].blockedCount)

        // 3rd: YouTube (1 total, 1 blocked)
        assertEquals("com.google.android.youtube", topApps[2].packageName)
        assertEquals(1, topApps[2].totalCount)
        assertEquals(1, topApps[2].blockedCount)
    }

    @Test
    fun `retention purging correctly deletes records older than cutoff`() = runBlocking {
        val now = 1_000_000_000L
        val thirtyDaysAgo = now - (30L * 24 * 60 * 60 * 1000)
        val ninetyDaysAgo = now - (90L * 24 * 60 * 60 * 1000)
        val hundredDaysAgo = now - (100L * 24 * 60 * 60 * 1000)

        // Insert events
        notificationDao.insertEvent(
            NotificationEvent(
                id = 1, notificationKey = "old_1", packageName = "com.app", appLabel = "App",
                initialTitle = "Old", latestTitle = "Old", initialText = "M", latestText = "M",
                channelId = "c", firstSeenAt = hundredDaysAgo, lastUpdatedAt = hundredDaysAgo
            )
        )
        notificationDao.insertEvent(
            NotificationEvent(
                id = 2, notificationKey = "recent_1", packageName = "com.app", appLabel = "App",
                initialTitle = "Recent", latestTitle = "Recent", initialText = "M", latestText = "M",
                channelId = "c", firstSeenAt = thirtyDaysAgo, lastUpdatedAt = thirtyDaysAgo
            )
        )
        notificationDao.insertEvent(
            NotificationEvent(
                id = 3, notificationKey = "recent_2", packageName = "com.app", appLabel = "App",
                initialTitle = "Now", latestTitle = "Now", initialText = "M", latestText = "M",
                channelId = "c", firstSeenAt = now, lastUpdatedAt = now
            )
        )

        assertEquals(3, notificationDao.events.size)

        // Purge records older than 90 days
        val deletedCount = notificationDao.deleteEventsOlderThan(ninetyDaysAgo)

        assertEquals(1, deletedCount)
        assertEquals(2, notificationDao.events.size)
        assertEquals(listOf(2L, 3L), notificationDao.events.map { it.id })
    }
}
