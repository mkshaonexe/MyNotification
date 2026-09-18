package com.my.notificationai.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.my.notificationai.core.domain.engine.NotificationDeduplicationEngine
import com.my.notificationai.core.domain.engine.NotificationRuleEngine
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MyNotificationListenerService : NotificationListenerService() {

    @Inject
    lateinit var ruleEngine: NotificationRuleEngine

    @Inject
    lateinit var deduplicationEngine: NotificationDeduplicationEngine

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
        deduplicationEngine.clearActiveKeys()
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

        // Skip our own app notifications to avoid recursion
        if (packageName == applicationContext.packageName) {
            return
        }

        serviceScope.launch {
            try {
                // Fetch user-facing app label
                val appLabel = try {
                    val pm = packageManager
                    val ai = pm.getApplicationInfo(packageName, 0)
                    pm.getApplicationLabel(ai).toString()
                } catch (e: Exception) {
                    packageName
                }

                // Deterministic rule evaluation
                val ruleResult = ruleEngine.evaluate(sbn)

                // UNCONDITIONAL CAPTURE: Always persist to database through deduplication engine
                deduplicationEngine.processPostedNotification(sbn, appLabel, ruleResult)

                // If rule dictates blocking, suppress notification from tray
                if (ruleResult.shouldBlock) {
                    try {
                        cancelNotification(sbn.key)
                        Log.d(TAG, "Blocked and cancelled notification from $packageName [${ruleResult.reason}]")
                    } catch (e: SecurityException) {
                        Log.w(TAG, "System prevented cancellation of notification from $packageName", e)
                    }
                } else {
                    Log.d(TAG, "Allowed notification from $packageName [${ruleResult.reason}]")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error processing incoming notification", e)
            }
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?, rankingMap: RankingMap?, reason: Int) {
        super.onNotificationRemoved(sbn, rankingMap, reason)
        if (sbn == null) return

        serviceScope.launch {
            try {
                val duration = deduplicationEngine.processRemovedNotification(sbn.key)
                if (duration != null) {
                    Log.d(TAG, "Notification ${sbn.key} removed after ${duration}ms (reason: $reason)")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error handling notification removal", e)
            }
        }
    }
}
