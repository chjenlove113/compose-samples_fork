package com.app.tintuccongnghe.data.repository

import com.app.tintuccongnghe.data.local.SettingsManager
import com.app.tintuccongnghe.domain.repository.ISettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(
    private val settingsManager: SettingsManager
) : ISettingsRepository {
    override val nightModeFlow: Flow<Boolean> = settingsManager.nightModeFlow
    override val fontScaleFlow: Flow<Float> = settingsManager.fontScaleFlow
    override val viewModeFlow: Flow<String> = settingsManager.viewModeFlow
    override val languageFlow: Flow<String> = settingsManager.languageFlow
    override val refreshIntervalFlow: Flow<Long> = settingsManager.refreshIntervalFlow
    override val dailyRefreshEnabledFlow: Flow<Boolean> = settingsManager.dailyRefreshEnabledFlow
    override val dailyRefreshTimesFlow: Flow<String> = settingsManager.dailyRefreshTimesFlow
    override val refreshStrategyFlow: Flow<String> = settingsManager.refreshStrategyFlow

    override suspend fun setNightMode(enabled: Boolean) {
        settingsManager.setNightMode(enabled)
    }

    override suspend fun setFontScale(scale: Float) {
        settingsManager.setFontScale(scale)
    }

    override suspend fun setViewMode(mode: String) {
        settingsManager.setViewMode(mode)
    }

    override suspend fun setLanguage(language: String) {
        settingsManager.setLanguage(language)
    }

    override suspend fun setRefreshInterval(minutes: Long) {
        settingsManager.setRefreshInterval(minutes)
    }

    override suspend fun setDailyRefreshSettings(enabled: Boolean, timesJson: String) {
        settingsManager.setDailyRefreshSettings(enabled, timesJson)
    }

    override suspend fun setRefreshStrategy(strategy: String) {
        settingsManager.setRefreshStrategy(strategy)
    }
}
