package com.app.tintuccongnghe.data.repository

import com.app.tintuccongnghe.data.api.AppUserSiteService
import com.app.tintuccongnghe.data.local.AppDatabase
import com.app.tintuccongnghe.data.local.entities.toDomain
import com.app.tintuccongnghe.data.local.entities.toEntity
import com.app.tintuccongnghe.domain.models.AppUserSite
import com.app.tintuccongnghe.domain.models.AppUserSiteCreateRequest
import com.app.tintuccongnghe.domain.models.AppUserSiteCreateResponse
import com.app.tintuccongnghe.domain.models.AppUserSiteRequest
import com.app.tintuccongnghe.domain.repository.IAppUserSiteRepository
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
        val remoteList = service.getAppUserSiteList(request)
        val localList = dao.getAppUserSitesList(request.IdentityId, request.AppIdEncrypt)
        
        remoteList.forEach { remoteSite ->
            val exists = localList.any { it.Id == remoteSite.Id && it.GROUP == remoteSite.GROUP }
            if (!exists) {
                dao.insert(remoteSite.apply {
                    IdentityId = request.IdentityId
                    AppIdEncrypt = request.AppIdEncrypt
                }.toEntity())
            }
        }
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
