package com.news.presentation.components

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.news.domain.models.NewsTag
import com.news.domain.usecases.GetNewsDetailHtmlUseCase
import com.news.domain.usecases.GetNewsTagsUseCase
import com.news.domain.util.DispatcherProvider
import com.news.presentation.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NewsDetailViewModel @Inject constructor(
    private val getNewsDetailHtmlUseCase: GetNewsDetailHtmlUseCase,
    private val getNewsTagsUseCase: GetNewsTagsUseCase,
    private val dispatcherProvider: DispatcherProvider
) : ViewModel() {

    private val _htmlState = MutableStateFlow<UiState<String>>(UiState.Loading)
    val htmlState: StateFlow<UiState<String>> = _htmlState.asStateFlow()

    private val _tagsState = MutableStateFlow<UiState<List<NewsTag>>>(UiState.Loading)
    val tagsState: StateFlow<UiState<List<NewsTag>>> = _tagsState.asStateFlow()

    fun fetchHtml(link: String, cat: String, id: Int, html: String, slug: String) {
        viewModelScope.launch(dispatcherProvider.main) {
            _htmlState.value = UiState.Loading
            getNewsDetailHtmlUseCase(link, cat, id, html, slug)
                .flowOn(dispatcherProvider.io)
                .catch { e ->
                    _htmlState.value = UiState.Error(e.message ?: "Unknown error")
                    fun fetchTags(newsId: Int) {
        viewModelScope.launch(dispatcherProvider.main) {
            _tagsState.value = UiState.Loading
            getNewsTagsUseCase(newsId)
                .flowOn(dispatcherProvider.io)
                .catch { e ->
                    _tagsState.value = UiState.Error(e.message ?: "Unknown error")
                }
                .collect { result ->
                    result.fold(
                        onSuccess = {
                            _tagsState.value = UiState.Success(it)
                        },
                        onFailure = {
                            _tagsState.value = UiState.Error(it.message ?: "Unknown error")
                        }
                    )
                }
        }
    }
}
                .collect { result ->
                    result.fold(
                        onSuccess = {
                            _htmlState.value = UiState.Success(it)
                        },
                        onFailure = {
                            _htmlState.value = UiState.Error(it.message ?: "Unknown error")
                            fun fetchTags(newsId: Int) {
        viewModelScope.launch(dispatcherProvider.main) {
            _tagsState.value = UiState.Loading
            getNewsTagsUseCase(newsId)
                .flowOn(dispatcherProvider.io)
                .catch { e ->
                    _tagsState.value = UiState.Error(e.message ?: "Unknown error")
                }
                .collect { result ->
                    result.fold(
                        onSuccess = {
                            _tagsState.value = UiState.Success(it)
                        },
                        onFailure = {
                            _tagsState.value = UiState.Error(it.message ?: "Unknown error")
                        }
                    )
                }
        }
    }
}
                    )
                    fun fetchTags(newsId: Int) {
        viewModelScope.launch(dispatcherProvider.main) {
            _tagsState.value = UiState.Loading
            getNewsTagsUseCase(newsId)
                .flowOn(dispatcherProvider.io)
                .catch { e ->
                    _tagsState.value = UiState.Error(e.message ?: "Unknown error")
                }
                .collect { result ->
                    result.fold(
                        onSuccess = {
                            _tagsState.value = UiState.Success(it)
                        },
                        onFailure = {
                            _tagsState.value = UiState.Error(it.message ?: "Unknown error")
                        }
                    )
                }
        }
    }
}
            fun fetchTags(newsId: Int) {
        viewModelScope.launch(dispatcherProvider.main) {
            _tagsState.value = UiState.Loading
            getNewsTagsUseCase(newsId)
                .flowOn(dispatcherProvider.io)
                .catch { e ->
                    _tagsState.value = UiState.Error(e.message ?: "Unknown error")
                }
                .collect { result ->
                    result.fold(
                        onSuccess = {
                            _tagsState.value = UiState.Success(it)
                        },
                        onFailure = {
                            _tagsState.value = UiState.Error(it.message ?: "Unknown error")
                        }
                    )
                }
        }
    }
}
        fun fetchTags(newsId: Int) {
        viewModelScope.launch(dispatcherProvider.main) {
            _tagsState.value = UiState.Loading
            getNewsTagsUseCase(newsId)
                .flowOn(dispatcherProvider.io)
                .catch { e ->
                    _tagsState.value = UiState.Error(e.message ?: "Unknown error")
                }
                .collect { result ->
                    result.fold(
                        onSuccess = {
                            _tagsState.value = UiState.Success(it)
                        },
                        onFailure = {
                            _tagsState.value = UiState.Error(it.message ?: "Unknown error")
                        }
                    )
                }
        }
    }
}
    fun fetchTags(newsId: Int) {
        viewModelScope.launch(dispatcherProvider.main) {
            _tagsState.value = UiState.Loading
            getNewsTagsUseCase(newsId)
                .flowOn(dispatcherProvider.io)
                .catch { e ->
                    _tagsState.value = UiState.Error(e.message ?: "Unknown error")
                }
                .collect { result ->
                    result.fold(
                        onSuccess = {
                            _tagsState.value = UiState.Success(it)
                        },
                        onFailure = {
                            _tagsState.value = UiState.Error(it.message ?: "Unknown error")
                        }
                    )
                }
        }
    }
}
