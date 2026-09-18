package com.my.notificationai

import com.my.notificationai.core.domain.engine.NotificationDeduplicationEngine
import com.my.notificationai.core.domain.models.RuleEvaluationResult
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DeduplicationEngineTest {

    private lateinit var notificationDao: FakeNotificationDao
    private lateinit var deduplicationEngine: NotificationDeduplicationEngine

    @Before
    fun setup() {
        notificationDao = FakeNotificationDao()
        deduplicationEngine = NotificationDeduplicationEngine(notificationDao)
    }

    @Test
    fun `initial notification creates new logical event`() = runBlocking {
        val eventId = deduplicationEngine.processPostedNotification(
            key = "0|com.example.download|100|null|10001",
            packageName = "com.example.download",
            appLabel = "Downloader",
            title = "Downloading file.zip",
            text = "0% downloaded",
            isOngoing = true,
            progress = 0,
            maxProgress = 100,
            ruleResult = RuleEvaluationResult(shouldBlock = false, reason = "Allowed")
        )

        assertEquals(1L, eventId)
        assertEquals(1, notificationDao.events.size)
        val event = notificationDao.events.first()
        assertEquals("Downloading file.zip", event.initialTitle)
        assertEquals("0% downloaded", event.initialText)
        assertEquals(1, event.updateCount)
        assertEquals(0, event.progress)
    }

    @Test
    fun `multiple progress updates update single logical event without inflating count`() = runBlocking {
        val key = "0|com.example.download|100|null|10001"

        // 1. Initial post
        val eventId1 = deduplicationEngine.processPostedNotification(
            key = key,
            packageName = "com.example.download",
            appLabel = "Downloader",
            title = "Downloading file.zip",
            text = "0% downloaded",
            isOngoing = true,
            progress = 0,
            maxProgress = 100,
            ruleResult = RuleEvaluationResult(shouldBlock = false, reason = "Allowed"),
            currentTimeMillis = 1000L
        )

        // 2. Update to 25% (200ms later)
        val eventId2 = deduplicationEngine.processPostedNotification(
            key = key,
            packageName = "com.example.download",
            appLabel = "Downloader",
            title = "Downloading file.zip",
            text = "25% downloaded",
            isOngoing = true,
            progress = 25,
            maxProgress = 100,
            ruleResult = RuleEvaluationResult(shouldBlock = false, reason = "Allowed"),
            currentTimeMillis = 1200L
        )

        // 3. Update to 50% (700ms later)
        val eventId3 = deduplicationEngine.processPostedNotification(
            key = key,
            packageName = "com.example.download",
            appLabel = "Downloader",
            title = "Downloading file.zip",
            text = "50% downloaded",
            isOngoing = true,
            progress = 50,
            maxProgress = 100,
            ruleResult = RuleEvaluationResult(shouldBlock = false, reason = "Allowed"),
            currentTimeMillis = 1700L
        )

        // 4. Update to 100% (1500ms later)
        val eventId4 = deduplicationEngine.processPostedNotification(
            key = key,
            packageName = "com.example.download",
            appLabel = "Downloader",
            title = "Download Complete",
            text = "100% downloaded",
            isOngoing = false,
            progress = 100,
            maxProgress = 100,
            ruleResult = RuleEvaluationResult(shouldBlock = false, reason = "Allowed"),
            currentTimeMillis = 2500L
        )

        // All returned the same logical event ID
        assertEquals(eventId1, eventId2)
        assertEquals(eventId1, eventId3)
        assertEquals(eventId1, eventId4)

        // Total events in database MUST BE exactly 1
        assertEquals(1, notificationDao.events.size)

        val event = notificationDao.events.first()
        assertEquals(4, event.updateCount)
        assertEquals("Downloading file.zip", event.initialTitle)
        assertEquals("Download Complete", event.latestTitle)
        assertEquals("100% downloaded", event.latestText)
        assertEquals(100, event.progress)
        assertEquals(1000L, event.firstSeenAt)
        assertEquals(2500L, event.lastUpdatedAt)
    }

    @Test
    fun `removal updates duration and removedAt`() = runBlocking {
        val key = "0|com.example.call|200|null|10002"

        // Call started at 10000ms
        deduplicationEngine.processPostedNotification(
            key = key,
            packageName = "com.android.dialer",
            appLabel = "Phone",
            title = "Ongoing Call",
            text = "0:01",
            isOngoing = true,
            ruleResult = RuleEvaluationResult(shouldBlock = false, reason = "Call Bypass"),
            currentTimeMillis = 10000L
        )

        // Call ended at 45000ms (duration 35 seconds)
        val duration = deduplicationEngine.processRemovedNotification(
            key = key,
            currentTimeMillis = 45000L
        )

        assertEquals(35000L, duration)
        val event = notificationDao.events.first()
        assertNotNull(event.removedAt)
        assertEquals(45000L, event.removedAt)
        assertEquals(35000L, event.durationMs)
    }

    @Test
    fun `posting again after removal creates a new distinct event`() = runBlocking {
        val key = "0|com.whatsapp|300|null|10003"

        // First message at 1000L
        val id1 = deduplicationEngine.processPostedNotification(
            key = key,
            packageName = "com.whatsapp",
            appLabel = "WhatsApp",
            title = "Alice",
            text = "Hello",
            ruleResult = RuleEvaluationResult(shouldBlock = false, reason = "Allowed"),
            currentTimeMillis = 1000L
        )

        // User dismissed notification at 2000L
        deduplicationEngine.processRemovedNotification(key = key, currentTimeMillis = 2000L)

        // Second message with same key arriving hours later at 50000L
        val id2 = deduplicationEngine.processPostedNotification(
            key = key,
            packageName = "com.whatsapp",
            appLabel = "WhatsApp",
            title = "Alice",
            text = "Are you there?",
            ruleResult = RuleEvaluationResult(shouldBlock = false, reason = "Allowed"),
            currentTimeMillis = 50000L
        )

        assertTrue("New event ID must be different after dismissal", id1 != id2)
        assertEquals(2, notificationDao.events.size)
    }

    @Test
    fun `deduplicates across process restart when in-memory cache is lost`() = runBlocking {
        val key = "0|com.spotify.music|400|null|10004"

        // Event posted
        val id1 = deduplicationEngine.processPostedNotification(
            key = key,
            packageName = "com.spotify.music",
            appLabel = "Spotify",
            title = "Song A",
            text = "Artist A",
            isOngoing = true,
            ruleResult = RuleEvaluationResult(shouldBlock = false, reason = "Allowed"),
            currentTimeMillis = 1000L
        )

        // Simulate app process kill / service restart
        deduplicationEngine.clearActiveKeys()

        // New progress update arrives from system
        val id2 = deduplicationEngine.processPostedNotification(
            key = key,
            packageName = "com.spotify.music",
            appLabel = "Spotify",
            title = "Song A",
            text = "Artist A - 1:30",
            isOngoing = true,
            ruleResult = RuleEvaluationResult(shouldBlock = false, reason = "Allowed"),
            currentTimeMillis = 3000L
        )

        assertEquals("Must re-bind to existing active event across restart", id1, id2)
        assertEquals(1, notificationDao.events.size)
        assertEquals(2, notificationDao.events.first().updateCount)
    }
}
