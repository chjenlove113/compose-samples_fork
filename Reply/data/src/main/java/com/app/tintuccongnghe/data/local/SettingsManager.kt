package com.app.tintuccongnghe.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "settings")

@Singleton
class SettingsManager @Inject constructor(@ApplicationContext context: Context) {

    private val dataStore = context.dataStore

    private object PreferencesKeys {
        val NIGHT_MODE = booleanPreferencesKey("night_mode")
        val FONT_SCALE = floatPreferencesKey("font_scale")
        val VIEW_MODE = androidx.datastore.preferences.core.stringPreferencesKey("view_mode")
        val LANGUAGE = androidx.datastore.preferences.core.stringPreferencesKey("language")
        val REFRESH_INTERVAL = androidx.datastore.preferences.core.longPreferencesKey("refresh_interval")
        val DAILY_REFRESH_ENABLED = booleanPreferencesKey("daily_refresh_enabled")
        val DAILY_REFRESH_TIMES = androidx.datastore.preferences.core.stringPreferencesKey("daily_refresh_times")
        val REFRESH_STRATEGY = androidx.datastore.preferences.core.stringPreferencesKey("refresh_strategy")
    }

    val nightModeFlow: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.NIGHT_MODE] ?: false
    }

    val fontScaleFlow: Flow<Float> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.FONT_SCALE] ?: 1.0f
    }

    val viewModeFlow: Flow<String> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.VIEW_MODE] ?: "list"
    }

    val languageFlow: Flow<String> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.LANGUAGE] ?: "en"
    }

    val refreshIntervalFlow: Flow<Long> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.REFRESH_INTERVAL] ?: 15L
    }

    val dailyRefreshEnabledFlow: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.DAILY_REFRESH_ENABLED] ?: false
    }

    val dailyRefreshTimesFlow: Flow<String> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.DAILY_REFRESH_TIMES] ?: "[]"
    }

    val refreshStrategyFlow: Flow<String> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.REFRESH_STRATEGY] ?: "DAILY" // DAILY for Work2Scheduler, INTERVAL for WorkScheduler
    }

    suspend fun setNightMode(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.NIGHT_MODE] = enabled
        }
    }

    suspend fun setFontScale(scale: Float) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.FONT_SCALE] = scale
        }
    }

    suspend fun setViewMode(mode: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.VIEW_MODE] = mode
        }
    }

    suspend fun setLanguage(language: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.LANGUAGE] = language
        }
    }

    suspend fun setRefreshInterval(minutes: Long) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.REFRESH_INTERVAL] = minutes
        }
    }

    suspend fun setDailyRefreshSettings(enabled: Boolean, timesJson: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.DAILY_REFRESH_ENABLED] = enabled
            preferences[PreferencesKeys.DAILY_REFRESH_TIMES] = timesJson
        }
    }

    suspend fun setRefreshStrategy(strategy: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.REFRESH_STRATEGY] = strategy
        }
    }
}
