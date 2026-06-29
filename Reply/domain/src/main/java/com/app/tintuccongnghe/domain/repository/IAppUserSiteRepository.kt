package com.app.tintuccongnghe.domain.repository

import com.app.tintuccongnghe.domain.models.AppUserSite
import com.app.tintuccongnghe.domain.models.AppUserSiteCreateRequest
import com.app.tintuccongnghe.domain.models.AppUserSiteCreateResponse
import com.app.tintuccongnghe.domain.models.AppUserSiteRequest
import kotlinx.coroutines.flow.Flow

interface IAppUserSiteRepository {
    fun getAppUserSiteList(request: AppUserSiteRequest): Flow<List<AppUserSite>>
    suspend fun refreshAppUserSiteList(request: AppUserSiteRequest)
    suspend fun updateAppUserSite(site: AppUserSite): Boolean
    suspend fun createOrUpdateAppUserSite(request: AppUserSiteCreateRequest): AppUserSiteCreateResponse
    suspend fun deleteAppUserSite(request: AppUserSiteCreateRequest): AppUserSiteCreateResponse
}
