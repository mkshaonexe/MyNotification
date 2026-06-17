package com.my.notificationai.service

import android.content.Intent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.my.notificationai.data.AppRepository
import com.my.notificationai.data.SavedNotification
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MyNotificationListenerService : NotificationListenerService() {

    @Inject
    lateinit var repository: AppRepository

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    companion object {
        private const val TAG = "MyNotificationListener"
        var isServiceRunning = false
    }

    override fun onCreate() {
        super.onCreate()
        isServiceRunning = true
        Log.d(TAG, "Service Created")
    }

    override fun onDestroy() {
        super.onDestroy()
        isServiceRunning = false
        serviceJob.cancel()
        Log.d(TAG, "Service Destroyed")
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        isServiceRunning = true
        Log.d(TAG, "Listener Connected")
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        isServiceRunning = false
        Log.d(TAG, "Listener Disconnected")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        val packageName = sbn.packageName
        val notification = sbn.notification
        val extras = notification?.extras
        val title = extras?.getCharSequence("android.title")?.toString() ?: ""
        val text = extras?.getCharSequence("android.text")?.toString() ?: ""
        val channelId = sbn.notification?.channelId ?: ""

        // Skip our own notifications to avoid recursion
        if (packageName == applicationContext.packageName) {
            return
        }

        serviceScope.launch {
            val isBlockAll = repository.isBlockAllEnabled.first()
            val quickPauseUntil = repository.quickPauseUntil.first()
            val now = System.currentTimeMillis()

            // Check if Quick Pause is active
            if (now < quickPauseUntil) {
                Log.d(TAG, "Quick pause active. Skipping interception.")
                return@launch
            }

            // Check Smart Whitelist:
            // Auto-detect and protect: Phone calls, SMS/OTP, Alarms
            val isCall = packageName.contains("dialer") || packageName.contains("telephony") || packageName.contains("phone")
            val isSms = packageName.contains("messaging") || packageName.contains("sms") || packageName.contains("mms")
            val isAlarm = packageName.contains("clock") || packageName.contains("alarm")
            
            // Checking if notification is a call by checking category
            val isCallCategory = notification?.category == android.app.Notification.CATEGORY_CALL || notification?.category == android.app.Notification.CATEGORY_ALARM

            if (isCall || isSms || isAlarm || isCallCategory) {
                // Auto Whitelisted - do not block
                Log.d(TAG, "Whitelisted app $packageName. Allowing notification.")
                return@launch
            }

            // Check if we should block this app
            val blockedApp = repository.getBlockedApp(packageName)
            val shouldBlock = isBlockAll || (blockedApp != null && blockedApp.isBlocked)

            if (shouldBlock) {
                // Intercept and dismiss notification
                cancelNotification(sbn.key)
                Log.d(TAG, "Blocked notification from $packageName")

                // Fetch app name (label)
                val appLabel = try {
                    val pm = packageManager
                    val ai = pm.getApplicationInfo(packageName, 0)
                    pm.getApplicationLabel(ai).toString()
                } catch (e: Exception) {
                    packageName
                }

                // Check if OTP
                val isOtp = detectOtp(title, text)
                val otpCode = if (isOtp) extractOtp(text) else null

                // Save to Room Database
                val savedNotification = SavedNotification(
                    packageName = packageName,
                    appLabel = appLabel,
                    title = title,
                    body = text,
                    channelId = channelId,
                    category = if (isOtp) "OTP" else "Normal",
                    isOtp = isOtp,
                    otpCode = otpCode,
                    receivedAt = now
                )
                repository.insertNotification(savedNotification)
            }
        }
    }

    private fun detectOtp(title: String, body: String): Boolean {
        val otpPattern = "\\b\\d{4,8}\\b".toRegex()
        val textToSearch = "$title $body".lowercase()
        val hasOtpKeyword = textToSearch.contains("otp") || textToSearch.contains("code") || textToSearch.contains("verification") || textToSearch.contains("verify")
        return hasOtpKeyword && otpPattern.containsMatchIn("$title $body")
    }

    private fun extractOtp(body: String): String? {
        val otpPattern = "\\b\\d{4,8}\\b".toRegex()
        return otpPattern.find(body)?.value
    }
}
