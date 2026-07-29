package com.app.tintuccongnghe.data.local.dao

import androidx.room.*
import com.app.tintuccongnghe.data.local.entities.AppUserSiteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppUserSiteDao {
    @Query("SELECT * FROM app_user_sites WHERE IdentityId = :identityId AND AppIdEncrypt = :appId")
    fun getAppUserSites(identityId: String, appId: String): Flow<List<AppUserSiteEntity>>

    @Query("SELECT * FROM app_user_sites WHERE IdentityId = :identityId AND AppIdEncrypt = :appId")
    suspend fun getAppUserSitesList(identityId: String, appId: String): List<AppUserSiteEntity>

    @Query("SELECT * FROM app_user_sites")
    suspend fun getAllSites(): List<AppUserSiteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(sites: List<AppUserSiteEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(site: AppUserSiteEntity)

    @Update
    suspend fun update(site: AppUserSiteEntity)

    @Delete
    suspend fun delete(site: AppUserSiteEntity)

    @Query("DELETE FROM app_user_sites WHERE Id = :id AND `GROUP` = :group AND Kind = :kind")
    suspend fun deleteById(id: Int, group: String, kind: String)

    @Query("DELETE FROM app_user_sites WHERE IdentityId = :identityId AND AppIdEncrypt = :appId")
    suspend fun deleteSites(identityId: String, appId: String)

    @Query("DELETE FROM app_user_sites")
    suspend fun deleteAll()
}
