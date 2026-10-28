package com.app.tintuccongnghe.newsScraping

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.tintuccongnghe.account.BillingManager
import com.app.tintuccongnghe.data.local.AppDatabase
import com.app.tintuccongnghe.data.local.entities.NewsDetailEntity
import com.app.tintuccongnghe.domain.models.NewsDetail
import com.app.tintuccongnghe.domain.repository.INewsScrapingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface NewsDetailUiState {
    object Loading : NewsDetailUiState
    data class Success(val detail: NewsDetail, val description: String = "") : NewsDetailUiState
    data class Error(val message: String) : NewsDetailUiState
}

@HiltViewModel
class NewsDetailViewModel @Inject constructor(
    private val repository: INewsScrapingRepository,
    private val appDatabase: AppDatabase,
    billingManager: BillingManager
) : ViewModel() {

    val hasActiveSubscription: StateFlow<Boolean> = billingManager.hasWebsiteLimitSubscription
    val hasCloudStorageSubscription: StateFlow<Boolean> = billingManager.hasCloudStorageSubscription

    private val _uiState = MutableStateFlow<NewsDetailUiState>(NewsDetailUiState.Loading)
    val uiState: StateFlow<NewsDetailUiState> = _uiState.asStateFlow()

    fun loadDetail(websiteId: String, link: String) {
        viewModelScope.launch {
            _uiState.value = NewsDetailUiState.Loading
            try {
                val detail = repository.getNewsDetail(websiteId, link)
                val scrapedNews = appDatabase.scrapedNewsDao().getNewsByLink(link)
                val description = scrapedNews?.shortDes ?: ""
                _uiState.value = NewsDetailUiState.Success(detail, description)
            } catch (e: Exception) {
                _uiState.value = NewsDetailUiState.Error(e.localizedMessage ?: "Không thể tải chi tiết bài viết")
            }
        }
    }

    fun toggleFavorite() {
        val current = _uiState.value
        if (current is NewsDetailUiState.Success) {
            viewModelScope.launch {
                val updatedDetail = current.detail.copy(isFavorite = !current.detail.isFavorite)
                appDatabase.newsDetailDao().insertDetail(
                    NewsDetailEntity(
                        link = updatedDetail.link,
                        websiteId = updatedDetail.websiteId,
                        title = updatedDetail.title,
                        author = updatedDetail.author,
                        publishDate = updatedDetail.publishDate,
                        description = current.description,
                        contentHtml = updatedDetail.contentHtml,
                        isFavorite = updatedDetail.isFavorite
                    )
                )
                _uiState.value = current.copy(detail = updatedDetail)
            }
        }
    }
}
