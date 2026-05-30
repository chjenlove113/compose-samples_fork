package com.news.presentation.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.news.domain.models.AppUserSite
import com.news.domain.models.AppUserSiteRequest
import com.news.domain.usecases.GetAppUserSiteListUseCase
import com.news.domain.usecases.UpdateAppUserSiteUseCase
import com.news.domain.usecases.GetAuthInfoUseCase
import com.news.utils.AppContants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AppUserSiteUiState(
    val isLoading: Boolean = false,
    val sites: Map<String, List<AppUserSite>> = emptyMap(),
    val error: String? = null
)

@HiltViewModel
class AppUserSiteViewModel @Inject constructor(
    private val getAppUserSiteListUseCase: GetAppUserSiteListUseCase,
    private val updateAppUserSiteUseCase: UpdateAppUserSiteUseCase,
    private val getAuthInfoUseCase: GetAuthInfoUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AppUserSiteUiState())
    val uiState: StateFlow<AppUserSiteUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            getAuthInfoUseCase().collect { auth ->
                auth?.let {
                    loadSites(AppUserSiteRequest(it.IdentityId, "UserIdEncrypt", AppContants.app_Id))
                }
            }
        }
    }

    fun loadSites(request: AppUserSiteRequest) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val list = getAppUserSiteListUseCase(request)
                val grouped = list.groupBy { it.Kind }
                _uiState.update { it.copy(isLoading = false, sites = grouped) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.localizedMessage) }
            }
        }
    }

    fun toggleActive(site: AppUserSite) {
        viewModelScope.launch {
            val updatedSite = site.copy(IsActive = !site.IsActive)
            val success = updateAppUserSiteUseCase(updatedSite)
            if (success) {
                // Refresh local state
                _uiState.update { state ->
                    val newMap = state.sites.mapValues { entry ->
                        entry.value.map { if (it.Id == site.Id) updatedSite else it }
                    }
                    state.copy(sites = newMap)
                }
            }
        }
    }
    
    fun updateSite(site: AppUserSite) {
        viewModelScope.launch {
            val success = updateAppUserSiteUseCase(site)
            if (success) {
                _uiState.update { state ->
                    val newMap = state.sites.mapValues { entry ->
                        entry.value.map { if (it.Id == site.Id) site else it }
                    }
                    state.copy(sites = newMap)
                }
            }
        }
    }
}
