package com.app.tintuccongnghe.domain.repository

import kotlinx.coroutines.flow.Flow

interface ISettingsRepository {
    val nightModeFlow: Flow<Boolean>
    val fontScaleFlow: Flow<Float>
    val viewModeFlow: Flow<String>
    val languageFlow: Flow<String>
    val refreshIntervalFlow: Flow<Long>
    val dailyRefreshEnabledFlow: Flow<Boolean>
    val dailyRefreshTimesFlow: Flow<String>
    val refreshStrategyFlow: Flow<String>
    suspend fun setNightMode(enabled: Boolean)
    suspend fun setFontScale(scale: Float)
    suspend fun setViewMode(mode: String)
    suspend fun setLanguage(language: String)
    suspend fun setRefreshInterval(minutes: Long)
    suspend fun setDailyRefreshSettings(enabled: Boolean, timesJson: String)
    suspend fun setRefreshStrategy(strategy: String)
}
