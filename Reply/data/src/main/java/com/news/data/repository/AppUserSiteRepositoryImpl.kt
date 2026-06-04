package com.news.data.repository

import com.news.data.api.AppUserSiteService
import com.news.data.local.AppDatabase
import com.news.data.local.entities.toDomain
import com.news.data.local.entities.toEntity
import com.news.domain.models.AppUserSite
import com.news.domain.models.AppUserSiteCreateRequest
import com.news.domain.models.AppUserSiteCreateResponse
import com.news.domain.models.AppUserSiteRequest
import com.news.domain.repository.IAppUserSiteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AppUserSiteRepositoryImpl @Inject constructor(
    private val service: AppUserSiteService,
    private val database: AppDatabase
) : IAppUserSiteRepository {

    private val dao = database.appUserSiteDao()

    override fun getAppUserSiteList(request: AppUserSiteRequest): Flow<List<AppUserSite>> {
        return dao.getAppUserSites(request.IdentityId, request.AppIdEncrypt).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun refreshAppUserSiteList(request: AppUserSiteRequest) {
        val list = service.getAppUserSiteList(request)
        dao.refreshSites(
            request.IdentityId,
            request.AppIdEncrypt,
            list.map { it.apply { 
                IdentityId = request.IdentityId
                AppIdEncrypt = request.AppIdEncrypt
            }.toEntity() }
        )
    }

    override suspend fun updateAppUserSite(site: AppUserSite): Boolean {
        return try {
            service.updateAppUserSite(site)
            dao.insert(site.toEntity())
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun createOrUpdateAppUserSite(request: AppUserSiteCreateRequest): AppUserSiteCreateResponse {
        return service.createOrUpdateAppUserSite(request)
    }

    override suspend fun deleteAppUserSite(request: AppUserSiteCreateRequest): AppUserSiteCreateResponse {
        return service.deleteAppUserSite(request)
    }
}
