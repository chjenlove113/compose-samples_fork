package com.news.domain.repository

import com.news.domain.models.AppUserSite
import com.news.domain.models.AppUserSiteCreateRequest
import com.news.domain.models.AppUserSiteCreateResponse
import com.news.domain.models.AppUserSiteRequest

interface IAppUserSiteRepository {
    suspend fun getAppUserSiteList(request: AppUserSiteRequest): List<AppUserSite>
    suspend fun updateAppUserSite(site: AppUserSite): Boolean
    suspend fun createOrUpdateAppUserSite(request: AppUserSiteCreateRequest): AppUserSiteCreateResponse
}
