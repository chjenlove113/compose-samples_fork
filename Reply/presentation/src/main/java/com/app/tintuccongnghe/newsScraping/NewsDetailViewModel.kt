package com.app.tintuccongnghe.newsScraping

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    data class Success(val detail: NewsDetail) : NewsDetailUiState
    data class Error(val message: String) : NewsDetailUiState
}

@HiltViewModel
class NewsDetailViewModel @Inject constructor(
    private val repository: INewsScrapingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<NewsDetailUiState>(NewsDetailUiState.Loading)
    val uiState: StateFlow<NewsDetailUiState> = _uiState.asStateFlow()

    fun loadDetail(websiteId: String, link: String) {
        viewModelScope.launch {
            _uiState.value = NewsDetailUiState.Loading
            try {
                val detail = repository.getNewsDetail(websiteId, link)
                _uiState.value = NewsDetailUiState.Success(detail)
            } catch (e: Exception) {
                _uiState.value = NewsDetailUiState.Error(e.localizedMessage ?: "Không thể tải chi tiết bài viết")
            }
        }
    }
}
