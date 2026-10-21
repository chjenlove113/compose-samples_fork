package com.app.tintuccongnghe.data.repository

import com.app.tintuccongnghe.data.local.AppDatabase
import com.app.tintuccongnghe.data.local.entities.toDomain
import com.app.tintuccongnghe.data.local.entities.toEntity
import com.app.tintuccongnghe.domain.models.AppUserSite
import com.app.tintuccongnghe.domain.models.AppUserSiteCopyRequest
import com.app.tintuccongnghe.domain.models.AppUserSiteCreateRequest
import com.app.tintuccongnghe.domain.models.AppUserSiteCreateResponse
import com.app.tintuccongnghe.domain.models.AppUserSiteRequest
import com.app.tintuccongnghe.domain.repository.IAppUserSiteRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AppUserSiteRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val database: AppDatabase
) : IAppUserSiteRepository {

    private val dao = database.appUserSiteDao()

    override fun getAppUserSiteList(request: AppUserSiteRequest): Flow<List<AppUserSite>> {
        return dao.getAppUserSitesWithCommon(request.IdentityId, request.AppIdEncrypt).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun refreshAppUserSiteList(request: AppUserSiteRequest) {
        try {
            // 1. PUSH: Upload unsynced local items to Firestore server ONLY if logged in
            if (request.IdentityId != "local_user") {
                val localListForPush = dao.getAllSites()
                val unsyncedItems = localListForPush.filter { !it.isSynced && it.IdentityId == request.IdentityId }

                unsyncedItems.forEach { local ->
                    try {
                        val querySnapshot = firestore.collection("user_sites")
                            .whereEqualTo("IdentityId", local.IdentityId)
                            .whereEqualTo("Id", local.Id)
                            .get()
                            .await()

                        val siteMap = mapOf(
                            "Id" to local.Id,
                            "Key" to local.Key,
                            "Name" to local.Name,
                            "TextColor" to local.TextColor,
                            "BackgroundColor" to local.BackgroundColor,
                            "Url" to local.Url,
                            "IsActive" to local.IsActive,
                            "Kind" to local.Kind,
                            "GROUP" to local.GROUP,
                            "Stt" to local.Stt,
                            "LimitEdit" to local.LimitEdit,
                            "AllowEdit" to local.AllowEdit,
                            "OtherCanSee" to local.OtherCanSee,
                            "IdentityId" to local.IdentityId,
                            "AppIdEncrypt" to local.AppIdEncrypt
                        )

                        if (!querySnapshot.isEmpty) {
                            for (doc in querySnapshot.documents) {
                                doc.reference.set(siteMap, SetOptions.merge()).await()
                            }
                        } else {
                            firestore.collection("user_sites").add(siteMap).await()
                        }

                        dao.update(local.copy(isSynced = true))
                    } catch (_: Exception) {}
                }
            }

            // 2. PULL: Fetch remote items from Firestore (always fetch "common", and user specific if logged in)
            val remoteItems = mutableListOf<AppUserSite>()

            try {
                val commonSnapshot = firestore.collection("user_sites")
                    .whereEqualTo("IdentityId", "common")
                    .get()
                    .await()

                val commonList = commonSnapshot.documents.mapNotNull { doc ->
                    try {
                        AppUserSite(
                            Id = (doc.getLong("Id") ?: doc.getLong("id") ?: 0).toInt(),
                            Key = doc.getString("Key") ?: doc.getString("key") ?: "",
                            Name = doc.getString("Name") ?: doc.getString("name") ?: "",
                            TextColor = doc.getString("TextColor") ?: doc.getString("textColor"),
                            BackgroundColor = doc.getString("BackgroundColor") ?: doc.getString("backgroundColor"),
                            Url = doc.getString("Url") ?: doc.getString("url") ?: "",
                            IsActive = doc.getBoolean("IsActive") ?: doc.getBoolean("isActive") ?: true,
                            Kind = doc.getString("Kind") ?: doc.getString("kind") ?: "",
                            GROUP = doc.getString("GROUP") ?: doc.getString("group") ?: "",
                            Stt = (doc.getLong("Stt") ?: doc.getLong("stt") ?: 0).toInt(),
                            LimitEdit = doc.getBoolean("LimitEdit") ?: doc.getBoolean("limitEdit") ?: false,
                            AllowEdit = doc.getBoolean("AllowEdit") ?: doc.getBoolean("allowEdit") ?: true,
                            OtherCanSee = doc.getBoolean("OtherCanSee") ?: doc.getBoolean("otherCanSee") ?: false,
                            IdentityId = "common",
                            AppIdEncrypt = doc.getString("AppIdEncrypt") ?: doc.getString("appIdEncrypt") ?: request.AppIdEncrypt,
                            isSynced = true
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
                remoteItems.addAll(commonList)
            } catch (_: Exception) {}

            if (request.IdentityId != "local_user") {
                try {
                    val userSnapshot = firestore.collection("user_sites")
                        .whereEqualTo("IdentityId", request.IdentityId)
                        .get()
                        .await()

                    val userList = userSnapshot.documents.mapNotNull { doc ->
                        try {
                            AppUserSite(
                                Id = (doc.getLong("Id") ?: doc.getLong("id") ?: 0).toInt(),
                                Key = doc.getString("Key") ?: doc.getString("key") ?: "",
                                Name = doc.getString("Name") ?: doc.getString("name") ?: "",
                                TextColor = doc.getString("TextColor") ?: doc.getString("textColor"),
                                BackgroundColor = doc.getString("BackgroundColor") ?: doc.getString("backgroundColor"),
                                Url = doc.getString("Url") ?: doc.getString("url") ?: "",
                                IsActive = doc.getBoolean("IsActive") ?: doc.getBoolean("isActive") ?: true,
                                Kind = doc.getString("Kind") ?: doc.getString("kind") ?: "",
                                GROUP = doc.getString("GROUP") ?: doc.getString("group") ?: "",
                                Stt = (doc.getLong("Stt") ?: doc.getLong("stt") ?: 0).toInt(),
                                LimitEdit = doc.getBoolean("LimitEdit") ?: doc.getBoolean("limitEdit") ?: false,
                                AllowEdit = doc.getBoolean("AllowEdit") ?: doc.getBoolean("allowEdit") ?: true,
                                OtherCanSee = doc.getBoolean("OtherCanSee") ?: doc.getBoolean("otherCanSee") ?: false,
                                IdentityId = request.IdentityId,
                                AppIdEncrypt = doc.getString("AppIdEncrypt") ?: doc.getString("appIdEncrypt") ?: request.AppIdEncrypt,
                                isSynced = true
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                    remoteItems.addAll(userList)
                } catch (_: Exception) {}
            }

            val localList = dao.getAllSites()

            remoteItems.forEach { remote ->
                val existingLocal = if (request.IdentityId != "local_user" && remote.IdentityId == request.IdentityId) {
                    localList.find { it.IdentityId == request.IdentityId && it.Key == remote.Key }
                } else {
                    localList.find { it.Key == remote.Key }
                }

                if (existingLocal == null) {
                    val targetIdentityId = if (remote.IdentityId == request.IdentityId) request.IdentityId else "common"
                    dao.insert(remote.toEntity().copy(IdentityId = targetIdentityId))
                } else {
                    if (existingLocal.isSynced) {
                        val updatedEntity = remote.toEntity().copy(
                            IdentityId = existingLocal.IdentityId,
                            GROUP = existingLocal.GROUP,
                            lastRefreshTime = existingLocal.lastRefreshTime,
                            nextRefreshTime = existingLocal.nextRefreshTime,
                            itemCount = existingLocal.itemCount,
                            isSynced = true
                        )
                        dao.update(updatedEntity)
                    }
                }
            }
        } catch (e: Exception) {
            // Offline fallback
        }
    }

    override suspend fun updateAppUserSite(site: AppUserSite): Boolean {
        return try {
            val isLoggedIn = site.IdentityId != "local_user"
            // CRUD operations only modify local database first (isSynced = false if logged in)
            val entity = site.toEntity().copy(isSynced = !isLoggedIn)
            dao.insert(entity)
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun createOrUpdateAppUserSite(request: AppUserSiteCreateRequest): AppUserSiteCreateResponse {
        return try {
            val siteId = if (request.Id == 0) (System.currentTimeMillis() % Int.MAX_VALUE).toInt() else request.Id
            val isLoggedIn = request.IdentityId != "local_user"

            val site = AppUserSite(
                Id = siteId,
                Key = request.Name,
                Name = request.Name,
                TextColor = null,
                BackgroundColor = null,
                Url = request.Url,
                IsActive = request.Status,
                Kind = request.Kind,
                GROUP = request.GROUP,
                Stt = 0,
                LimitEdit = false,
                AllowEdit = true,
                OtherCanSee = request.OtherCanSee,
                IdentityId = request.IdentityId,
                AppIdEncrypt = request.AppIdEncrypt,
                isSynced = !isLoggedIn
            )
            dao.insert(site.toEntity())
            AppUserSiteCreateResponse(Id = siteId, Mess = "Success")
        } catch (e: Exception) {
            AppUserSiteCreateResponse(Id = request.Id, Mess = e.message ?: "Error")
        }
    }

    override suspend fun deleteAppUserSite(request: AppUserSiteCreateRequest): AppUserSiteCreateResponse {
        dao.deleteById(request.Id)

        if (request.IdentityId != "local_user") {
            try {
                val querySnapshot = firestore.collection("user_sites")
                    .whereEqualTo("IdentityId", request.IdentityId)
                    .whereEqualTo("Id", request.Id)
                    .get()
                    .await()

                for (doc in querySnapshot.documents) {
                    doc.reference.delete().await()
                }
            } catch (_: Exception) {}
        }
        return AppUserSiteCreateResponse(Id = request.Id, Mess = "Success")
    }

    override suspend fun copyAppUserSite(request: AppUserSiteCopyRequest): AppUserSiteCreateResponse {
        return try {
            val localList = dao.getAllSites()
            val target = localList.find { it.Id == request.Id }
            if (target != null) {
                val newId = (System.currentTimeMillis() % Int.MAX_VALUE).toInt()
                val isLoggedIn = request.IdentityId != "local_user"
                val copiedEntity = target.copy(
                    Id = newId,
                    Name = "${target.Name} (Copy)",
                    IdentityId = request.IdentityId,
                    isSynced = !isLoggedIn
                )
                dao.insert(copiedEntity)
                AppUserSiteCreateResponse(Id = newId, Mess = "Success")
            } else {
                AppUserSiteCreateResponse(Id = request.Id, Mess = "Site not found")
            }
        } catch (e: Exception) {
            AppUserSiteCreateResponse(Id = request.Id, Mess = e.message ?: "Error")
        }
    }
}
