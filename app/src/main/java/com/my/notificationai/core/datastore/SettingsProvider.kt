package com.my.notificationai.core.datastore

import kotlinx.coroutines.flow.Flow

interface SettingsProvider {
    val isMasterBlockerEnabled: Flow<Boolean>
    val isBlockAllEnabled: Flow<Boolean>
    val activeBlockingMode: Flow<String>
    val quickPauseUntil: Flow<Long>
    val themePreference: Flow<String>
    val isOtpProtectionEnabled: Flow<Boolean>
    val isFinancialProtectionEnabled: Flow<Boolean>
    val isPromotionalSmsFilterEnabled: Flow<Boolean>
    val isEmergencyBypassEnabled: Flow<Boolean>
    val dataRetentionDays: Flow<Int>
    val isOnboardingCompleted: Flow<Boolean>
}
