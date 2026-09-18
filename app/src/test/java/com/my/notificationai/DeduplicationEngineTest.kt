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

    private lateinit var dao: FakeNotificationDao
    private lateinit var deduplicationEngine: NotificationDeduplicationEngine

    @Before
    fun setup() {
        dao = FakeNotificationDao()
        deduplicationEngine = NotificationDeduplicationEngine(dao)
    }

    @Test
    fun `initial notification creates new logical event with updateCount 1`() = runBlocking {
        val ruleResult = RuleEvaluationResult(shouldBlock = false, reason = "Allowed")
        val eventId = deduplicationEngine.processPostedNotification(
            key = "0|com.example.download|1|null|1000",
            packageName = "com.example.download",
            appLabel = "Downloader",
            title = "Downloading file.zip",
            text = "0% downloaded",
            progress = 0,
            maxProgress = 100,
            ruleResult = ruleResult,
            currentTimeMillis = 1000L
        )

        val saved = dao.getEventById(eventId)
        assertNotNull(saved)
        assertEquals(1, saved?.updateCount)
        assertEquals("Downloading file.zip", saved?.initialTitle)
        assertEquals("0% downloaded", saved?.initialText)
        assertEquals(1000L, saved?.firstSeenAt)
        assertEquals(1000L, saved?.lastUpdatedAt)
    }

    @Test
    fun `continuous updates with same key merge into single event and increment updateCount`() = runBlocking {
        val key = "0|com.example.download|1|null|1000"
        val ruleResult = RuleEvaluationResult(shouldBlock = false, reason = "Allowed")

        val id1 = deduplicationEngine.processPostedNotification(
            key = key,
            packageName = "com.example.download",
            appLabel = "Downloader",
            title = "Downloading file.zip",
            text = "10% downloaded",
            progress = 10,
            maxProgress = 100,
            ruleResult = ruleResult,
            currentTimeMillis = 1000L
        )

        val id2 = deduplicationEngine.processPostedNotification(
            key = key,
            packageName = "com.example.download",
            appLabel = "Downloader",
            title = "Downloading file.zip",
            text = "45% downloaded",
            progress = 45,
            maxProgress = 100,
            ruleResult = ruleResult,
            currentTimeMillis = 1600L
        )

        assertEquals(id1, id2)
        assertEquals(1, dao.events.size)

        val saved = dao.getEventById(id1)
        assertEquals(2, saved?.updateCount)
        assertEquals("45% downloaded", saved?.latestText)
        assertEquals("10% downloaded", saved?.initialText)
        assertEquals(1000L, saved?.firstSeenAt)
        assertEquals(1600L, saved?.lastUpdatedAt)
    }

    @Test
    fun `rapid snapshots under 500ms are throttled to eliminate SQLite lock contention`() = runBlocking {
        val key = "0|com.example.timer|1|null|2000"
        val ruleResult = RuleEvaluationResult(shouldBlock = false, reason = "Allowed")

        deduplicationEngine.processPostedNotification(
            key = key,
            packageName = "com.example.timer",
            appLabel = "Timer",
            title = "Timer",
            text = "00:01",
            ruleResult = ruleResult,
            currentTimeMillis = 1000L
        )

        // Rapid updates within 500ms
        deduplicationEngine.processPostedNotification(
            key = key,
            packageName = "com.example.timer",
            appLabel = "Timer",
            title = "Timer",
            text = "00:02",
            ruleResult = ruleResult,
            currentTimeMillis = 1100L // 100ms later
        )
        deduplicationEngine.processPostedNotification(
            key = key,
            packageName = "com.example.timer",
            appLabel = "Timer",
            title = "Timer",
            text = "00:03",
            ruleResult = ruleResult,
            currentTimeMillis = 1200L // 200ms later
        )

        // Snapshot should not be inserted yet (under 500ms)
        assertEquals(0, dao.updates.size)

        // Update at 600ms later (total 1600L > 1000L + 500L)
        deduplicationEngine.processPostedNotification(
            key = key,
            packageName = "com.example.timer",
            appLabel = "Timer",
            title = "Timer",
            text = "00:04",
            ruleResult = ruleResult,
            currentTimeMillis = 1600L
        )

        assertEquals(1, dao.updates.size)
        assertEquals("00:04", dao.updates.first().text)
    }

    @Test
    fun `notification removal records duration and removed timestamp`() = runBlocking {
        val key = "0|com.google.android.dialer|1|null|3000"
        val ruleResult = RuleEvaluationResult(shouldBlock = false, reason = "Call")

        val eventId = deduplicationEngine.processPostedNotification(
            key = key,
            packageName = "com.google.android.dialer",
            appLabel = "Phone",
            title = "Ongoing Call",
            text = "02:14",
            ruleResult = ruleResult,
            currentTimeMillis = 5000L
        )

        val duration = deduplicationEngine.processRemovedNotification(
            key = key,
            currentTimeMillis = 12000L // 7 seconds later
        )

        assertEquals(7000L, duration)
        val saved = dao.getEventById(eventId)
        assertEquals(7000L, saved?.durationMs)
        assertEquals(12000L, saved?.removedAt)
    }

    @Test
    fun `distinct keys produce independent events without collision`() = runBlocking {
        val ruleResult = RuleEvaluationResult(shouldBlock = false, reason = "Allowed")

        val id1 = deduplicationEngine.processPostedNotification(
            key = "key_whatsapp_1",
            packageName = "com.whatsapp",
            appLabel = "WhatsApp",
            title = "Alice",
            text = "Hello!",
            ruleResult = ruleResult
        )

        val id2 = deduplicationEngine.processPostedNotification(
            key = "key_telegram_1",
            packageName = "org.telegram.messenger",
            appLabel = "Telegram",
            title = "Bob",
            text = "Hey there!",
            ruleResult = ruleResult
        )

        assertTrue(id1 != id2)
        assertEquals(2, dao.events.size)
    }
}
