package com.app.tintuccongnghe.domain.usecases

import com.app.tintuccongnghe.domain.models.DailyRefreshSettings
import com.app.tintuccongnghe.domain.models.RefreshTime
import com.app.tintuccongnghe.domain.repository.ISettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.serialization.json.Json
import javax.inject.Inject

class GetDailyRefreshSettingsUseCase @Inject constructor(
    private val repository: ISettingsRepository
) {
    operator fun invoke(): Flow<DailyRefreshSettings> {
        return combine(
            repository.dailyRefreshEnabledFlow,
            repository.dailyRefreshTimesFlow
        ) { enabled, timesJson ->
            val times = try {
                Json.decodeFromString<List<RefreshTime>>(timesJson)
            } catch (e: Exception) {
                emptyList()
            }
            DailyRefreshSettings(enabled, times)
        }
    }
}
