package com.my.notificationai.di

import android.content.Context
import androidx.room.Room
import com.my.notificationai.data.AppDatabase
import com.my.notificationai.data.AppRepository
import com.my.notificationai.data.NotificationDao
import com.my.notificationai.data.SettingsDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DiModules {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "my_notification_db"
        )
        .fallbackToDestructiveMigration() // Simple for MVP development
        .build()
    }

    @Provides
    @Singleton
    fun provideNotificationDao(database: AppDatabase): NotificationDao {
        return database.notificationDao()
    }

    @Provides
    @Singleton
    fun provideSettingsDataStore(@ApplicationContext context: Context): SettingsDataStore {
        return SettingsDataStore(context)
    }

    @Provides
    @Singleton
    fun provideAppRepository(
        notificationDao: NotificationDao,
        settingsDataStore: SettingsDataStore
    ): AppRepository {
        return AppRepository(notificationDao, settingsDataStore)
    }
}
