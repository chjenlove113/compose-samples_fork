package com.app.tintuccongnghe.newsScraping

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.tintuccongnghe.domain.models.NewsCategory
import com.app.tintuccongnghe.domain.models.NewsItem
import com.app.tintuccongnghe.domain.models.NewsWebsite
import com.app.tintuccongnghe.domain.repository.INewsScrapingRepository
import com.app.tintuccongnghe.domain.usecases.GetCategoriesUseCase
import com.app.tintuccongnghe.domain.usecases.GetWebsitesUseCase
import com.app.tintuccongnghe.domain.usecases.ScrapeAndSaveNewsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface NewsScrapingUiState {
    object Loading : NewsScrapingUiState
    data class Success(
        val websites: List<NewsWebsite> = emptyList(),
        val homeNews: List<NewsItem> = emptyList(),
        val websiteCategories: Map<String, List<NewsCategory>> = emptyMap(),
        val websiteNews: Map<String, List<NewsItem>> = emptyMap(),
        val categoryNews: Map<String, List<NewsItem>> = emptyMap(),
        val selectedMainTab: Int = 0, // 0 = Home, 1..N = Website
        val selectedSubTabMap: Map<String, Int> = emptyMap(), // websiteId -> sub-tab index (0 = All, 1..M = Category)
        val isLoading: Boolean = false,
        val isLoadingMore: Boolean = false,
        val currentPage: Int = 1,
        val hasMorePages: Boolean = true
    ) : NewsScrapingUiState
    object Empty : NewsScrapingUiState
    data class Error(val message: String) : NewsScrapingUiState
}

@HiltViewModel
class NewsScrapingViewModel @Inject constructor(
    private val getWebsitesUseCase: GetWebsitesUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val scrapeAndSaveNewsUseCase: ScrapeAndSaveNewsUseCase,
    private val repository: INewsScrapingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<NewsScrapingUiState>(NewsScrapingUiState.Loading)
    val uiState: StateFlow<NewsScrapingUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = NewsScrapingUiState.Loading
            try {
                val websites = getWebsitesUseCase()
                if (websites.isEmpty()) {
                    _uiState.value = NewsScrapingUiState.Empty
                    return@launch
                }

                // Observe Home news
                viewModelScope.launch {
                    repository.getHomeNews().collectLatest { homeItems ->
                        val current = _uiState.value
                        if (current is NewsScrapingUiState.Success) {
                            _uiState.value = current.copy(homeNews = homeItems)
                        }
                    }
                }

                val categoriesMap = mutableMapOf<String, List<NewsCategory>>()
                for (site in websites) {
                    val cats = getCategoriesUseCase(site.id)
                    categoriesMap[site.id] = cats

                    // Observe website news ("All" tab)
                    viewModelScope.launch {
                        repository.getNewsForWebsite(site.id).collectLatest { siteItems ->
                            val current = _uiState.value
                            if (current is NewsScrapingUiState.Success) {
                                val updatedMap = current.websiteNews.toMutableMap()
                                updatedMap[site.id] = siteItems
                                _uiState.value = current.copy(websiteNews = updatedMap)
                            }
                        }
                    }

                    // Observe category news
                    for (cat in cats) {
                        viewModelScope.launch {
                            repository.getCachedNews(cat.id).collectLatest { catItems ->
                                val current = _uiState.value
                                if (current is NewsScrapingUiState.Success) {
                                    val updatedMap = current.categoryNews.toMutableMap()
                                    updatedMap[cat.id] = catItems
                                    _uiState.value = current.copy(categoryNews = updatedMap)
                                }
                            }
                        }
                    }
                }

                _uiState.value = NewsScrapingUiState.Success(
                    websites = websites,
                    websiteCategories = categoriesMap,
                    isLoading = true
                )

                // Trigger initial scrape for all websites and their categories
                for (site in websites) {
                    val cats = categoriesMap[site.id] ?: emptyList()
                    for (cat in cats) {
                        launch {
                            try {
                                scrapeAndSaveNewsUseCase(site.id, cat, page = 1, append = false)
                            } catch (_: Exception) {}
                        }
                    }
                }

                _uiState.value = (_uiState.value as? NewsScrapingUiState.Success)?.copy(isLoading = false) ?: _uiState.value

            } catch (e: Exception) {
                _uiState.value = NewsScrapingUiState.Error(e.localizedMessage ?: "Lỗi tải dữ liệu")
            }
        }
    }

    fun selectMainTab(index: Int) {
        val current = _uiState.value
        if (current is NewsScrapingUiState.Success) {
            _uiState.value = current.copy(selectedMainTab = index, currentPage = 1, hasMorePages = true)
        }
    }

    fun selectSubTab(websiteId: String, subIndex: Int) {
        val current = _uiState.value
        if (current is NewsScrapingUiState.Success) {
            val updatedSubMap = current.selectedSubTabMap.toMutableMap()
            updatedSubMap[websiteId] = subIndex
            _uiState.value = current.copy(selectedSubTabMap = updatedSubMap, currentPage = 1, hasMorePages = true)
        }
    }

    fun loadMore() {
        val current = _uiState.value
        if (current is NewsScrapingUiState.Success && !current.isLoadingMore && current.hasMorePages) {
            if (current.selectedMainTab > 0) {
                val site = current.websites.getOrNull(current.selectedMainTab - 1)
                if (site != null) {
                    val subIndex = current.selectedSubTabMap[site.id] ?: 0
                    if (subIndex > 0) {
                        val cats = current.websiteCategories[site.id] ?: emptyList()
                        val cat = cats.getOrNull(subIndex - 1)
                        if (cat != null) {
                            val nextPage = current.currentPage + 1
                            _uiState.value = current.copy(isLoadingMore = true)
                            viewModelScope.launch {
                                try {
                                    val newItems = scrapeAndSaveNewsUseCase(site.id, cat, page = nextPage, append = true)
                                    val latest = _uiState.value
                                    if (latest is NewsScrapingUiState.Success) {
                                        if (newItems.isEmpty()) {
                                            _uiState.value = latest.copy(isLoadingMore = false, hasMorePages = false)
                                        } else {
                                            _uiState.value = latest.copy(isLoadingMore = false, currentPage = nextPage)
                                        }
                                    }
                                } catch (_: Exception) {
                                    val latest = _uiState.value
                                    if (latest is NewsScrapingUiState.Success) {
                                        _uiState.value = latest.copy(isLoadingMore = false)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    fun refresh() {
        loadData()
    }
}
