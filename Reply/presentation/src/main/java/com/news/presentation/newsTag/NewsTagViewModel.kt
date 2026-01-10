package com.news.presentation.newsTag

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.news.domain.models.NewsTag
import com.news.domain.usecases.GetNewsTagUseCase
import com.news.domain.util.DispatcherProvider
import com.news.presentation.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NewsTagViewModel @Inject constructor(
    private val useCase: GetNewsTagUseCase,
    private val dispatcherProvider: DispatcherProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<NewsTag>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<NewsTag>>> = _uiState

    init {
        fetchNewsTag()
    }

    fun fetchNewsTag() {
        viewModelScope.launch(dispatcherProvider.main) {
            useCase.invoke().flowOn(dispatcherProvider.io)
                .catch { e ->
                    Log.d("NewsTagViewModel", "fetchNewsTag: ${e.toString()}")
                    _uiState.value = UiState.Error(e.toString()) }
                .collect { _uiState.value = UiState.Success(it) }

        }
    }

}