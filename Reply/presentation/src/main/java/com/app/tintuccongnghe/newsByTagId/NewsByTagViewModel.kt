package com.app.tintuccongnghe.newsByTagId

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.tintuccongnghe.data.local.IAppDbService
import com.app.tintuccongnghe.domain.models.News
import com.app.tintuccongnghe.domain.usecases.GetNewsByTagUseCase
import com.app.tintuccongnghe.domain.util.DispatcherProvider
import com.app.tintuccongnghe.base.UiState
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = NewsByTagViewModel.Factory::class)
class NewsByTagViewModel @AssistedInject constructor(
    @Assisted val tagSlug: String,
    private val getNewsByTagUseCase: GetNewsByTagUseCase,
    private val appDbService: IAppDbService,
    private val dispatcherProvider: DispatcherProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<News>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<News>>> = _uiState

    val isSaved: StateFlow<Boolean> = appDbService.isTagSaved(tagSlug)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

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

    fun toggleSaveTag() {
        viewModelScope.launch(dispatcherProvider.io) {
            if (isSaved.value) {
                appDbService.deleteTagSlug(tagSlug)
            } else {
                appDbService.saveTagSlug(tagSlug)
            }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(tagSlug: String): NewsByTagViewModel
    }
}
