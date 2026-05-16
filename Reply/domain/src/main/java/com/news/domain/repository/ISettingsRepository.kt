package com.news.domain.repository

import kotlinx.coroutines.flow.Flow

interface ISettingsRepository {
    val nightModeFlow: Flow<Boolean>
    suspend fun setNightMode(enabled: Boolean)
}
