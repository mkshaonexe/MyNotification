package com.my.notificationai.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.my.notificationai.core.database.entities.NotificationEvent
import com.my.notificationai.core.database.entities.NotificationUpdate
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {

    // --- Events CRUD ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: NotificationEvent): Long

    @Update
    suspend fun updateEvent(event: NotificationEvent)

    @Delete
    suspend fun deleteEvent(event: NotificationEvent)

    @Query("DELETE FROM notification_events WHERE id = :id")
    suspend fun deleteEventById(id: Long)

    @Query("DELETE FROM notification_events")
    suspend fun deleteAllEvents()

    @Query("SELECT * FROM notification_events WHERE id = :id LIMIT 1")
    suspend fun getEventById(id: Long): NotificationEvent?

    @Query("SELECT * FROM notification_events WHERE id = :id LIMIT 1")
    fun getEventByIdFlow(id: Long): Flow<NotificationEvent?>

    @Query("SELECT * FROM notification_events WHERE notification_key = :key LIMIT 1")
    suspend fun getEventByKey(key: String): NotificationEvent?

    @Query("SELECT * FROM notification_events WHERE notification_key = :key AND removed_at IS NULL ORDER BY id DESC LIMIT 1")
    suspend fun getActiveEventByKey(key: String): NotificationEvent?

    @Query("SELECT * FROM notification_events ORDER BY last_updated_at DESC")
    fun getAllEventsFlow(): Flow<List<NotificationEvent>>

    @Query("""
        SELECT * FROM notification_events 
        WHERE (:query = '' OR app_label LIKE '%' || :query || '%' OR latest_title LIKE '%' || :query || '%' OR latest_text LIKE '%' || :query || '%')
          AND (
            :filter = 'ALL' 
            OR (:filter = 'ALLOWED' AND was_blocked = 0)
            OR (:filter = 'BLOCKED' AND was_blocked = 1)
            OR (:filter = 'IMPORTANT' AND (is_otp = 1 OR is_financial = 1))
          )
        ORDER BY last_updated_at DESC
    """)
    fun getFilteredEventsFlow(query: String, filter: String): Flow<List<NotificationEvent>>

    // --- Analytics Aggregations ---

    @Query("SELECT COUNT(*) FROM notification_events")
    fun getTotalEventsCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM notification_events WHERE was_blocked = 1")
    fun getBlockedEventsCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM notification_events WHERE was_blocked = 0")
    fun getAllowedEventsCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM notification_events WHERE is_otp = 1 OR is_financial = 1")
    fun getImportantEventsCount(): Flow<Int>

    @Query("SELECT * FROM notification_events WHERE last_updated_at >= :startTime AND last_updated_at <= :endTime ORDER BY last_updated_at DESC")
    fun getEventsBetween(startTime: Long, endTime: Long): Flow<List<NotificationEvent>>

    @Query("SELECT COUNT(*) FROM notification_events WHERE last_updated_at >= :startTime AND last_updated_at <= :endTime")
    fun getEventsCountBetween(startTime: Long, endTime: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM notification_events WHERE was_blocked = 1 AND last_updated_at >= :startTime AND last_updated_at <= :endTime")
    fun getBlockedEventsCountBetween(startTime: Long, endTime: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM notification_events WHERE was_blocked = 0 AND last_updated_at >= :startTime AND last_updated_at <= :endTime")
    fun getAllowedEventsCountBetween(startTime: Long, endTime: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM notification_events WHERE (is_otp = 1 OR is_financial = 1) AND last_updated_at >= :startTime AND last_updated_at <= :endTime")
    fun getImportantEventsCountBetween(startTime: Long, endTime: Long): Flow<Int>

    @Query("""
        SELECT package_name, app_label, COUNT(*) AS total_count, 
               SUM(CASE WHEN was_blocked = 1 THEN 1 ELSE 0 END) AS blocked_count 
        FROM notification_events 
        WHERE last_updated_at >= :startTime AND last_updated_at <= :endTime 
        GROUP BY package_name 
        ORDER BY total_count DESC 
        LIMIT :limit
    """)
    fun getTopAppsBetween(startTime: Long, endTime: Long, limit: Int = 10): Flow<List<AppNotificationCount>>

    @Query("DELETE FROM notification_events WHERE last_updated_at < :cutoffTimestamp")
    suspend fun deleteEventsOlderThan(cutoffTimestamp: Long): Int

    @Query("UPDATE notification_events SET is_read = 1 WHERE id = :id")
    suspend fun markEventAsRead(id: Long)

    @Query("UPDATE notification_events SET is_read = 1")
    suspend fun markAllEventsAsRead()

    @Query("UPDATE notification_events SET block_reason = :blockReason WHERE id = :id")
    suspend fun updateBlockReason(id: Long, blockReason: String)

    // --- Updates CRUD ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUpdate(update: NotificationUpdate): Long

    @Query("SELECT * FROM notification_updates WHERE event_id = :eventId ORDER BY timestamp ASC")
    fun getUpdatesForEvent(eventId: Long): Flow<List<NotificationUpdate>>

    @Query("SELECT * FROM notification_updates WHERE event_id = :eventId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestUpdateForEvent(eventId: Long): NotificationUpdate?

    @Query("DELETE FROM notification_updates WHERE event_id = :eventId")
    suspend fun deleteUpdatesForEvent(eventId: Long)
}
