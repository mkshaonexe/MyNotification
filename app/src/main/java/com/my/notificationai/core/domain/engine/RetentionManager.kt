package com.my.notificationai.core.domain.engine

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.my.notificationai.core.database.dao.NotificationDao
import com.my.notificationai.core.datastore.SettingsDataStore
import kotlinx.coroutines.flow.first
import java.io.File
import java.util.concurrent.TimeUnit

class RetentionManager(
    private val notificationDao: NotificationDao,
    private val settingsDataStore: SettingsDataStore,
    private val context: Context
) {

    suspend fun purgeExpiredRecords(): Int {
        val retentionDays = settingsDataStore.dataRetentionDays.first()
        if (retentionDays <= 0 || retentionDays >= 9999) {
            // Forever - do not purge
            return 0
        }
        val cutoff = System.currentTimeMillis() - (retentionDays * 24L * 60L * 60L * 1000L)
        return notificationDao.deleteEventsOlderThan(cutoff)
    }

    suspend fun clearAllHistory() {
        notificationDao.deleteAllEvents()
    }

    fun getDatabaseSizeBytes(): Long {
        return try {
            val dbFile = context.getDatabasePath("my_notification_db")
            if (dbFile.exists()) dbFile.length() else 0L
        } catch (e: Exception) {
            0L
        }
    }

    fun scheduleDailyPruning() {
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .build()

        val request = PeriodicWorkRequestBuilder<RetentionWorker>(24, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "RetentionCleanupWork",
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}

class RetentionWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            // In standalone execution or worker injection
            val db = androidx.room.Room.databaseBuilder(
                applicationContext,
                com.my.notificationai.core.database.AppDatabase::class.java,
                "my_notification_db"
            ).fallbackToDestructiveMigration(true).build()

            val settings = SettingsDataStore(applicationContext)
            val manager = RetentionManager(db.notificationDao(), settings, applicationContext)
            manager.purgeExpiredRecords()
            db.close()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
