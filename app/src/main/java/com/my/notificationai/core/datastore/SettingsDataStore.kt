package com.my.notificationai.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsDataStore(context: Context) {

    private val dataStore = context.dataStore

    companion object {
        val MASTER_BLOCKER_KEY = booleanPreferencesKey("is_master_blocker_enabled")
        val ACTIVE_BLOCKING_MODE_KEY = stringPreferencesKey("active_blocking_mode") // SOCIAL_MEDIA, SELECTED_APPS, BLOCK_ALL, CUSTOM
        val QUICK_PAUSE_UNTIL_KEY = longPreferencesKey("quick_pause_until")
        val THEME_KEY = stringPreferencesKey("theme_preference") // DARK, LIGHT, SYSTEM
        val OTP_PROTECTION_KEY = booleanPreferencesKey("is_otp_protection_enabled")
        val FINANCIAL_PROTECTION_KEY = booleanPreferencesKey("is_financial_protection_enabled")
        val PROMOTIONAL_SMS_KEY = booleanPreferencesKey("is_promotional_sms_filter_enabled")
        val EMERGENCY_BYPASS_KEY = booleanPreferencesKey("is_emergency_bypass_enabled")
        val DATA_RETENTION_DAYS_KEY = intPreferencesKey("data_retention_days")
        val ONBOARDING_COMPLETED_KEY = booleanPreferencesKey("is_onboarding_completed")
    }

    val isMasterBlockerEnabled: Flow<Boolean> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[MASTER_BLOCKER_KEY] ?: true }

    // Compatibility alias
    val isBlockAllEnabled: Flow<Boolean> = isMasterBlockerEnabled

    val activeBlockingMode: Flow<String> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[ACTIVE_BLOCKING_MODE_KEY] ?: "SOCIAL_MEDIA" }

    val quickPauseUntil: Flow<Long> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[QUICK_PAUSE_UNTIL_KEY] ?: 0L }

    val themePreference: Flow<String> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[THEME_KEY] ?: "DARK" }

    val isOtpProtectionEnabled: Flow<Boolean> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[OTP_PROTECTION_KEY] ?: true }

    val isFinancialProtectionEnabled: Flow<Boolean> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[FINANCIAL_PROTECTION_KEY] ?: true }

    val isPromotionalSmsFilterEnabled: Flow<Boolean> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[PROMOTIONAL_SMS_KEY] ?: true }

    val isEmergencyBypassEnabled: Flow<Boolean> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[EMERGENCY_BYPASS_KEY] ?: true }

    val dataRetentionDays: Flow<Int> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[DATA_RETENTION_DAYS_KEY] ?: 90 }

    val isOnboardingCompleted: Flow<Boolean> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[ONBOARDING_COMPLETED_KEY] ?: false }

    suspend fun setMasterBlockerEnabled(enabled: Boolean) {
        dataStore.edit { it[MASTER_BLOCKER_KEY] = enabled }
    }

    suspend fun setBlockAllEnabled(enabled: Boolean) {
        setMasterBlockerEnabled(enabled)
    }

    suspend fun setActiveBlockingMode(mode: String) {
        dataStore.edit { it[ACTIVE_BLOCKING_MODE_KEY] = mode }
    }

    suspend fun setQuickPauseUntil(timestamp: Long) {
        dataStore.edit { it[QUICK_PAUSE_UNTIL_KEY] = timestamp }
    }

    suspend fun setThemePreference(theme: String) {
        dataStore.edit { it[THEME_KEY] = theme }
    }

    suspend fun setOtpProtectionEnabled(enabled: Boolean) {
        dataStore.edit { it[OTP_PROTECTION_KEY] = enabled }
    }

    suspend fun setFinancialProtectionEnabled(enabled: Boolean) {
        dataStore.edit { it[FINANCIAL_PROTECTION_KEY] = enabled }
    }

    suspend fun setPromotionalSmsFilterEnabled(enabled: Boolean) {
        dataStore.edit { it[PROMOTIONAL_SMS_KEY] = enabled }
    }

    suspend fun setEmergencyBypassEnabled(enabled: Boolean) {
        dataStore.edit { it[EMERGENCY_BYPASS_KEY] = enabled }
    }

    suspend fun setDataRetentionDays(days: Int) {
        dataStore.edit { it[DATA_RETENTION_DAYS_KEY] = days }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { it[ONBOARDING_COMPLETED_KEY] = completed }
    }
}
