package com.news.data.repository

import com.news.data.local.SettingsManager
import com.news.domain.repository.ISettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(
    private val settingsManager: SettingsManager
) : ISettingsRepository {
    override val nightModeFlow: Flow<Boolean> = settingsManager.nightModeFlow
    override val fontScaleFlow: Flow<Float> = settingsManager.fontScaleFlow
    override val viewModeFlow: Flow<String> = settingsManager.viewModeFlow

    override suspend fun setNightMode(enabled: Boolean) {
        settingsManager.setNightMode(enabled)
    }

    override suspend fun setFontScale(scale: Float) {
        settingsManager.setFontScale(scale)
    }

    override suspend fun setViewMode(mode: String) {
        settingsManager.setViewMode(mode)
    }
}
