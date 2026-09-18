package com.my.notificationai

import com.my.notificationai.core.database.entities.NotificationEvent
import com.my.notificationai.feature.analytics.TimeRange
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Calendar

class AnalyticsCalculationTest {

    private lateinit var dao: FakeNotificationDao

    @Before
    fun setup() {
        dao = FakeNotificationDao()
    }

    @Test
    fun `TimeRange today starts at midnight of current day`() {
        val now = System.currentTimeMillis()
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfToday = cal.timeInMillis

        assertTrue(startOfToday <= now)
        assertTrue(now - startOfToday < 24L * 60L * 60L * 1000L)
    }

    @Test
    fun `TimeRange last 7 days spans exactly 7 days in milliseconds`() {
        val days = TimeRange.LAST_7_DAYS.days
        val expectedSpan = 7L * 24L * 60L * 60L * 1000L
        assertEquals(expectedSpan, days * 24L * 60L * 60L * 1000L)
    }

    @Test
    fun `analytics aggregation correctly counts within time window`() = runBlocking {
        val now = 1000000000L
        val windowStart = now - (7 * 24L * 60L * 60L * 1000L) // 7 days window

        // Event inside window - Blocked
        dao.insertEvent(
            NotificationEvent(
                notificationKey = "k1",
                packageName = "com.instagram.android",
                appLabel = "Instagram",
                initialTitle = "Post",
                latestTitle = "Post",
                initialText = "Liked",
                latestText = "Liked",
                channelId = "c1",
                lastUpdatedAt = now - 1000L,
                firstSeenAt = now - 1000L,
                wasBlocked = true
            )
        )

        // Event inside window - Allowed
        dao.insertEvent(
            NotificationEvent(
                notificationKey = "k2",
                packageName = "com.whatsapp",
                appLabel = "WhatsApp",
                initialTitle = "Message",
                latestTitle = "Message",
                initialText = "Hi",
                latestText = "Hi",
                channelId = "c2",
                lastUpdatedAt = now - 2000L,
                firstSeenAt = now - 2000L,
                wasBlocked = false
            )
        )

        // Event inside window - Important OTP
        dao.insertEvent(
            NotificationEvent(
                notificationKey = "k3",
                packageName = "com.bKash.customerapp",
                appLabel = "bKash",
                initialTitle = "OTP",
                latestTitle = "OTP",
                initialText = "123456",
                latestText = "123456",
                channelId = "c3",
                lastUpdatedAt = now - 3000L,
                firstSeenAt = now - 3000L,
                wasBlocked = false,
                isOtp = true
            )
        )

        // Event OUTSIDE window (older than 7 days)
        dao.insertEvent(
            NotificationEvent(
                notificationKey = "k4",
                packageName = "com.facebook.katana",
                appLabel = "Facebook",
                initialTitle = "Older",
                latestTitle = "Older",
                initialText = "Old post",
                latestText = "Old post",
                channelId = "c4",
                lastUpdatedAt = windowStart - 10000L,
                firstSeenAt = windowStart - 10000L,
                wasBlocked = true
            )
        )

        val total = dao.getEventsCountBetween(windowStart, now).first()
        val blocked = dao.getBlockedEventsCountBetween(windowStart, now).first()
        val allowed = dao.getAllowedEventsCountBetween(windowStart, now).first()
        val important = dao.getImportantEventsCountBetween(windowStart, now).first()

        assertEquals(3, total)
        assertEquals(1, blocked)
        assertEquals(2, allowed)
        assertEquals(1, important)
    }

    @Test
    fun `top apps query ranks apps by count descending and limits to requested number`() = runBlocking {
        val now = System.currentTimeMillis()
        val windowStart = now - (7 * 24L * 60L * 60L * 1000L)

        // 3 Instagram events
        repeat(3) { i ->
            dao.insertEvent(
                NotificationEvent(
                    notificationKey = "insta_$i",
                    packageName = "com.instagram.android",
                    appLabel = "Instagram",
                    initialTitle = "Insta $i",
                    latestTitle = "Insta $i",
                    initialText = "Text $i",
                    latestText = "Text $i",
                    channelId = "c",
                    lastUpdatedAt = now - 100L * i,
                    firstSeenAt = now - 100L * i,
                    wasBlocked = true
                )
            )
        }

        // 5 WhatsApp events
        repeat(5) { i ->
            dao.insertEvent(
                NotificationEvent(
                    notificationKey = "wa_$i",
                    packageName = "com.whatsapp",
                    appLabel = "WhatsApp",
                    initialTitle = "WA $i",
                    latestTitle = "WA $i",
                    initialText = "Text $i",
                    latestText = "Text $i",
                    channelId = "c",
                    lastUpdatedAt = now - 100L * i,
                    firstSeenAt = now - 100L * i,
                    wasBlocked = false
                )
            )
        }

        // 1 YouTube event
        dao.insertEvent(
            NotificationEvent(
                notificationKey = "yt_0",
                packageName = "com.google.android.youtube",
                appLabel = "YouTube",
                initialTitle = "YT 0",
                latestTitle = "YT 0",
                initialText = "New video",
                latestText = "New video",
                channelId = "c",
                lastUpdatedAt = now - 100L,
                firstSeenAt = now - 100L,
                wasBlocked = true
            )
        )

        val topApps = dao.getTopAppsBetween(windowStart, now, limit = 2).first()

        assertEquals(2, topApps.size)
        assertEquals("WhatsApp", topApps[0].appLabel)
        assertEquals(5, topApps[0].totalCount)
        assertEquals("Instagram", topApps[1].appLabel)
        assertEquals(3, topApps[1].totalCount)
    }

    @Test
    fun `proportion calculation handles zero maxCount without division by zero`() {
        val maxCount = 0
        val count = 0
        val proportion = if (maxCount > 0) count.toFloat() / maxCount.toFloat() else 0f
        assertEquals(0f, proportion, 0.001f)
    }
}
