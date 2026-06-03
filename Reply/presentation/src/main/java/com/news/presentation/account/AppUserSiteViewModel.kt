package com.news.presentation.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.news.domain.models.AppUserSite
import com.news.domain.models.AppUserSiteRequest
import com.news.domain.models.AppUserSiteCreateRequest
import com.news.domain.usecases.GetAppUserSiteListUseCase
import com.news.domain.usecases.UpdateAppUserSiteUseCase
import com.news.domain.usecases.GetAuthInfoUseCase
import com.news.domain.usecases.CreateOrUpdateAppUserSiteUseCase
import com.news.domain.usecases.DeleteAppUserSiteUseCase
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
    val error: String? = null,
    val isLoggedIn: Boolean = false
)

@HiltViewModel
class AppUserSiteViewModel @Inject constructor(
    private val getAppUserSiteListUseCase: GetAppUserSiteListUseCase,
    private val updateAppUserSiteUseCase: UpdateAppUserSiteUseCase,
    private val getAuthInfoUseCase: GetAuthInfoUseCase,
    private val createOrUpdateAppUserSiteUseCase: CreateOrUpdateAppUserSiteUseCase,
    private val deleteAppUserSiteUseCase: DeleteAppUserSiteUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AppUserSiteUiState())
    val uiState: StateFlow<AppUserSiteUiState> = _uiState.asStateFlow()

    private var currentIdentityId: String = ""

    init {
        viewModelScope.launch {
            getAuthInfoUseCase().collect { auth ->
                _uiState.update { it.copy(isLoggedIn = auth != null) }
                auth?.let {
                    currentIdentityId = it.IdentityId
                    loadSites(AppUserSiteRequest(it.IdentityId, "UserIdEncrypt", AppContants.app_Id))
                } ?: run {
                    _uiState.update { it.copy(sites = emptyMap()) }
                }
            }
        }
    }

    fun loadSites(request: AppUserSiteRequest) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val list = getAppUserSiteListUseCase(request)
                // Filter out duplicates that could cause key collisions in LazyColumn
                val uniqueList = list.distinctBy { "${it.Kind}_${it.GROUP}_${it.Id}" }
                val grouped = uniqueList.groupBy { it.Kind }
                _uiState.update { it.copy(isLoading = false, sites = grouped) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.localizedMessage) }
            }
        }
    }

    fun createOrUpdateSite(id: Int, name: String, url: String, icon: String, kind: String, isActive: Boolean, otherCanSee: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val request = AppUserSiteCreateRequest(
                    Id = id,
                    Name = name,
                    Url = url,
                    Icon = icon,
                    Status = isActive,
                    OtherCanSee = otherCanSee,
                    Kind = kind,
                    IdentityId = currentIdentityId,
                    AppIdEncrypt = AppContants.app_Id
                )
                val response = createOrUpdateAppUserSiteUseCase(request)
                if (response.Id > 0 || response.Mess == "") {
                    // Refresh the list
                    loadSites(AppUserSiteRequest(currentIdentityId, "UserIdEncrypt", AppContants.app_Id))
                } else {
                    _uiState.update { it.copy(isLoading = false, error = response.Mess) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.localizedMessage) }
            }
        }
    }

    fun deleteSite(id: Int, name: String, url: String, icon: String, kind: String, isActive: Boolean, otherCanSee: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val request = AppUserSiteCreateRequest(
                    Id = id,
                    Name = name,
                    Url = url,
                    Icon = icon,
                    Status = isActive,
                    OtherCanSee = otherCanSee,
                    Kind = kind,
                    IdentityId = currentIdentityId,
                    AppIdEncrypt = AppContants.app_Id
                )
                val response = deleteAppUserSiteUseCase(request)
                if (response.Id > 0 || response.Mess == "") {
                    loadSites(AppUserSiteRequest(currentIdentityId, "UserIdEncrypt", AppContants.app_Id))
                } else {
                    _uiState.update { it.copy(isLoading = false, error = response.Mess) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.localizedMessage) }
            }
        }
    }

    fun toggleActive(site: AppUserSite) {
        viewModelScope.launch {
            site.IdentityId = currentIdentityId
            site.AppIdEncrypt = AppContants.app_Id
            val updatedSite = site.copy(IsActive = !site.IsActive)

            val success = updateAppUserSiteUseCase(updatedSite)
            if (success) {
                // Refresh local state
                _uiState.update { state ->
                    val newMap = state.sites.mapValues { entry ->
                        entry.value.map { if (it.Id == site.Id && it.GROUP == site.GROUP) updatedSite else it }
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
                        entry.value.map { if (it.Id == site.Id && it.GROUP == site.GROUP) site else it }
                    }
                    state.copy(sites = newMap)
                }
            }
        }
    }
}
