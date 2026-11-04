package com.app.tintuccongnghe.data.local.dao

import androidx.room.*
import com.app.tintuccongnghe.data.local.entities.NewsDetailEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NewsDetailDao {
    @Query("SELECT * FROM news_details WHERE link = :link LIMIT 1")
    suspend fun getDetailByLink(link: String): NewsDetailEntity?

    @Query("SELECT * FROM news_details WHERE isFavorite = 1")
    fun getFavoriteDetails(): Flow<List<NewsDetailEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDetail(detail: NewsDetailEntity)

    @Update
    suspend fun updateDetail(detail: NewsDetailEntity)
}
