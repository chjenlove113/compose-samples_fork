package com.app.tintuccongnghe.account

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.tintuccongnghe.data.local.AppDatabase
import com.app.tintuccongnghe.data.local.entities.News_Site
import com.app.tintuccongnghe.data.local.entities.RssItemEntity
import com.app.tintuccongnghe.domain.models.AppUserSite
import com.app.tintuccongnghe.domain.models.AppUserSiteCopyRequest
import com.app.tintuccongnghe.domain.models.AppUserSiteRequest
import com.app.tintuccongnghe.domain.models.AppUserSiteCreateRequest
import com.app.tintuccongnghe.domain.usecases.GetAppUserSiteListUseCase
import com.app.tintuccongnghe.domain.usecases.UpdateAppUserSiteUseCase
import com.app.tintuccongnghe.domain.usecases.GetAuthInfoUseCase
import com.app.tintuccongnghe.domain.usecases.CreateOrUpdateAppUserSiteUseCase
import com.app.tintuccongnghe.domain.usecases.DeleteAppUserSiteUseCase
import com.app.tintuccongnghe.domain.usecases.CopyAppUserSiteUseCase
import com.app.tintuccongnghe.domain.usecases.RefreshAppUserSiteListUseCase
import com.app.tintuccongnghe.notifications.RssSyncLiveUpdate
import com.app.tintuccongnghe.utils.AppContants
import com.rometools.rome.feed.synd.SyndFeed
import com.rometools.rome.io.SyndFeedInput
import com.rometools.rome.io.XmlReader
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
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
import javax.inject.Inject

data class AppUserSiteUiState(
    val isLoading: Boolean = false,
    val syncingSiteIds: Set<Int> = emptySet(),
    val sites: Map<String, List<AppUserSite>> = emptyMap(),
    val sampleSites: List<News_Site> = emptyList(),
    val error: String? = null,
    val isLoggedIn: Boolean = false,
    val hasActiveSubscription: Boolean = false,
    val hasCloudStorageSubscription: Boolean = false,
    val rssLimit: Int = 5
)

@HiltViewModel
class AppUserSiteViewModel @Inject constructor(
    private val appDatabase: AppDatabase,
    private val okHttpClient: OkHttpClient,
    private val getAppUserSiteListUseCase: GetAppUserSiteListUseCase,
    private val refreshAppUserSiteListUseCase: RefreshAppUserSiteListUseCase,
    private val updateAppUserSiteUseCase: UpdateAppUserSiteUseCase,
    private val getAuthInfoUseCase: GetAuthInfoUseCase,
    private val createOrUpdateAppUserSiteUseCase: CreateOrUpdateAppUserSiteUseCase,
    private val deleteAppUserSiteUseCase: DeleteAppUserSiteUseCase,
    private val copyAppUserSiteUseCase: CopyAppUserSiteUseCase,
    private val rssSyncLiveUpdate: RssSyncLiveUpdate,
    private val billingManager: BillingManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AppUserSiteUiState())
    val uiState: StateFlow<AppUserSiteUiState> = _uiState.asStateFlow()

    private var currentIdentityId: String = ""
    private var dataCollectionJob: Job? = null

    init {
        viewModelScope.launch {
            appDatabase.newsSiteDao().getAll().collect { samples ->
                _uiState.update { it.copy(sampleSites = samples) }
            }
        }
        viewModelScope.launch {
            billingManager.hasWebsiteLimitSubscription.collect { active ->
                val limit = if (active) 35 else 5
                _uiState.update { it.copy(hasActiveSubscription = active, rssLimit = limit) }
            }
        }
        viewModelScope.launch {
            billingManager.hasCloudStorageSubscription.collect { active ->
                _uiState.update { it.copy(hasCloudStorageSubscription = active) }
                // Re-initialize data with effective identity ID based on cloud subscription status
                if (currentIdentityId.isNotEmpty()) {
                    initData(currentIdentityId)
                }
            }
        }
        viewModelScope.launch {
            getAuthInfoUseCase().collect { auth ->
                _uiState.update { it.copy(isLoggedIn = auth != null) }
                if (auth != null) {
                    initData(auth.IdentityId)
                } else {
                    initData("local_user")
                }
            }
        }
    }

    private fun initData(identityId: String) {
        currentIdentityId = identityId
        val effectiveId = if (_uiState.value.hasCloudStorageSubscription) identityId else "local_user"
        val request = AppUserSiteRequest(effectiveId, "UserIdEncrypt", AppContants.app_Id)
        observeSites(request)
        loadSites(request)
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
                refreshAppUserSiteListUseCase(request, force = false)
                _uiState.update { it.copy(isLoading = false, error = null) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.localizedMessage) }
            }
        }
    }

    fun retryLoad() {
        if (currentIdentityId.isNotEmpty()) {
            val effectiveId = if (_uiState.value.hasCloudStorageSubscription) currentIdentityId else "local_user"
            loadSites(AppUserSiteRequest(effectiveId, "UserIdEncrypt", AppContants.app_Id))
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun syncData() {
        if (!_uiState.value.hasCloudStorageSubscription) {
            _uiState.update { it.copy(error = "Bạn chưa mua gói Cloud Storage. Không thể đồng bộ lên cloud.") }
            return
        }
        if (currentIdentityId.isNotEmpty() && currentIdentityId != "local_user") {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true, error = null) }
                try {
                    refreshAppUserSiteListUseCase(AppUserSiteRequest(currentIdentityId, "UserIdEncrypt", AppContants.app_Id), force = true)
                    _uiState.update { it.copy(isLoading = false, error = "Synced successfully with Firestore") }
                } catch (e: Exception) {
                    _uiState.update { it.copy(isLoading = false, error = e.localizedMessage ?: "Sync failed") }
                }
            }
        } else {
            _uiState.update { it.copy(error = "Vui lòng đăng nhập tài khoản để đồng bộ cloud.") }
        }
    }

    fun buySubscription(activity: Activity, productId: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = billingManager.launchBillingFlow(activity, productId)
            onResult(success)
        }
    }

    fun createOrUpdateSite(id: Int, name: String, url: String, icon: String, kind: String, group: String, isActive: Boolean, otherCanSee: Boolean) {
        // User sites always count. Common sites count only when IsActive == true.
        val rssSiteCount = _uiState.value.sites.values.flatten().count { it.GROUP == "1" && (it.IdentityId != "common" || it.IsActive) }
        val limit = _uiState.value.rssLimit

        if (id == 0) {
            if (rssSiteCount >= limit) {
                _uiState.update { it.copy(error = "You have reached the limit of $limit RSS sites. Upgrade limit subscription for 35 sites.") }
                return
            }
        } else {
            // Edit check: if subscription expired and user has > 5 user-created sites, restrict editing
            if (!_uiState.value.hasActiveSubscription && rssSiteCount > 5) {
                _uiState.update { it.copy(error = "Your website limit subscription has expired. Please renew to edit RSS sites.") }
                return
            }
        }

        val effectiveId = if (_uiState.value.hasCloudStorageSubscription) currentIdentityId else "local_user"

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
                    IdentityId = effectiveId,
                    AppIdEncrypt = AppContants.app_Id
                )
                val response = createOrUpdateAppUserSiteUseCase(request)
                if (response.Id > 0 || response.Mess == "") {
                    // Refresh the list
                    val reqId = if (_uiState.value.hasCloudStorageSubscription) currentIdentityId else "local_user"
                    loadSites(AppUserSiteRequest(reqId, "UserIdEncrypt", AppContants.app_Id))
                } else {
                    _uiState.update { it.copy(isLoading = false, error = response.Mess) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.localizedMessage) }
            }
        }
    }

    fun deleteSite(id: Int, name: String, url: String, icon: String, kind: String, group: String, isActive: Boolean, otherCanSee: Boolean) {
        val effectiveId = if (_uiState.value.hasCloudStorageSubscription) currentIdentityId else "local_user"
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
                    IdentityId = effectiveId,
                    AppIdEncrypt = AppContants.app_Id
                )
                val response = deleteAppUserSiteUseCase(request)
                if (response.Id > 0 || response.Mess == "") {
                    val reqId = if (_uiState.value.hasCloudStorageSubscription) currentIdentityId else "local_user"
                    loadSites(AppUserSiteRequest(reqId, "UserIdEncrypt", AppContants.app_Id))
                } else {
                    _uiState.update { it.copy(isLoading = false, error = response.Mess) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.localizedMessage) }
            }
        }
    }

    fun copySite(id: Int) {
        val effectiveId = if (_uiState.value.hasCloudStorageSubscription) currentIdentityId else "local_user"
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val request = AppUserSiteCopyRequest(
                    IdentityId = effectiveId,
                    Id = id
                )
                val response = copyAppUserSiteUseCase(request)
                if (response.Id > 0 || response.Mess == "") {
                    initData(currentIdentityId)
                } else {
                    _uiState.update { it.copy(isLoading = false, error = response.Mess) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.localizedMessage) }
            }
        }
    }

    fun toggleActive(site: AppUserSite, isChecked: Boolean) {
        viewModelScope.launch {
            val effectiveId = if (_uiState.value.hasCloudStorageSubscription) currentIdentityId else "local_user"
            site.IdentityId = effectiveId
            site.AppIdEncrypt = AppContants.app_Id

            val updatedSite = if (!isChecked) {
                appDatabase.rssItemDao().deleteItemsForSite(site.Id, site.GROUP, site.Kind)
                site.copy(IsActive = false, itemCount = 0)
            } else {
                site.copy(IsActive = true)
            }

            updateAppUserSiteUseCase(updatedSite)

            if (isChecked && updatedSite.GROUP == "1" && updatedSite.Url.isNotEmpty()) {
                syncSite(updatedSite)
            }
        }
    }

    fun syncSite(site: AppUserSite) {
        viewModelScope.launch {
            _uiState.update { it.copy(syncingSiteIds = it.syncingSiteIds + site.Id) }
            val notificationSession = rssSyncLiveUpdate.start(
                totalFeeds = 1,
                feedName = site.Name,
                syncKey = site.Id
            )
            var newItemCount = 0

            try {
                withContext(Dispatchers.IO) {
                    if (site.GROUP == "1" && site.Url.isNotEmpty()) {
                        val syndFeed: SyndFeed = SyndFeedInput().build(XmlReader(okHttpClient.newCall(Request.Builder().url(site.Url).build()).execute().body!!.byteStream()))
                        val rssEntities = syndFeed.entries.map { entry ->
                            RssItemEntity(
                                title = entry.title ?: "",
                                link = entry.link ?: "",
                                description = entry.description?.value,
                                pubDate = entry.publishedDate?.time ?: System.currentTimeMillis(),
                                siteId = site.Id,
                                siteGroup = site.GROUP,
                                siteKind = site.Kind
                            )
                        }
                        newItemCount = rssEntities.size
                        appDatabase.rssItemDao().insertAll(rssEntities)
                    }
                }

                val currentTime = System.currentTimeMillis()
                val updatedSite = site.copy(
                    lastRefreshTime = currentTime,
                    nextRefreshTime = currentTime + (30 * 60 * 1000),
                    itemCount = newItemCount
                )
                updateAppUserSiteUseCase(updatedSite)
                rssSyncLiveUpdate.complete(notificationSession, 1, newItemCount)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                rssSyncLiveUpdate.failed(notificationSession, e.localizedMessage ?: "Sync failed")
            } finally {
                _uiState.update { it.copy(syncingSiteIds = it.syncingSiteIds - site.Id) }
            }
        }
    }
}
