package com.news.presentation.showHomeRSS

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.news.data.local.AppDatabase
import com.news.data.local.entities.RssItemEntity
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ShowHomeRssChildUiState(
    val rssItems: List<RssItemEntity> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel(assistedFactory = ShowHomeRssChildViewModelFactory::class)
class ShowHomeRssChildViewModel @AssistedInject constructor(
    @Assisted("siteId") private val siteId: Int,
    @Assisted("siteGroup") private val siteGroup: String,
    @Assisted("siteKind") private val siteKind: String,
    private val appDatabase: AppDatabase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ShowHomeRssChildUiState())
    val uiState: StateFlow<ShowHomeRssChildUiState> = _uiState.asStateFlow()

    init {
        loadRssItems()
    }

    private fun loadRssItems() {
        viewModelScope.launch {
            appDatabase.rssItemDao().getRssItemsForSite(siteId, siteGroup, siteKind)
                .collect { items ->
                    _uiState.update { it.copy(rssItems = items) }
                }
        }
    }
}

@AssistedFactory
interface ShowHomeRssChildViewModelFactory {
    fun create(
        @Assisted("siteId") siteId: Int,
        @Assisted("siteGroup") siteGroup: String,
        @Assisted("siteKind") siteKind: String
    ): ShowHomeRssChildViewModel
}
