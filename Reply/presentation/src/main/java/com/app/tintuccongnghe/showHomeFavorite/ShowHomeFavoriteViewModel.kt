package com.app.tintuccongnghe.showHomeFavorite

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.tintuccongnghe.data.local.AppDatabase
import com.app.tintuccongnghe.data.local.entities.RssItemEntity
import com.app.tintuccongnghe.data.mappers.toNewsModel
import com.app.tintuccongnghe.domain.models.News
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class FavoriteItem {
    data class LocalRss(val item: RssItemEntity) : FavoriteItem()
    data class Website(val news: News) : FavoriteItem()
}

data class ShowHomeFavoriteUiState(
    val favorites: List<FavoriteItem> = emptyList(),
    val selectedRssItem: RssItemEntity? = null,
    val selectedWebsiteNews: News? = null,
    val isLoading: Boolean = false
)

@HiltViewModel
class ShowHomeFavoriteViewModel @Inject constructor(
    private val appDatabase: AppDatabase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ShowHomeFavoriteUiState())
    val uiState: StateFlow<ShowHomeFavoriteUiState> = _uiState.asStateFlow()

    init {
        loadFavorites()
    }

    private fun loadFavorites() {
        _uiState.update { it.copy(isLoading = true) }
        
        val rssFavoritesFlow = appDatabase.rssItemDao().getFavoriteItems()
        val websiteFavoritesFlow = appDatabase.newsDao().getAll()

        combine(rssFavoritesFlow, websiteFavoritesFlow) { rssItems, websiteNewsEntities ->
            val combined = mutableListOf<FavoriteItem>()
            
            combined.addAll(rssItems.map { FavoriteItem.LocalRss(it) })
            combined.addAll(websiteNewsEntities.map { FavoriteItem.Website(it.toNewsModel()) })
            
            combined.sortedWith { a, b ->
                val timeA = when (a) {
                    is FavoriteItem.LocalRss -> a.item.pubDate ?: 0L
                    is FavoriteItem.Website -> {
                        websiteNewsEntities.find { it.Id == a.news.Id }?.CreatedAt?.time ?: 0L
                    }
                }
                val timeB = when (b) {
                    is FavoriteItem.LocalRss -> b.item.pubDate ?: 0L
                    is FavoriteItem.Website -> {
                        websiteNewsEntities.find { it.Id == b.news.Id }?.CreatedAt?.time ?: 0L
                    }
                }
                timeB.compareTo(timeA) // Descending
            }
        }.onEach { sortedList ->
            _uiState.update { it.copy(favorites = sortedList, isLoading = false) }
        }.launchIn(viewModelScope)
    }

    fun setSelectedItem(item: FavoriteItem?) {
        when (item) {
            is FavoriteItem.LocalRss -> {
                _uiState.update { it.copy(selectedRssItem = item.item, selectedWebsiteNews = null) }
            }
            is FavoriteItem.Website -> {
                _uiState.update { it.copy(selectedWebsiteNews = item.news, selectedRssItem = null) }
            }
            null -> {
                _uiState.update { it.copy(selectedRssItem = null, selectedWebsiteNews = null) }
            }
        }
    }

    fun toggleFavoriteRss(item: RssItemEntity) {
        viewModelScope.launch {
            val updatedItem = item.copy(isFavorite = !item.isFavorite)
            appDatabase.rssItemDao().update(updatedItem)
            if (_uiState.value.selectedRssItem?.link == item.link) {
                _uiState.update { it.copy(selectedRssItem = if (updatedItem.isFavorite) updatedItem else null) }
            }
        }
    }

    fun deleteWebsiteFavorite(news: News) {
        viewModelScope.launch {
            val entity = appDatabase.newsDao().getNewsById(news.Id)
            if (entity != null) {
                appDatabase.newsDao().delete(entity)
                if (_uiState.value.selectedWebsiteNews?.Id == news.Id) {
                    _uiState.update { it.copy(selectedWebsiteNews = null) }
                }
            }
        }
    }
}
