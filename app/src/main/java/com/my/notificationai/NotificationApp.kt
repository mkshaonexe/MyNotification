package com.my.notificationai

import android.app.Application
import com.my.notificationai.core.domain.engine.RetentionManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class NotificationApp : Application() {

    @Inject
    lateinit var retentionManager: RetentionManager

    override fun onCreate() {
        super.onCreate()
        try {
            retentionManager.scheduleDailyPruning()
        } catch (e: Exception) {
            // Ignored in headless or test environments
        }
    }
}
