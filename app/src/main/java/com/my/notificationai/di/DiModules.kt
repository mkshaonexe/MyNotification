package com.my.notificationai.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.my.notificationai.core.database.AppDatabase
import com.my.notificationai.core.database.dao.NotificationDao
import com.my.notificationai.core.database.dao.RuleDao
import com.my.notificationai.core.database.entities.BlockingRule
import com.my.notificationai.core.database.entities.NotificationCategory
import com.my.notificationai.core.database.entities.Schedule
import com.my.notificationai.core.database.entities.WhitelistedKeyword
import com.my.notificationai.core.datastore.SettingsDataStore
import com.my.notificationai.core.domain.engine.BackupManager
import com.my.notificationai.core.domain.engine.FinancialDetector
import com.my.notificationai.core.domain.engine.NotificationDeduplicationEngine
import com.my.notificationai.core.domain.engine.NotificationRuleEngine
import com.my.notificationai.core.domain.engine.OtpDetector
import com.my.notificationai.core.domain.engine.PromotionalFilter
import com.my.notificationai.core.domain.engine.RetentionManager
import com.my.notificationai.core.domain.engine.ScheduleEngine
import com.my.notificationai.data.AppRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DiModules {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        lateinit var database: AppDatabase
        database = Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "my_notification_db"
        )
        .fallbackToDestructiveMigration(true)
        .addCallback(object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                CoroutineScope(Dispatchers.IO).launch {
                    val ruleDao = database.ruleDao()
                    val now = System.currentTimeMillis()
                    ruleDao.insertRules(
                        listOf(
                            BlockingRule(name = "Block Social Media", description = "Instagram, Facebook, TikTok...", ruleType = "SOCIAL_MEDIA", action = "BLOCK", isEnabled = true, priority = 50, createdAt = now, updatedAt = now),
                            BlockingRule(name = "Block Selected Apps", description = "Select apps to block", ruleType = "SELECTED_APPS", action = "BLOCK", isEnabled = false, priority = 40, createdAt = now, updatedAt = now),
                            BlockingRule(name = "Allow Important Keywords", description = "OTP, verification, payment...", ruleType = "ALLOW_IMPORTANT_KEYWORDS", action = "ALLOW", isEnabled = true, priority = 90, createdAt = now, updatedAt = now),
                            BlockingRule(name = "Schedule Blocking", description = "10:00 PM – 7:00 AM", ruleType = "SCHEDULE_BLOCKING", action = "BLOCK", isEnabled = false, priority = 60, createdAt = now, updatedAt = now),
                            BlockingRule(name = "Block Everything", description = "Except whitelisted apps", ruleType = "BLOCK_EVERYTHING", action = "BLOCK", isEnabled = false, priority = 10, createdAt = now, updatedAt = now),
                            BlockingRule(name = "Custom Rules", description = "User-defined custom rules", ruleType = "CUSTOM", action = "BLOCK", isEnabled = false, priority = 20, createdAt = now, updatedAt = now)
                        )
                    )
                    ruleDao.insertCategories(
                        listOf(
                            NotificationCategory(categoryId = "social", name = "Social Media", packageNames = "com.instagram.android,com.facebook.katana,com.facebook.orca,com.zhiliaoapp.musically,com.twitter.android,com.snapchat.android,com.google.android.youtube,org.telegram.messenger", isEditable = true),
                            NotificationCategory(categoryId = "financial", name = "Financial", packageNames = "com.bKash.customerapp,com.konasl.nagad,com.dbbl.mbs.upay,com.ibbl.cellfin,com.thecitybank.citytouch,com.ebl.skybanking", isEditable = true),
                            NotificationCategory(categoryId = "messaging", name = "Messaging", packageNames = "com.whatsapp,org.telegram.messenger,com.facebook.orca,com.google.android.apps.messaging", isEditable = true),
                            NotificationCategory(categoryId = "work", name = "Work", packageNames = "com.google.android.gm,com.microsoft.office.outlook,com.slack,com.google.android.calendar", isEditable = true)
                        )
                    )
                    ruleDao.insertWhitelistedKeywords(
                        listOf("otp", "code", "verification", "one-time", "token", "2fa", "security code", "password").map {
                            WhitelistedKeyword(keyword = it, category = "OTP", addedAt = now)
                        }
                    )
                    ruleDao.insertSchedule(
                        Schedule(title = "Night Focus", startHour = 22, startMinute = 0, endHour = 7, endMinute = 0, repeatDays = "ALL", action = "BLOCK_ALL", isEnabled = false, exceptions = "OTP,Financial,Calls", createdAt = now)
                    )
                }
            }
        })
        .build()
        return database
    }

    @Provides
    @Singleton
    fun provideNotificationDao(database: AppDatabase): NotificationDao = database.notificationDao()

    @Provides
    @Singleton
    fun provideRuleDao(database: AppDatabase): RuleDao = database.ruleDao()

    @Provides
    @Singleton
    fun provideSettingsDataStore(@ApplicationContext context: Context): SettingsDataStore = SettingsDataStore(context)

    @Provides
    @Singleton
    fun provideOtpDetector(): OtpDetector = OtpDetector()

    @Provides
    @Singleton
    fun provideFinancialDetector(): FinancialDetector = FinancialDetector()

    @Provides
    @Singleton
    fun providePromotionalFilter(): PromotionalFilter = PromotionalFilter()

    @Provides
    @Singleton
    fun provideScheduleEngine(): ScheduleEngine = ScheduleEngine()

    @Provides
    @Singleton
    fun provideNotificationRuleEngine(
        ruleDao: RuleDao,
        settingsDataStore: SettingsDataStore,
        otpDetector: OtpDetector,
        financialDetector: FinancialDetector,
        promotionalFilter: PromotionalFilter,
        scheduleEngine: ScheduleEngine
    ): NotificationRuleEngine = NotificationRuleEngine(
        ruleDao,
        settingsDataStore,
        otpDetector,
        financialDetector,
        promotionalFilter,
        scheduleEngine
    )

    @Provides
    @Singleton
    fun provideNotificationDeduplicationEngine(
        notificationDao: NotificationDao
    ): NotificationDeduplicationEngine = NotificationDeduplicationEngine(notificationDao)

    @Provides
    @Singleton
    fun provideRetentionManager(
        notificationDao: NotificationDao,
        settingsDataStore: SettingsDataStore,
        @ApplicationContext context: Context
    ): RetentionManager = RetentionManager(notificationDao, settingsDataStore, context)

    @Provides
    @Singleton
    fun provideBackupManager(ruleDao: RuleDao): BackupManager = BackupManager(ruleDao)
}
