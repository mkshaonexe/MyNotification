package com.my.notificationai.feature.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.my.notificationai.core.domain.engine.BackupManager
import com.my.notificationai.core.domain.engine.RetentionManager
import com.my.notificationai.data.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: AppRepository,
    private val retentionManager: RetentionManager,
    private val backupManager: BackupManager,
    @param:ApplicationContext private val context: Context
) : ViewModel() {

    val themePreference: StateFlow<String> = repository.themePreference
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "DARK")

    val dataRetentionDays: StateFlow<Int> = repository.dataRetentionDays
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 90)

    val isOtpProtectionEnabled: StateFlow<Boolean> = repository.isOtpProtectionEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val isFinancialProtectionEnabled: StateFlow<Boolean> = repository.isFinancialProtectionEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val isPromotionalSmsFilterEnabled: StateFlow<Boolean> = repository.isPromotionalSmsFilterEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val isEmergencyBypassEnabled: StateFlow<Boolean> = repository.isEmergencyBypassEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    private val _storageSizeMb = MutableStateFlow(0.0)
    val storageSizeMb: StateFlow<Double> = _storageSizeMb

    init {
        updateStorageSize()
    }

    fun updateStorageSize() {
        val bytes = retentionManager.getDatabaseSizeBytes()
        _storageSizeMb.value = (bytes.toDouble() / (1024 * 1024)).coerceAtLeast(0.05)
    }

    fun setTheme(theme: String) {
        viewModelScope.launch {
            repository.setThemePreference(theme)
        }
    }

    fun setRetentionDays(days: Int) {
        viewModelScope.launch {
            repository.setDataRetentionDays(days)
            retentionManager.purgeExpiredRecords()
            updateStorageSize()
        }
    }

    fun toggleOtpProtection(enabled: Boolean) {
        viewModelScope.launch { repository.setOtpProtectionEnabled(enabled) }
    }

    fun toggleFinancialProtection(enabled: Boolean) {
        viewModelScope.launch { repository.setFinancialProtectionEnabled(enabled) }
    }

    fun togglePromotionalFilter(enabled: Boolean) {
        viewModelScope.launch { repository.setPromotionalSmsFilterEnabled(enabled) }
    }

    fun toggleEmergencyBypass(enabled: Boolean) {
        viewModelScope.launch { repository.setEmergencyBypassEnabled(enabled) }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            retentionManager.clearAllHistory()
            updateStorageSize()
        }
    }

    suspend fun exportBackup(): String = backupManager.exportBackupJson()

    suspend fun importBackup(json: String): Boolean = backupManager.importBackupJson(json)

    fun isNotificationAccessGranted(): Boolean {
        val enabledListeners = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
        return enabledListeners != null && enabledListeners.contains(context.packageName)
    }

    fun isBatteryOptimizationIgnored(): Boolean {
        val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        return pm?.isIgnoringBatteryOptimizations(context.packageName) ?: false
    }

    fun openNotificationListenerSettings() {
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    fun openBatteryOptimizationSettings() {
        val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            val appDetailsIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:${context.packageName}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(appDetailsIntent)
        }
    }
}
