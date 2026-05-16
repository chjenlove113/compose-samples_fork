package com.news.domain.usecases

import com.news.domain.repository.ISettingsRepository
import javax.inject.Inject

class SetNightModeUseCase @Inject constructor(
    private val repository: ISettingsRepository
) {
    suspend operator fun invoke(enabled: Boolean) {
        repository.setNightMode(enabled)
    }
}
