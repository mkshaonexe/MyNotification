package com.my.notificationai.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsDataStore(private val context: Context) {

    private val dataStore = context.dataStore

    companion object {
        val BLOCK_ALL_KEY = booleanPreferencesKey("is_block_all_enabled")
        val QUICK_PAUSE_UNTIL_KEY = longPreferencesKey("quick_pause_until")
        val THEME_KEY = stringPreferencesKey("theme_preference")
    }

    val isBlockAllEnabled: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(androidx.datastore.preferences.core.emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[BLOCK_ALL_KEY] ?: false
        }

    val quickPauseUntil: Flow<Long> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(androidx.datastore.preferences.core.emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[QUICK_PAUSE_UNTIL_KEY] ?: 0L
        }

    val themePreference: Flow<String> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(androidx.datastore.preferences.core.emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[THEME_KEY] ?: "LIGHT" // default to light theme as requested
        }

    suspend fun setBlockAllEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[BLOCK_ALL_KEY] = enabled
        }
    }

    suspend fun setQuickPauseUntil(timestamp: Long) {
        dataStore.edit { preferences ->
            preferences[QUICK_PAUSE_UNTIL_KEY] = timestamp
        }
    }

    suspend fun setThemePreference(theme: String) {
        dataStore.edit { preferences ->
            preferences[THEME_KEY] = theme
        }
    }
}
