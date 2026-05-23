package com.news.domain.repository

import kotlinx.coroutines.flow.Flow

interface ISettingsRepository {
    val nightModeFlow: Flow<Boolean>
    val fontScaleFlow: Flow<Float>
    suspend fun setNightMode(enabled: Boolean)
    suspend fun setFontScale(scale: Float)
}
