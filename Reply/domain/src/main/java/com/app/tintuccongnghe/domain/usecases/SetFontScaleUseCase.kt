package com.app.tintuccongnghe.domain.usecases

import com.app.tintuccongnghe.domain.repository.ISettingsRepository
import javax.inject.Inject

class SetFontScaleUseCase @Inject constructor(
    private val repository: ISettingsRepository
) {
    suspend operator fun invoke(scale: Float) = repository.setFontScale(scale)
}
