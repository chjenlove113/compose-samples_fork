package com.app.tintuccongnghe.showHomeRSS

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.tintuccongnghe.data.local.AppDatabase
import com.app.tintuccongnghe.data.local.entities.RssItemEntity
import com.app.tintuccongnghe.data.local.entities.toDomain
import com.app.tintuccongnghe.data.local.entities.toEntity
import com.app.tintuccongnghe.domain.models.AppUserSite
import com.app.tintuccongnghe.domain.usecases.GetAuthInfoUseCase
import com.app.tintuccongnghe.utils.AppContants
import com.rometools.rome.feed.synd.SyndFeed
import com.rometools.rome.io.SyndFeedInput
import com.rometools.rome.io.XmlReader
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit
import javax.inject.Inject

data class ShowHomeRssUiState(
    val sites: List<AppUserSite> = emptyList(),
    val isLoading: Boolean = false,
    val isSyncing: Boolean = false,
    val error: String? = null,
    val selectedItem: RssItemEntity? = null,
    val isLoggedIn: Boolean = true
)

@HiltViewModel
class ShowHomeRssViewModel @Inject constructor(
    private val appDatabase: AppDatabase,
    private val okHttpClient: OkHttpClient,
    private val getAuthInfoUseCase: GetAuthInfoUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ShowHomeRssUiState())
    val uiState: StateFlow<ShowHomeRssUiState> = _uiState.asStateFlow()

    init {
        loadSites()
    }

    private fun loadSites() {
        viewModelScope.launch {
            getAuthInfoUseCase().collect { auth ->
                if (auth != null) {
                    _uiState.update { it.copy(isLoggedIn = true, isLoading = true) }
                    appDatabase.appUserSiteDao().getAppUserSites(auth.IdentityId, AppContants.app_Id)
                        .map { entities -> 
                            entities.filter { it.GROUP == "1" }.map { it.toDomain() } 
                        }
                        .collect { sites ->
                            _uiState.update { it.copy(sites = sites, isLoading = false) }
                        }
                } else {
                    _uiState.update { it.copy(isLoggedIn = false, isLoading = false) }
                }
            }
        }
    }

    fun syncRss() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true) }
            withContext(Dispatchers.IO) {
                val siteDao = appDatabase.appUserSiteDao()
                val rssItemDao = appDatabase.rssItemDao()
                val sites = _uiState.value.sites

                sites.forEach { site ->
                    try {
                        if (site.GROUP == "1" && site.Url.isNotEmpty()) {
                            val request = Request.Builder().url(site.Url).build()
                            okHttpClient.newCall(request).execute().use { response ->
                                val currentTime = System.currentTimeMillis()
                                val nextTime = currentTime + TimeUnit.MINUTES.toMillis(15)

                                if (response.isSuccessful) {
                                    val input = SyndFeedInput()
                                    val feed: SyndFeed = input.build(XmlReader(response.body!!.byteStream()))
                                    val itemsToInsert = mutableListOf<RssItemEntity>()
                                    val totalItems = feed.entries.size

                                    siteDao.update(site.toEntity().copy(
                                        lastRefreshTime = currentTime,
                                        nextRefreshTime = nextTime,
                                        itemCount = totalItems
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
                                                        siteName = site.Name
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
                        e.printStackTrace()
                    }
                }
            }
            _uiState.update { it.copy(isSyncing = false) }
        }
    }

    fun setSelectedRssItem(item: RssItemEntity?) {
        _uiState.update { it.copy(selectedItem = item) }
    }

    fun toggleFavorite(item: RssItemEntity) {
        viewModelScope.launch {
            val updatedItem = item.copy(isFavorite = !item.isFavorite)
            appDatabase.rssItemDao().update(updatedItem)
            if (_uiState.value.selectedItem?.link == item.link) {
                _uiState.update { it.copy(selectedItem = updatedItem) }
            }
        }
    }
}
