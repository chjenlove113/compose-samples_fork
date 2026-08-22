package com.app.tintuccongnghe.domain.usecases

import com.app.tintuccongnghe.domain.repository.ISettingsRepository
import javax.inject.Inject

class SetRefreshIntervalUseCase @Inject constructor(
    private val repository: ISettingsRepository
) {
    suspend operator fun invoke(minutes: Long) = repository.setRefreshInterval(minutes)
}
