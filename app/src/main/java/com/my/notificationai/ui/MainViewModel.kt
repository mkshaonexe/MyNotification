package com.my.notificationai.ui

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.my.notificationai.data.AppRepository
import com.my.notificationai.data.BlockedApp
import com.my.notificationai.data.SavedNotification
import com.my.notificationai.service.MyNotificationListenerService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AppItem(
    val packageName: String,
    val appLabel: String,
    val isBlocked: Boolean,
    val notificationCount: Int
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: AppRepository,
    @param:ApplicationContext private val context: Context
) : ViewModel() {

    // Theme and block settings
    val isBlockAllEnabled: StateFlow<Boolean> = repository.isBlockAllEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val quickPauseUntil: StateFlow<Long> = repository.quickPauseUntil
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val themePreference: StateFlow<String> = repository.themePreference
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "LIGHT")

    val savedNotifications: StateFlow<List<SavedNotification>> = repository.allNotifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val dbBlockedApps = repository.allBlockedApps
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Installed apps from package manager
    private val _rawInstalledApps = MutableStateFlow<List<Pair<String, String>>>(emptyList())

    // Search query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    // Combine raw installed apps, db block settings, search query, and notification count
    val appsList: StateFlow<List<AppItem>> = combine(
        _rawInstalledApps,
        dbBlockedApps,
        savedNotifications,
        _searchQuery
    ) { rawApps, dbBlocked, notifications, query ->
        val blockedMap = dbBlocked.associateBy { it.packageName }
        val countMap = notifications.groupBy { it.packageName }.mapValues { it.value.size }

        rawApps
            .map { (label, pkg) ->
                val isBlocked = blockedMap[pkg]?.isBlocked ?: false
                val count = countMap[pkg] ?: 0
                AppItem(packageName = pkg, appLabel = label, isBlocked = isBlocked, notificationCount = count)
            }
            .filter {
                query.isEmpty() || it.appLabel.contains(query, ignoreCase = true) || it.packageName.contains(query, ignoreCase = true)
            }
            .sortedWith(compareByDescending<AppItem> { it.notificationCount }.thenBy { it.appLabel })
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadInstalledApps()
    }

    fun loadInstalledApps() {
        viewModelScope.launch(Dispatchers.Default) {
            val pm = context.packageManager
            val packages = pm.getInstalledPackages(0)
            val appList = packages.mapNotNull { packageInfo ->
                val appInfo = packageInfo.applicationInfo ?: return@mapNotNull null
                // Filter out system apps by default, except system dialer/messages
                val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                if (isSystem) {
                    val isDialerOrSms = packageInfo.packageName.contains("dialer") ||
                            packageInfo.packageName.contains("messaging") ||
                            packageInfo.packageName.contains("phone")
                    if (!isDialerOrSms) return@mapNotNull null
                }
                // Skip our own app
                if (packageInfo.packageName == context.packageName) return@mapNotNull null

                val label = pm.getApplicationLabel(appInfo).toString()
                label to packageInfo.packageName
            }.sortedBy { it.first }
            _rawInstalledApps.value = appList
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // Toggle service block all
    fun toggleBlockAll(enabled: Boolean) {
        viewModelScope.launch {
            repository.setBlockAllEnabled(enabled)
        }
    }

    // Toggle block status for a specific app
    fun toggleAppBlock(packageName: String, appLabel: String, currentBlocked: Boolean) {
        viewModelScope.launch {
            val app = repository.getBlockedApp(packageName)
            if (app == null) {
                repository.insertBlockedApp(
                    BlockedApp(
                        packageName = packageName,
                        appLabel = appLabel,
                        isBlocked = !currentBlocked
                    )
                )
            } else {
                repository.updateBlockedApp(
                    app.copy(isBlocked = !currentBlocked, updatedAt = System.currentTimeMillis())
                )
            }
        }
    }

    // Quick pause logic (e.g. pause for 15 mins, 1 hour, etc.)
    fun enableQuickPause(minutes: Int) {
        viewModelScope.launch {
            val untilTimestamp = System.currentTimeMillis() + (minutes * 60 * 1000L)
            repository.setQuickPauseUntil(untilTimestamp)
        }
    }

    fun cancelQuickPause() {
        viewModelScope.launch {
            repository.setQuickPauseUntil(0L)
        }
    }

    // Notifications operations
    fun markNotificationAsRead(notification: SavedNotification) {
        viewModelScope.launch {
            repository.updateNotification(notification.copy(isRead = true))
        }
    }

    fun deleteNotification(notification: SavedNotification) {
        viewModelScope.launch {
            repository.deleteNotification(notification)
        }
    }

    fun deleteNotificationById(id: Int) {
        viewModelScope.launch {
            repository.deleteNotificationById(id)
        }
    }

    fun clearAllNotifications() {
        viewModelScope.launch {
            repository.deleteAllNotifications()
        }
    }

    // Settings theme setting
    fun setTheme(theme: String) {
        viewModelScope.launch {
            repository.setThemePreference(theme)
        }
    }

    // Service status helper
    fun isNotificationAccessGranted(): Boolean {
        val enabledListeners = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
        return enabledListeners != null && enabledListeners.contains(context.packageName)
    }

    fun isServiceRunning(): Boolean {
        return MyNotificationListenerService.isServiceRunning && isNotificationAccessGranted()
    }
}
