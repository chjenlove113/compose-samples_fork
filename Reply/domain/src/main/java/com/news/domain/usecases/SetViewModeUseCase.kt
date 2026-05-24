package com.news.domain.usecases

import com.news.domain.repository.ISettingsRepository
import javax.inject.Inject

class SetViewModeUseCase @Inject constructor(
    private val repository: ISettingsRepository
) {
    suspend operator fun invoke(mode: String) = repository.setViewMode(mode)
}
