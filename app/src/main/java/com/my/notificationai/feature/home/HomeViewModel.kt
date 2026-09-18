package com.my.notificationai.feature.home

import android.content.Context
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.my.notificationai.data.AppRepository
import com.my.notificationai.service.MyNotificationListenerService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: AppRepository,
    @param:ApplicationContext private val context: Context
) : ViewModel() {

    val isMasterBlockerEnabled: StateFlow<Boolean> = repository.isMasterBlockerEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val blockedCount: StateFlow<Int> = repository.blockedEventsCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val allowedCount: StateFlow<Int> = repository.allowedEventsCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val importantCount: StateFlow<Int> = repository.importantEventsCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val quickPauseUntil: StateFlow<Long> = repository.quickPauseUntil
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    fun toggleMasterBlocker() {
        viewModelScope.launch {
            val current = isMasterBlockerEnabled.value
            repository.setMasterBlockerEnabled(!current)
        }
    }

    fun setQuickPause(minutes: Int) {
        viewModelScope.launch {
            val until = System.currentTimeMillis() + (minutes * 60 * 1000L)
            repository.setQuickPauseUntil(until)
        }
    }

    fun cancelQuickPause() {
        viewModelScope.launch {
            repository.setQuickPauseUntil(0L)
        }
    }

    fun isNotificationAccessGranted(): Boolean {
        val enabledListeners = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
        return enabledListeners != null && enabledListeners.contains(context.packageName)
    }

    fun isServiceRunning(): Boolean {
        return MyNotificationListenerService.isServiceRunning && isNotificationAccessGranted()
    }
}
