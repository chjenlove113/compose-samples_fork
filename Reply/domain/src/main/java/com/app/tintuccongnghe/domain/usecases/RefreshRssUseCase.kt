package com.app.tintuccongnghe.domain.usecases

import com.app.tintuccongnghe.domain.repository.IRssRepository
import javax.inject.Inject

class RefreshRssUseCase @Inject constructor(
    private val rssRepository: IRssRepository
) {
    suspend operator fun invoke() {
        rssRepository.refreshRssItems()
    }
}
