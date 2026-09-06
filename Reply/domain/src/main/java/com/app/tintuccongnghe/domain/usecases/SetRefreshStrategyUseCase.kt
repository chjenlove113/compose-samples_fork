package com.app.tintuccongnghe.domain.usecases

import com.app.tintuccongnghe.domain.repository.ISettingsRepository
import javax.inject.Inject

class SetRefreshStrategyUseCase @Inject constructor(
    private val repository: ISettingsRepository
) {
    suspend operator fun invoke(strategy: String) = repository.setRefreshStrategy(strategy)
}
