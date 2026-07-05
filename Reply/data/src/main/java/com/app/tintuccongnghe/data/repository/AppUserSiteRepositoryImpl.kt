package com.app.tintuccongnghe.data.repository

import com.app.tintuccongnghe.data.api.AppUserSiteService
import com.app.tintuccongnghe.data.local.AppDatabase
import com.app.tintuccongnghe.data.local.entities.toDomain
import com.app.tintuccongnghe.data.local.entities.toEntity
import com.app.tintuccongnghe.domain.models.AppUserSite
import com.app.tintuccongnghe.domain.models.AppUserSiteCopyRequest
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
        
        val remoteKeys = remoteList.map { Triple(it.Id, it.GROUP, it.Kind) }.toSet()
        
        // 1. Delete local items that are no longer on the server
        localList.forEach { local ->
            if (Triple(local.Id, local.GROUP, local.Kind) !in remoteKeys) {
                dao.delete(local)
            }
        }
        
        // 2. Sync Server data to Local (Insert or Update)
        remoteList.forEach { remote ->
            // Ensure request identifiers are set on the remote objects before conversion
            remote.IdentityId = request.IdentityId
            remote.AppIdEncrypt = request.AppIdEncrypt
            
            val existing = localList.find { 
                it.Id == remote.Id && it.GROUP == remote.GROUP && it.Kind == remote.Kind 
            }
            
            if (existing != null) {
                // Update existing item, preserving local metadata like sync times and item counts
                val updatedEntity = remote.toEntity().copy(
                    lastRefreshTime = existing.lastRefreshTime,
                    nextRefreshTime = existing.nextRefreshTime,
                    itemCount = existing.itemCount
                )
                dao.update(updatedEntity)
            } else {
                // New item found on server, insert into local database
                dao.insert(remote.toEntity())
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
        // Delete locally first for immediate UI update and clean local state
        dao.deleteById(request.Id, request.GROUP, request.Kind)
        // Then notify the server
        return service.deleteAppUserSite(request)
    }

    override suspend fun copyAppUserSite(request: AppUserSiteCopyRequest): AppUserSiteCreateResponse {
        return service.copyAppUserSite(request)
    }
}
