package com.app.tintuccongnghe.data.local.dao

import androidx.room.*
import com.app.tintuccongnghe.data.local.entities.NewsWebsiteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NewsWebsiteDao {
    @Query("SELECT * FROM news_websites_table")
    fun getAllWebsites(): Flow<List<NewsWebsiteEntity>>

    @Query("SELECT * FROM news_websites_table")
    suspend fun getAllWebsitesList(): List<NewsWebsiteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(websites: List<NewsWebsiteEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(website: NewsWebsiteEntity)

    @Query("UPDATE news_websites_table SET isActive = :isActive WHERE id = :id")
    suspend fun updateActiveStatus(id: String, isActive: Boolean)
}
