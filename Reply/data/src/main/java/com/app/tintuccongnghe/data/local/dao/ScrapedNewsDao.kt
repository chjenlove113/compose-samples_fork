package com.app.tintuccongnghe.data.local.dao

import androidx.room.*
import com.app.tintuccongnghe.data.local.entities.ScrapedNewsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScrapedNewsDao {
    @Query("SELECT * FROM scraped_news WHERE categoryId = :categoryId")
    fun getNewsForCategory(categoryId: String): Flow<List<ScrapedNewsEntity>>

    @Query("SELECT * FROM scraped_news WHERE websiteId = :websiteId")
    fun getNewsForWebsite(websiteId: String): Flow<List<ScrapedNewsEntity>>

    @Query("SELECT * FROM scraped_news ORDER BY date DESC")
    fun getHomeNews(): Flow<List<ScrapedNewsEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ScrapedNewsEntity>)

    @Query("DELETE FROM scraped_news WHERE categoryId = :categoryId")
    suspend fun deleteForCategory(categoryId: String)

    @Query("DELETE FROM scraped_news")
    suspend fun deleteAll()
}
