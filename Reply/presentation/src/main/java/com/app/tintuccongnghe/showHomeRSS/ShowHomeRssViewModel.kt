package com.app.tintuccongnghe.showHomeRSS

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.tintuccongnghe.data.local.AppDatabase
import com.app.tintuccongnghe.data.local.entities.RssItemEntity
import com.app.tintuccongnghe.data.local.entities.toDomain
import com.app.tintuccongnghe.domain.models.AppUserSite
import com.app.tintuccongnghe.domain.usecases.GetAuthInfoUseCase
import com.app.tintuccongnghe.utils.AppContants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ShowHomeRssUiState(
    val sites: List<AppUserSite> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedItem: RssItemEntity? = null
)

@HiltViewModel
class ShowHomeRssViewModel @Inject constructor(
    private val appDatabase: AppDatabase,
    private val getAuthInfoUseCase: GetAuthInfoUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ShowHomeRssUiState())
    val uiState: StateFlow<ShowHomeRssUiState> = _uiState.asStateFlow()

    init {
        loadSites()
    }

    private fun loadSites() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            getAuthInfoUseCase().collect { auth ->
                if (auth != null) {
                    appDatabase.appUserSiteDao().getAppUserSites(auth.IdentityId, AppContants.app_Id)
                        .map { entities -> entities.map { it.toDomain() } }
                        .collect { sites ->
                            _uiState.update { it.copy(sites = sites, isLoading = false) }
                        }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Please login to view RSS feeds") }
                }
            }
        }
    }

    fun setSelectedRssItem(item: RssItemEntity?) {
        _uiState.update { it.copy(selectedItem = item) }
    }
}
