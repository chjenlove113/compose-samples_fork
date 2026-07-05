package com.app.tintuccongnghe.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.tintuccongnghe.data.local.AppDatabase
import com.app.tintuccongnghe.data.local.entities.RssItemEntity
import com.app.tintuccongnghe.data.local.entities.toEntity
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
import com.rometools.rome.feed.synd.SyndFeed
import com.rometools.rome.io.SyndFeedInput
import com.rometools.rome.io.XmlReader
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit
import javax.inject.Inject

data class AppUserSiteUiState(
    val isLoading: Boolean = false,
    val syncingSiteIds: Set<Int> = emptySet(),
    val sites: Map<String, List<AppUserSite>> = emptyMap(),
    val error: String? = null,
    val isLoggedIn: Boolean = false
)

@HiltViewModel
class AppUserSiteViewModel @Inject constructor(
    private val appDatabase: AppDatabase,
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

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun createOrUpdateSite(id: Int, name: String, url: String, icon: String, kind: String, group: String, isActive: Boolean, otherCanSee: Boolean) {
        if (id == 0) {
            val rssSiteCount = _uiState.value.sites.values.flatten().count { it.GROUP == "1" }
            if (rssSiteCount >= 10) {
                _uiState.update { it.copy(error = "You have reached the limit of 10 RSS sites.") }
                return
            }
        }

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
                    GROUP = group,
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

    fun deleteSite(id: Int, name: String, url: String, icon: String, kind: String, group: String, isActive: Boolean, otherCanSee: Boolean) {
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
                    GROUP = group,
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

    fun syncSite(site: AppUserSite) {
        viewModelScope.launch {
            _uiState.update { it.copy(syncingSiteIds = it.syncingSiteIds + site.Id) }
            withContext(Dispatchers.IO) {
                try {
                    if (site.GROUP == "1" && site.Url.isNotEmpty()) {
                        val client = OkHttpClient()
                        val request = Request.Builder().url(site.Url).build()
                        client.newCall(request).execute().use { response ->
                            val currentTime = System.currentTimeMillis()
                            val nextTime = currentTime + TimeUnit.MINUTES.toMillis(15)

                            if (response.isSuccessful) {
                                val input = SyndFeedInput()
                                val feed: SyndFeed = input.build(XmlReader(response.body!!.byteStream()))
                                val itemsToInsert = mutableListOf<RssItemEntity>()
                                val totalItems = feed.entries.size

                                val siteEntity = site.toEntity().copy(
                                    lastRefreshTime = currentTime,
                                    nextRefreshTime = nextTime,
                                    itemCount = totalItems
                                )
                                appDatabase.appUserSiteDao().update(siteEntity)

                                feed.entries.forEach { entry ->
                                    val link = entry.link ?: ""
                                    if (link.isNotEmpty()) {
                                        val rssItemDao = appDatabase.rssItemDao()
                                        val existingItem = rssItemDao.getItemByLink(link)
                                        if (existingItem != null) {
                                            rssItemDao.update(existingItem.copy(updDate = currentTime))
                                        } else {
                                            val contentValue = entry.contents?.firstOrNull()?.value
                                                ?: entry.modules?.filterIsInstance<com.rometools.rome.feed.module.DCModule>()?.firstOrNull()?.description
                                                ?: entry.description?.value

                                            itemsToInsert.add(
                                                RssItemEntity(
                                                    title = entry.title ?: "",
                                                    link = link,
                                                    description = entry.description?.value,
                                                    pubDate = entry.publishedDate?.time,
                                                    siteId = site.Id,
                                                    siteGroup = site.GROUP,
                                                    siteKind = site.Kind,
                                                    updDate = currentTime,
                                                    content = contentValue,
                                                    siteName = site.Name
                                                )
                                            )
                                        }
                                    }
                                }

                                if (itemsToInsert.isNotEmpty()) {
                                    appDatabase.rssItemDao().insertAll(itemsToInsert)
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            _uiState.update { it.copy(syncingSiteIds = it.syncingSiteIds - site.Id) }
        }
    }
}
