package com.app.tintuccongnghe.domain.usecases

import com.app.tintuccongnghe.domain.repository.ISettingsRepository
import javax.inject.Inject

class SetNightModeUseCase @Inject constructor(
    private val repository: ISettingsRepository
) {
    suspend operator fun invoke(enabled: Boolean) {
        repository.setNightMode(enabled)
    }
}
