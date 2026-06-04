package com.news.domain.repository

import com.news.domain.models.AppUserSite
import com.news.domain.models.AppUserSiteCreateRequest
import com.news.domain.models.AppUserSiteCreateResponse
import com.news.domain.models.AppUserSiteRequest
import kotlinx.coroutines.flow.Flow

interface IAppUserSiteRepository {
    fun getAppUserSiteList(request: AppUserSiteRequest): Flow<List<AppUserSite>>
    suspend fun refreshAppUserSiteList(request: AppUserSiteRequest)
    suspend fun updateAppUserSite(site: AppUserSite): Boolean
    suspend fun createOrUpdateAppUserSite(request: AppUserSiteCreateRequest): AppUserSiteCreateResponse
    suspend fun deleteAppUserSite(request: AppUserSiteCreateRequest): AppUserSiteCreateResponse
}
