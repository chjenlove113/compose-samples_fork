package com.app.tintuccongnghe.data.repository

import android.util.Log
import androidx.room.withTransaction
import com.app.tintuccongnghe.data.local.AppDatabase
import com.app.tintuccongnghe.data.local.entities.AppUserSiteEntity
import com.app.tintuccongnghe.data.local.entities.RssItemEntity
import com.app.tintuccongnghe.data.local.entities.toDomain
import com.app.tintuccongnghe.data.mappers.toRssItem
import com.app.tintuccongnghe.domain.models.AppUserSite
import com.app.tintuccongnghe.domain.models.RssItem
import com.app.tintuccongnghe.domain.repository.IRssRepository
import com.rometools.rome.feed.synd.SyndFeed
import com.rometools.rome.io.SyndFeedInput
import com.rometools.rome.io.XmlReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class RssRepositoryImpl @Inject constructor(
    private val appDatabase: AppDatabase,
    private val okHttpClient: OkHttpClient
) : IRssRepository {
    private val rssItemDao = appDatabase.rssItemDao()
    private val siteDao = appDatabase.appUserSiteDao()

    override fun getLatestRssItems(): Flow<List<RssItem>> {
        return rssItemDao.getLatestRssItemsFlow().map { list -> list.map { it.toRssItem() } }
    }

    override fun getRssItemsForSite(siteId: Int, siteGroup: String, siteKind: String): Flow<List<RssItem>> {
        return rssItemDao.getRssItemsForSite(siteId, siteGroup, siteKind).map { list -> list.map { it.toRssItem() } }
    }

    override fun getRssSites(): Flow<List<AppUserSite>> {
        return siteDao.getAllSitesFlow().map { entities -> 
            entities.filter { it.GROUP == "1" }.map { it.toDomain() }
        }
    }

    override suspend fun fetchLatestRssItems(): List<RssItem> {
        return rssItemDao.getLatestRssItems().map { it.toRssItem() }
    }

    override suspend fun getItemByLink(link: String): RssItem? {
        return rssItemDao.getItemByLink(link)?.toRssItem()
    }

    override suspend fun saveSyncedRssItems(items: List<RssItem>) = withContext(Dispatchers.IO) {
        appDatabase.withTransaction {
            val parentSites = items
                .distinctBy { Triple(it.siteId, it.siteGroup, it.siteKind) }
                .map { item ->
                    AppUserSiteEntity(
                        Id = item.siteId,
                        Key = "wear-${item.siteId}-${item.siteGroup}-${item.siteKind}",
                        Name = item.siteName ?: "RSS",
                        TextColor = null,
                        BackgroundColor = null,
                        Url = "",
                        IsActive = true,
                        Kind = item.siteKind,
                        GROUP = item.siteGroup,
                        Stt = 0,
                        LimitEdit = false,
                        AllowEdit = false,
                        OtherCanSee = false,
                        IdentityId = "wear-sync",
                        AppIdEncrypt = "wear-sync",
                        itemCount = items.count {
                            it.siteId == item.siteId &&
                                it.siteGroup == item.siteGroup &&
                                it.siteKind == item.siteKind
                        }
                    )
                }

            if (parentSites.isNotEmpty()) {
                siteDao.insertAll(parentSites)
            }
            rssItemDao.deleteAll()
            if (items.isNotEmpty()) {
                rssItemDao.insertAll(items.map { item ->
                    RssItemEntity(
                        title = item.title,
                        link = item.link,
                        description = item.description,
                        pubDate = item.pubDate,
                        siteId = item.siteId,
                        siteGroup = item.siteGroup,
                        siteKind = item.siteKind,
                        updDate = item.updDate,
                        content = item.content,
                        siteName = item.siteName,
                        imageUrl = item.imageUrl,
                        isFavorite = item.isFavorite
                    )
                })
            }
        }
    }

    override suspend fun refreshRssItems() = withContext(Dispatchers.IO) {
        Log.d("RssRepositoryImpl", "Refreshing RSS items...")
        val sites = siteDao.getAllSites()
        
        sites.forEach { site ->
            try {
                if (site.GROUP == "1" && !site.Url.isNullOrEmpty()) {
                    val request = Request.Builder().url(site.Url).build()
                    okHttpClient.newCall(request).execute().use { response ->
                        val currentTime = System.currentTimeMillis()
                        val nextTime = currentTime + TimeUnit.MINUTES.toMillis(15)

                        if (response.isSuccessful) {
                            val input = SyndFeedInput()
                            val feed: SyndFeed = input.build(XmlReader(response.body!!.byteStream()))
                            val itemsToInsert = mutableListOf<RssItemEntity>()

                            siteDao.update(site.copy(
                                lastRefreshTime = currentTime,
                                nextRefreshTime = nextTime,
                                itemCount = feed.entries.size
                            ))
                            
                            feed.entries.forEach { entry ->
                                val link = entry.link ?: ""
                                if (link.isNotEmpty()) {
                                    val existingItem = rssItemDao.getItemByLink(link)
                                    if (existingItem != null) {
                                        rssItemDao.update(existingItem.copy(updDate = currentTime))
                                    } else {
                                        val contentValue = entry.contents?.firstOrNull()?.value
                                            ?: entry.modules?.filterIsInstance<com.rometools.rome.feed.module.DCModule>()?.firstOrNull()?.description
                                            ?: entry.description?.value

                                        val imageUrl = entry.enclosures?.firstOrNull()?.url
                                            ?: extractImageFromHtml(contentValue ?: "")

                                        itemsToInsert.add(
                                            RssItemEntity(
                                                title = entry.title ?: "",
                                                link = link,
                                                description = entry.description?.value,
                                                pubDate = entry.publishedDate?.time,
                                                siteId = site.Id,
                                                siteGroup = site.GROUP,
                                                siteKind = site.Kind,
                                                updDate = currentTime,
                                                content = contentValue,
                                                siteName = site.Name,
                                                imageUrl = imageUrl
                                            )
                                        )
                                    }
                                }
                            }
                            
                            if (itemsToInsert.isNotEmpty()) {
                                rssItemDao.insertAll(itemsToInsert)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("RssRepositoryImpl", "Error refreshing site ${site.Id}", e)
            }
        }
    }

    private fun extractImageFromHtml(html: String): String? {
        val imgRegex = "<img[^>]+src\\s*=\\s*['\"]([^'\"]+)['\"][^>]*>".toRegex(RegexOption.IGNORE_CASE)
        return imgRegex.find(html)?.groupValues?.get(1)
    }
}
