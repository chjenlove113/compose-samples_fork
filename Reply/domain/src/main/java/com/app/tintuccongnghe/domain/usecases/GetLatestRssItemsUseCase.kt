package com.app.tintuccongnghe.domain.usecases

import com.app.tintuccongnghe.domain.models.RssItem
import com.app.tintuccongnghe.domain.repository.IRssRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetLatestRssItemsUseCase @Inject constructor(
    private val rssRepository: IRssRepository
) {
    operator fun invoke(): Flow<List<RssItem>> {
        return rssRepository.getLatestRssItems()
    }
}
