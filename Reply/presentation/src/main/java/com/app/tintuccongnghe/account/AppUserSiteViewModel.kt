package com.app.tintuccongnghe.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.tintuccongnghe.domain.models.AppUserSite
import com.app.tintuccongnghe.domain.models.AppUserSiteRequest
import com.app.tintuccongnghe.domain.models.AppUserSiteCreateRequest
import com.app.tintuccongnghe.domain.usecases.GetAppUserSiteListUseCase
import com.app.tintuccongnghe.domain.usecases.UpdateAppUserSiteUseCase
import com.app.tintuccongnghe.domain.usecases.GetAuthInfoUseCase
import com.app.tintuccongnghe.domain.usecases.CreateOrUpdateAppUserSiteUseCase
import com.app.tintuccongnghe.domain.usecases.DeleteAppUserSiteUseCase
import com.app.tintuccongnghe.domain.usecases.RefreshAppUserSiteListUseCase
import com.app.tintuccongnghe.utils.AppContants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
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
    private val refreshAppUserSiteListUseCase: RefreshAppUserSiteListUseCase,
    private val updateAppUserSiteUseCase: UpdateAppUserSiteUseCase,
    private val getAuthInfoUseCase: GetAuthInfoUseCase,
    private val createOrUpdateAppUserSiteUseCase: CreateOrUpdateAppUserSiteUseCase,
    private val deleteAppUserSiteUseCase: DeleteAppUserSiteUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AppUserSiteUiState())
    val uiState: StateFlow<AppUserSiteUiState> = _uiState.asStateFlow()

    private var currentIdentityId: String = ""
    private var dataCollectionJob: Job? = null

    init {
        viewModelScope.launch {
            getAuthInfoUseCase().collect { auth ->
                _uiState.update { it.copy(isLoggedIn = auth != null) }
                auth?.let {
                    currentIdentityId = it.IdentityId
                    val request = AppUserSiteRequest(it.IdentityId, "UserIdEncrypt", AppContants.app_Id)
                    observeSites(request)
                    loadSites(request)
                } ?: run {
                    _uiState.update { it.copy(sites = emptyMap()) }
                    dataCollectionJob?.cancel()
                }
            }
        }
    }

    private fun observeSites(request: AppUserSiteRequest) {
        dataCollectionJob?.cancel()
        dataCollectionJob = viewModelScope.launch {
            getAppUserSiteListUseCase(request).collect { list ->
                // Filter out duplicates that could cause key collisions in LazyColumn
                val uniqueList = list.distinctBy { "${it.Kind}_${it.GROUP}_${it.Id}" }
                val grouped = uniqueList.groupBy { it.Kind }
                _uiState.update { it.copy(sites = grouped) }
            }
        }
    }

    fun loadSites(request: AppUserSiteRequest) {
        viewModelScope.launch {
            if (_uiState.value.sites.isEmpty()) {
                _uiState.update { it.copy(isLoading = true, error = null) }
            }
            try {
                refreshAppUserSiteListUseCase(request)
                _uiState.update { it.copy(isLoading = false, error = null) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.localizedMessage) }
            }
        }
    }

    fun retryLoad() {
        if (currentIdentityId.isNotEmpty()) {
            loadSites(AppUserSiteRequest(currentIdentityId, "UserIdEncrypt", AppContants.app_Id))
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
            updateAppUserSiteUseCase(updatedSite)
        }
    }
    
    fun updateSite(site: AppUserSite) {
        viewModelScope.launch {
            updateAppUserSiteUseCase(site)
        }
    }
}
