package com.app.tintuccongnghe.domain.usecases

import com.app.tintuccongnghe.domain.repository.ISettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetNightModeUseCase @Inject constructor(
    private val repository: ISettingsRepository
) {
    operator fun invoke(): Flow<Boolean> = repository.nightModeFlow
}
