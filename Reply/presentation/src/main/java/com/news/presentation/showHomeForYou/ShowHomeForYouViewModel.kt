package com.news.presentation.showHomeForYou

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.news.domain.models.ShowHomeDataModel
import com.news.domain.usecases.GetAuthInfoUseCase
import com.news.domain.usecases.GetViewModeUseCase
import com.news.domain.usecases.SetViewModeUseCase
import com.news.domain.usecases.ShowHomeUseCase
import com.news.domain.util.DispatcherProvider
import com.news.presentation.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShowHomeForYouViewModel @Inject constructor(
    private val showHomeUseCase: ShowHomeUseCase,
    private val dispatcherProvider: DispatcherProvider,
    private val getViewModeUseCase: GetViewModeUseCase,
    private val setViewModeUseCase: SetViewModeUseCase,
    private val getAuthInfoUseCase: GetAuthInfoUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow<UiState<ShowHomeDataModel>>(UiState.Loading)
    val uiState: MutableStateFlow<UiState<ShowHomeDataModel>> = _uiState

    val viewMode: StateFlow<String> = getViewModeUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "list"
        )

    init {
        viewModelScope.launch {
            getAuthInfoUseCase().collect { auth ->
                fetchShowHome(auth?.IdentityId ?: "")
            }
        }
    }

    fun fetchShowHome(userId: String = "") {
        viewModelScope.launch(dispatcherProvider.main) {
            showHomeUseCase.invoke(userId = userId).flowOn(dispatcherProvider.io)
                .catch { e -> _uiState.value = UiState.Error(e.toString()) }
                .collect { _uiState.value = UiState.Success(it) }
        }
    }

    fun setViewMode(mode: String) {
        viewModelScope.launch {
            setViewModeUseCase(mode)
        }
    }
}
