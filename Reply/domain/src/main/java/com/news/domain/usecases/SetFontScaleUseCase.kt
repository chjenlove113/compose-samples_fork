package com.news.domain.usecases

import com.news.domain.repository.ISettingsRepository
import javax.inject.Inject

class SetFontScaleUseCase @Inject constructor(
    private val repository: ISettingsRepository
) {
    suspend operator fun invoke(scale: Float) = repository.setFontScale(scale)
}
