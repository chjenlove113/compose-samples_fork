package com.app.tintuccongnghe.showHomeRSS

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.app.tintuccongnghe.data.local.AppDatabase
import com.app.tintuccongnghe.data.local.entities.RssItemEntity
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*

data class ShowHomeRssChildUiState(
    val isLoading: Boolean = false
)

@HiltViewModel(assistedFactory = ShowHomeRssChildViewModel.Factory::class)
class ShowHomeRssChildViewModel @AssistedInject constructor(
    @Assisted private val siteId: Int,
    @Assisted("siteGroup") private val siteGroup: String,
    @Assisted("siteKind") private val siteKind: String,
    private val appDatabase: AppDatabase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ShowHomeRssChildUiState())
    val uiState: StateFlow<ShowHomeRssChildUiState> = _uiState.asStateFlow()

    val rssItemsPagingData: Flow<PagingData<RssItemEntity>> = Pager(
        config = PagingConfig(
            pageSize = 10,
            enablePlaceholders = false
        ),
        pagingSourceFactory = { appDatabase.rssItemDao().getRssItemsForSitePaging(siteId, siteGroup, siteKind) }
    ).flow.cachedIn(viewModelScope)

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted siteId: Int,
            @Assisted("siteGroup") siteGroup: String,
            @Assisted("siteKind") siteKind: String
        ): ShowHomeRssChildViewModel
    }
}
