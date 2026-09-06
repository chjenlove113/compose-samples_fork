package com.app.tintuccongnghe.domain.usecases

import com.app.tintuccongnghe.domain.models.RefreshTime
import com.app.tintuccongnghe.domain.repository.ISettingsRepository
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

class SetDailyRefreshSettingsUseCase @Inject constructor(
    private val repository: ISettingsRepository
) {
    suspend operator fun invoke(enabled: Boolean, times: List<RefreshTime>) {
        val timesJson = Json.encodeToString(times)
        repository.setDailyRefreshSettings(enabled, timesJson)
    }
}
