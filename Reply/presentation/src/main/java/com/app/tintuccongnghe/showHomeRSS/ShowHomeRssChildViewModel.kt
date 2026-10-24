package com.app.tintuccongnghe.showHomeRSS

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.app.tintuccongnghe.data.local.AppDatabase
import com.app.tintuccongnghe.data.local.entities.RssItemEntity
import com.rometools.rome.feed.synd.SyndFeed
import com.rometools.rome.io.SyndFeedInput
import com.rometools.rome.io.XmlReader
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

data class ShowHomeRssChildUiState(
    val isLoading: Boolean = false
)

@HiltViewModel(assistedFactory = ShowHomeRssChildViewModel.Factory::class)
class ShowHomeRssChildViewModel @AssistedInject constructor(
    @Assisted private val siteId: Int,
    @Assisted("siteGroup") private val siteGroup: String,
    @Assisted("siteKind") private val siteKind: String,
    private val appDatabase: AppDatabase,
    private val okHttpClient: OkHttpClient
) : ViewModel() {

    private val _uiState = MutableStateFlow(ShowHomeRssChildUiState())
    val uiState: StateFlow<ShowHomeRssChildUiState> = _uiState.asStateFlow()

    init {
        checkAndSyncIfNeeded()
    }

    private fun checkAndSyncIfNeeded() {
        viewModelScope.launch(Dispatchers.IO) {
            val count = appDatabase.rssItemDao().getItemCountForSite(siteId, siteGroup, siteKind)
            if (count == 0) {
                val site = appDatabase.appUserSiteDao().getSiteById(siteId, siteGroup, siteKind)
                if (site != null && !site.Url.isNullOrEmpty()) {
                    try {
                        val request = Request.Builder().url(site.Url).build()
                        okHttpClient.newCall(request).execute().use { response ->
                            if (response.isSuccessful) {
                                val currentTime = System.currentTimeMillis()
                                val nextTime = currentTime + TimeUnit.MINUTES.toMillis(15)
                                val input = SyndFeedInput()
                                val feed: SyndFeed = input.build(XmlReader(response.body!!.byteStream()))
                                val itemsToInsert = mutableListOf<RssItemEntity>()

                                appDatabase.appUserSiteDao().update(site.copy(
                                    lastRefreshTime = currentTime,
                                    nextRefreshTime = nextTime,
                                    itemCount = feed.entries.size
                                ))

                                feed.entries.forEach { entry ->
                                    val link = entry.link ?: ""
                                    if (link.isNotEmpty()) {
                                        val existingItem = appDatabase.rssItemDao().getItemByLink(link)
                                        if (existingItem == null) {
                                            val contentValue = entry.contents?.firstOrNull()?.value
                                                ?: entry.description?.value
                                            val imageUrl = entry.enclosures?.firstOrNull()?.url

                                            itemsToInsert.add(
                                                RssItemEntity(
                                                    title = entry.title ?: "",
                                                    link = link,
                                                    description = entry.description?.value,
                                                    pubDate = entry.publishedDate?.time,
                                                    siteId = siteId,
                                                    siteGroup = siteGroup,
                                                    siteKind = siteKind,
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
                                    appDatabase.rssItemDao().insertAll(itemsToInsert)
                                }
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    val rssItemsPagingData: Flow<PagingData<RssItemEntity>> = Pager(
        config = PagingConfig(
            pageSize = 10,
            enablePlaceholders = false
        ),
        pagingSourceFactory = { appDatabase.rssItemDao().getRssItemsForSitePaging(siteId, siteGroup, siteKind) }
    ).flow.cachedIn(viewModelScope)

    fun toggleFavorite(item: RssItemEntity) {
        viewModelScope.launch {
            val updated = item.copy(isFavorite = !item.isFavorite)
            appDatabase.rssItemDao().update(updated)
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted siteId: Int,
            @Assisted("siteGroup") siteGroup: String,
            @Assisted("siteKind") siteKind: String
        ): ShowHomeRssChildViewModel
    }
}
