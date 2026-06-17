package com.my.notificationai.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {

    // Blocked Apps
    @Query("SELECT * FROM blocked_apps ORDER BY app_label ASC")
    fun getAllBlockedApps(): Flow<List<BlockedApp>>

    @Query("SELECT * FROM blocked_apps WHERE package_name = :packageName LIMIT 1")
    suspend fun getBlockedApp(packageName: String): BlockedApp?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlockedApp(app: BlockedApp)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlockedApps(apps: List<BlockedApp>)

    @Update
    suspend fun updateBlockedApp(app: BlockedApp)

    @Delete
    suspend fun deleteBlockedApp(app: BlockedApp)

    // Saved Notifications
    @Query("SELECT * FROM saved_notifications WHERE deleted_at IS NULL ORDER BY received_at DESC")
    fun getAllNotifications(): Flow<List<SavedNotification>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: SavedNotification)

    @Update
    suspend fun updateNotification(notification: SavedNotification)

    @Delete
    suspend fun deleteNotification(notification: SavedNotification)

    @Query("DELETE FROM saved_notifications WHERE id = :id")
    suspend fun deleteNotificationById(id: Int)

    @Query("DELETE FROM saved_notifications")
    suspend fun deleteAllNotifications()
}
