package com.app.tintuccongnghe.domain.repository

import com.app.tintuccongnghe.domain.models.AppUserSite
import com.app.tintuccongnghe.domain.models.RssItem
import kotlinx.coroutines.flow.Flow

interface IRssRepository {
    fun getLatestRssItems(): Flow<List<RssItem>>
    fun getRssItemsForSite(siteId: Int, siteGroup: String, siteKind: String): Flow<List<RssItem>>
    fun getRssSites(): Flow<List<AppUserSite>>
    suspend fun fetchLatestRssItems(): List<RssItem>
    suspend fun refreshRssItems()
    suspend fun saveSyncedRssItems(items: List<RssItem>)
    suspend fun getItemByLink(link: String): RssItem?
}
