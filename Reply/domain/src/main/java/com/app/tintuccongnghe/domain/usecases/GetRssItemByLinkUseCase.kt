package com.app.tintuccongnghe.domain.usecases

import com.app.tintuccongnghe.domain.models.RssItem
import com.app.tintuccongnghe.domain.repository.IRssRepository
import javax.inject.Inject

class GetRssItemByLinkUseCase @Inject constructor(
    private val rssRepository: IRssRepository
) {
    suspend operator fun invoke(link: String): RssItem? {
        return rssRepository.getItemByLink(link)
    }
}
