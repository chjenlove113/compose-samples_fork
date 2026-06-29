package com.app.tintuccongnghe.showHomeChild

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.tintuccongnghe.domain.models.ShowHomeDataModel
import com.app.tintuccongnghe.domain.usecases.ShowHomeUseCase
import com.app.tintuccongnghe.domain.util.DispatcherProvider
import com.app.tintuccongnghe.base.UiState
import com.app.tintuccongnghe.showHome.ItemDetailSite
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

import javax.inject.Inject;

@HiltViewModel(assistedFactory = ShowHomeChildViewModel.Factory::class)
class ShowHomeChildViewModel @AssistedInject constructor(
    private val showHomeUseCase:ShowHomeUseCase,
    private val dispatcherProvider:DispatcherProvider,
    @Assisted val navKey: ItemDetailSite
) : ViewModel(){

    @AssistedFactory
    interface Factory {
        fun create(navKey: ItemDetailSite): ShowHomeChildViewModel
    }

    private val _uiState = MutableStateFlow<UiState<ShowHomeDataModel>>(UiState.Loading)
            val uiState: MutableStateFlow<UiState<ShowHomeDataModel>> = _uiState

    init {
        fetchShowHome()
    }

    fun fetchShowHome(pageNumber: Int = 1, siteSlug: String = navKey.slug.slug, userId: String = "") {
        viewModelScope.launch(dispatcherProvider.main) {
            showHomeUseCase.invoke(pageNumber, siteSlug, userId).flowOn(dispatcherProvider.io)
                    .catch { e -> _uiState.value = UiState.Error(e.toString()) }
                .collect { _uiState.value = UiState.Success(it) }
        }
    }
}
