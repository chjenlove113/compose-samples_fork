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
        return dao.getAppUserSites(request.IdentityId, request.AppIdEncrypt).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun refreshAppUserSiteList(request: AppUserSiteRequest) {
        try {
            val remoteItems = mutableListOf<AppUserSite>()

            // 1. Load common/public list for all users (IdentityId == "common")
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

            // 2. If logged in, also load user's specific list
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

            // 3. Compare with local data based on Key. If local does not have it, insert into local Room DB.
            remoteItems.forEach { remote ->
                val existingLocal = localList.find { it.Key == remote.Key }
                if (existingLocal == null) {
                    dao.insert(remote.toEntity().copy(IdentityId = if (request.IdentityId != "local_user") request.IdentityId else "local_user"))
                } else {
                    if (existingLocal.isSynced) {
                        val updatedEntity = remote.toEntity().copy(
                            IdentityId = existingLocal.IdentityId,
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
        val isLoggedIn = site.IdentityId != "local_user"
        val synced = if (isLoggedIn) {
            try {
                val querySnapshot = firestore.collection("user_sites")
                    .whereEqualTo("IdentityId", site.IdentityId)
                    .whereEqualTo("Id", site.Id)
                    .get()
                    .await()

                val siteMap = mapOf(
                    "Id" to site.Id,
                    "Key" to site.Key,
                    "Name" to site.Name,
                    "TextColor" to site.TextColor,
                    "BackgroundColor" to site.BackgroundColor,
                    "Url" to site.Url,
                    "IsActive" to site.IsActive,
                    "Kind" to site.Kind,
                    "GROUP" to site.GROUP,
                    "Stt" to site.Stt,
                    "LimitEdit" to site.LimitEdit,
                    "AllowEdit" to site.AllowEdit,
                    "OtherCanSee" to site.OtherCanSee,
                    "IdentityId" to site.IdentityId,
                    "AppIdEncrypt" to site.AppIdEncrypt
                )

                if (!querySnapshot.isEmpty) {
                    for (doc in querySnapshot.documents) {
                        doc.reference.set(siteMap, SetOptions.merge()).await()
                    }
                } else {
                    firestore.collection("user_sites").add(siteMap).await()
                }
                true
            } catch (_: Exception) {
                false
            }
        } else {
            true
        }

        try {
            val entity = site.toEntity().copy(isSynced = synced)
            dao.insert(entity)
            return true
        } catch (e: Exception) {
            return false
        }
    }

    override suspend fun createOrUpdateAppUserSite(request: AppUserSiteCreateRequest): AppUserSiteCreateResponse {
        val siteId = if (request.Id == 0) (System.currentTimeMillis() % Int.MAX_VALUE).toInt() else request.Id
        val isLoggedIn = request.IdentityId != "local_user"
        val synced = if (isLoggedIn) {
            try {
                val querySnapshot = firestore.collection("user_sites")
                    .whereEqualTo("IdentityId", request.IdentityId)
                    .whereEqualTo("Id", siteId)
                    .get()
                    .await()

                val dataMap = mapOf(
                    "Id" to siteId,
                    "Key" to request.Name,
                    "Name" to request.Name,
                    "Url" to request.Url,
                    "Icon" to request.Icon,
                    "IsActive" to request.Status,
                    "OtherCanSee" to request.OtherCanSee,
                    "Kind" to request.Kind,
                    "GROUP" to request.GROUP,
                    "IdentityId" to request.IdentityId,
                    "AppIdEncrypt" to request.AppIdEncrypt
                )

                if (!querySnapshot.isEmpty) {
                    for (doc in querySnapshot.documents) {
                        doc.reference.set(dataMap, SetOptions.merge()).await()
                    }
                } else {
                    firestore.collection("user_sites").add(dataMap).await()
                }
                true
            } catch (_: Exception) {
                false
            }
        } else {
            true
        }

        try {
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
                isSynced = synced
            )
            dao.insert(site.toEntity())
            return AppUserSiteCreateResponse(Id = siteId, Mess = "Success")
        } catch (e: Exception) {
            return AppUserSiteCreateResponse(Id = request.Id, Mess = e.message ?: "Error")
        }
    }

    override suspend fun deleteAppUserSite(request: AppUserSiteCreateRequest): AppUserSiteCreateResponse {
        dao.deleteById(request.Id, request.GROUP, request.Kind)

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
                    isSynced = false
                )

                val synced = if (isLoggedIn) {
                    try {
                        val site = copiedEntity.toDomain()
                        val siteMap = mapOf(
                            "Id" to site.Id,
                            "Key" to site.Key,
                            "Name" to site.Name,
                            "TextColor" to site.TextColor,
                            "BackgroundColor" to site.BackgroundColor,
                            "Url" to site.Url,
                            "IsActive" to site.IsActive,
                            "Kind" to site.Kind,
                            "GROUP" to site.GROUP,
                            "Stt" to site.Stt,
                            "LimitEdit" to site.LimitEdit,
                            "AllowEdit" to site.AllowEdit,
                            "OtherCanSee" to site.OtherCanSee,
                            "IdentityId" to site.IdentityId,
                            "AppIdEncrypt" to site.AppIdEncrypt
                        )
                        firestore.collection("user_sites").add(siteMap).await()
                        true
                    } catch (_: Exception) {
                        false
                    }
                } else {
                    true
                }

                dao.insert(copiedEntity.copy(isSynced = synced))
                AppUserSiteCreateResponse(Id = newId, Mess = "Success")
            } else {
                AppUserSiteCreateResponse(Id = request.Id, Mess = "Site not found")
            }
        } catch (e: Exception) {
            AppUserSiteCreateResponse(Id = request.Id, Mess = e.message ?: "Error")
        }
    }
}
