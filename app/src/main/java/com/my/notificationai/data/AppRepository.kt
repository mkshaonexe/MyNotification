package com.my.notificationai.data

import kotlinx.coroutines.flow.Flow

class AppRepository(
    private val notificationDao: NotificationDao,
    private val settingsDataStore: SettingsDataStore
) {
    // Blocked Apps
    val allBlockedApps: Flow<List<BlockedApp>> = notificationDao.getAllBlockedApps()
    
    suspend fun getBlockedApp(packageName: String): BlockedApp? {
        return notificationDao.getBlockedApp(packageName)
    }
    
    suspend fun insertBlockedApp(app: BlockedApp) {
        notificationDao.insertBlockedApp(app)
    }

    suspend fun insertBlockedApps(apps: List<BlockedApp>) {
        notificationDao.insertBlockedApps(apps)
    }

    suspend fun updateBlockedApp(app: BlockedApp) {
        notificationDao.updateBlockedApp(app)
    }

    suspend fun deleteBlockedApp(app: BlockedApp) {
        notificationDao.deleteBlockedApp(app)
    }

    // Saved Notifications
    val allNotifications: Flow<List<SavedNotification>> = notificationDao.getAllNotifications()

    suspend fun insertNotification(notification: SavedNotification) {
        notificationDao.insertNotification(notification)
    }

    suspend fun updateNotification(notification: SavedNotification) {
        notificationDao.updateNotification(notification)
    }

    suspend fun deleteNotification(notification: SavedNotification) {
        notificationDao.deleteNotification(notification)
    }

    suspend fun deleteNotificationById(id: Int) {
        notificationDao.deleteNotificationById(id)
    }

    suspend fun deleteAllNotifications() {
        notificationDao.deleteAllNotifications()
    }

    // DataStore Settings
    val isBlockAllEnabled: Flow<Boolean> = settingsDataStore.isBlockAllEnabled
    val quickPauseUntil: Flow<Long> = settingsDataStore.quickPauseUntil
    val themePreference: Flow<String> = settingsDataStore.themePreference

    suspend fun setBlockAllEnabled(enabled: Boolean) {
        settingsDataStore.setBlockAllEnabled(enabled)
    }

    suspend fun setQuickPauseUntil(timestamp: Long) {
        settingsDataStore.setQuickPauseUntil(timestamp)
    }

    suspend fun setThemePreference(theme: String) {
        settingsDataStore.setThemePreference(theme)
    }
}
