package com.news.presentation.showHome

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.news.domain.models.News
import com.news.domain.usecases.GetAuthInfoUseCase
import com.news.domain.usecases.ShowHomeChildPagingUseCase
import com.news.domain.util.DispatcherProvider
import com.news.presentation.base.UiState
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = ShowHomeChildPagingViewModel.Factory::class )
class ShowHomeChildPagingViewModel @AssistedInject constructor(
    private val showHomeChildPagingUseCase: ShowHomeChildPagingUseCase,
    private val dispatcherProvider: DispatcherProvider,
    @Assisted val navKey: ItemDetailSite,
    private val getAuthInfoUseCase: GetAuthInfoUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow<UiState<PagingData<News>>>(UiState.Loading)
    val uiState: MutableStateFlow<UiState<PagingData<News>>> = _uiState

    @AssistedFactory
    interface Factory{
        fun create(navKey: ItemDetailSite): ShowHomeChildPagingViewModel
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val items: Flow<PagingData<News>> = getAuthInfoUseCase().flatMapLatest { auth ->
        showHomeChildPagingUseCase.invoke(1, navKey.slug.Key, navKey.slug.slug, auth?.IdentityId ?: "")
    }.cachedIn(viewModelScope)

    init {
        viewModelScope.launch {
            getAuthInfoUseCase().collect { auth ->
                fetchShowHomeChildPaging(1, navKey.slug.Key, navKey.slug.slug, auth?.IdentityId ?: "")
            }
        }
    }

    fun fetchShowHomeChildPaging(pageNumber: Int = 1, site_Slug: String = "", cat_Slug: String = "", userId: String = "") {
        viewModelScope.launch(dispatcherProvider.main) {
            showHomeChildPagingUseCase.invoke(pageNumber, site_Slug, cat_Slug, userId).flowOn(dispatcherProvider.io).cachedIn(viewModelScope)
                .catch { e -> _uiState.value = UiState.Error(e.toString()) }
                .collect { _uiState.value = UiState.Success(it) }

        }
    }
}