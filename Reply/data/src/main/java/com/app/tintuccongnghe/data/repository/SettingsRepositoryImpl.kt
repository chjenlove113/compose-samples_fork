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
