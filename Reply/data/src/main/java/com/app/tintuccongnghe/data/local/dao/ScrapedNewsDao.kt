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

    @Query("SELECT * FROM scraped_news WHERE link = :link LIMIT 1")
    suspend fun getNewsByLink(link: String): ScrapedNewsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNews(item: ScrapedNewsEntity)

    @Update
    suspend fun updateNews(item: ScrapedNewsEntity)

    @Transaction
    suspend fun upsertNews(newItem: ScrapedNewsEntity) {
        val existing = getNewsByLink(newItem.link)
        if (existing == null) {
            insertNews(newItem)
        } else {
            val updated = newItem.copy(
                createdAt = existing.createdAt,
                updatedAt = System.currentTimeMillis()
            )
            updateNews(updated)
        }
    }

    @Transaction
    suspend fun upsertAll(items: List<ScrapedNewsEntity>) {
        for (item in items) {
            upsertNews(item)
        }
    }

    @Query("DELETE FROM scraped_news WHERE categoryId = :categoryId")
    suspend fun deleteForCategory(categoryId: String)

    @Query("DELETE FROM scraped_news")
    suspend fun deleteAll()
}
