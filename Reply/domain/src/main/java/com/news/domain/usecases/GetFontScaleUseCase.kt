package com.news.domain.usecases

import com.news.domain.repository.ISettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetFontScaleUseCase @Inject constructor(
    private val repository: ISettingsRepository
) {
    operator fun invoke(): Flow<Float> = repository.fontScaleFlow
}
