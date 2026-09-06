package com.app.tintuccongnghe.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.app.tintuccongnghe.data.local.entities.AuthEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AuthDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuth(auth: AuthEntity)

    @Query("SELECT * FROM auth_info LIMIT 1")
    fun getAuthInfo(): Flow<AuthEntity?>

    @Query("SELECT * FROM auth_info LIMIT 1")
    suspend fun getAuthInfoDirect(): AuthEntity?

    @Query("DELETE FROM auth_info")
    suspend fun clearAuth()
}
