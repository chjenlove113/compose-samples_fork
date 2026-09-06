package com.app.tintuccongnghe.domain.usecases

import com.app.tintuccongnghe.domain.repository.ISettingsRepository
import javax.inject.Inject

class SetLanguageUseCase @Inject constructor(
    private val repository: ISettingsRepository
) {
    suspend operator fun invoke(language: String) = repository.setLanguage(language)
}
