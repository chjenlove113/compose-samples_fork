package com.news.data.local.dao

import androidx.room.*
import com.news.data.local.entities.AppUserSiteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppUserSiteDao {
    @Query("SELECT * FROM app_user_sites WHERE IdentityId = :identityId AND AppIdEncrypt = :appId")
    fun getAppUserSites(identityId: String, appId: String): Flow<List<AppUserSiteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(sites: List<AppUserSiteEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(site: AppUserSiteEntity)

    @Query("DELETE FROM app_user_sites WHERE IdentityId = :identityId AND AppIdEncrypt = :appId")
    suspend fun deleteSites(identityId: String, appId: String)

    @Transaction
    suspend fun refreshSites(identityId: String, appId: String, sites: List<AppUserSiteEntity>) {
        deleteSites(identityId, appId)
        insertAll(sites)
    }
}
