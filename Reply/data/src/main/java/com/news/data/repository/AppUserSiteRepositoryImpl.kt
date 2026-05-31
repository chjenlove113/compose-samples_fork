package com.news.data.repository

import com.news.data.api.AppUserSiteService
import com.news.domain.models.AppUserSite
import com.news.domain.models.AppUserSiteCreateRequest
import com.news.domain.models.AppUserSiteCreateResponse
import com.news.domain.models.AppUserSiteRequest
import com.news.domain.repository.IAppUserSiteRepository
import javax.inject.Inject

class AppUserSiteRepositoryImpl @Inject constructor(
    private val service: AppUserSiteService
) : IAppUserSiteRepository {
    override suspend fun getAppUserSiteList(request: AppUserSiteRequest): List<AppUserSite> {
        return service.getAppUserSiteList(request)
    }

    override suspend fun updateAppUserSite(site: AppUserSite): Boolean {
        return try {
            service.updateAppUserSite(site)
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun createOrUpdateAppUserSite(request: AppUserSiteCreateRequest): AppUserSiteCreateResponse {
        return service.createOrUpdateAppUserSite(request)
    }
}
