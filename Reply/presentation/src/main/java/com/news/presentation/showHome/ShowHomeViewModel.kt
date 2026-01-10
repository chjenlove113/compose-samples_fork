package com.news.presentation.showHome

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.news.domain.models.ShowHomeDataModel
import com.news.domain.usecases.ShowHomeUseCase
import com.news.domain.util.DispatcherProvider
import com.news.presentation.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@HiltViewModel
class ShowHomeViewModel @Inject constructor(
    private val showHomeUseCase: ShowHomeUseCase,
    private val dispatcherProvider: DispatcherProvider
) : ViewModel() {
    private val _uiState = MutableStateFlow<UiState<ShowHomeDataModel>>(UiState.Loading)
    val uiState: MutableStateFlow<UiState<ShowHomeDataModel>> = _uiState

    init {
        fetchShowHome()
    }

    fun fetchShowHome() {
        viewModelScope.launch(dispatcherProvider.main) {
            showHomeUseCase.invoke().flowOn(dispatcherProvider.io)
                .catch { e -> _uiState.value = UiState.Error(e.toString()) }
                .collect { _uiState.value = UiState.Success(it) }
        }
    }
}