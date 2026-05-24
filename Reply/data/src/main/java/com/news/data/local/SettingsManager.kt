package com.news.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
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
}
