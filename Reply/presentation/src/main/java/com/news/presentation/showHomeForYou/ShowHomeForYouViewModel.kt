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

data class ShowHomeForYouUiState(
    val data: UiState<ShowHomeDataModel> = UiState.Loading,
    val isLoggedIn: Boolean = false
)

@HiltViewModel
class ShowHomeForYouViewModel @Inject constructor(
    private val showHomeUseCase: ShowHomeUseCase,
    private val dispatcherProvider: DispatcherProvider,
    private val getViewModeUseCase: GetViewModeUseCase,
    private val setViewModeUseCase: SetViewModeUseCase,
    private val getAuthInfoUseCase: GetAuthInfoUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(ShowHomeForYouUiState())
    val uiState: StateFlow<ShowHomeForYouUiState> = _uiState.asStateFlow()

    val viewMode: StateFlow<String> = getViewModeUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "list"
        )

    init {
        viewModelScope.launch {
            getAuthInfoUseCase().collect { auth ->
                _uiState.update { it.copy(isLoggedIn = auth != null) }
                if (auth != null) {
                    fetchShowHome(auth.IdentityId)
                }
            }
        }
    }

    fun fetchShowHome(userId: String = "") {
        viewModelScope.launch(dispatcherProvider.main) {
            _uiState.update { it.copy(data = UiState.Loading) }
            showHomeUseCase.invoke(userId = userId).flowOn(dispatcherProvider.io)
                .catch { e -> _uiState.update { it.copy(data = UiState.Error(e.toString())) } }
                .collect { data -> _uiState.update { it.copy(data = UiState.Success(data)) } }
        }
    }

    fun setViewMode(mode: String) {
        viewModelScope.launch {
            setViewModeUseCase(mode)
        }
    }
}
