package com.news.presentation.newsByTagId

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.news.domain.models.News
import com.news.domain.usecases.GetNewsByTagUseCase
import com.news.domain.util.DispatcherProvider
import com.news.presentation.base.UiState
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = NewsByTagViewModel.Factory::class)
class NewsByTagViewModel @AssistedInject constructor(
    @Assisted val tagSlug: String,
    private val getNewsByTagUseCase: GetNewsByTagUseCase,
    private val dispatcherProvider: DispatcherProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<News>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<News>>> = _uiState

    init {
        fetchNewsByTag()
    }

    fun fetchNewsByTag() {
        viewModelScope.launch(dispatcherProvider.main) {
            _uiState.value = UiState.Loading
            getNewsByTagUseCase(tagSlug, 1, 10)
                .flowOn(dispatcherProvider.io)
                .catch { e -> _uiState.value = UiState.Error(e.toString()) }
                .collect { _uiState.value = UiState.Success(it) }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(tagSlug: String): NewsByTagViewModel
    }
}
