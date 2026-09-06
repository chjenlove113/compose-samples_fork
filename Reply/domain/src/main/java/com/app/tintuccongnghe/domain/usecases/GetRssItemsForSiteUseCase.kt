package com.app.tintuccongnghe.domain.usecases

import com.app.tintuccongnghe.domain.models.RssItem
import com.app.tintuccongnghe.domain.repository.IRssRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetRssItemsForSiteUseCase @Inject constructor(
    private val repository: IRssRepository
) {
    operator fun invoke(siteId: Int, siteGroup: String, siteKind: String): Flow<List<RssItem>> {
        return repository.getRssItemsForSite(siteId, siteGroup, siteKind)
    }
}
