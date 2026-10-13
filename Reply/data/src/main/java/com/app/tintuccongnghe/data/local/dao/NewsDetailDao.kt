package com.app.tintuccongnghe.data.local.dao

import androidx.room.*
import com.app.tintuccongnghe.data.local.entities.NewsDetailEntity

@Dao
interface NewsDetailDao {
    @Query("SELECT * FROM news_details WHERE link = :link LIMIT 1")
    suspend fun getDetailByLink(link: String): NewsDetailEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDetail(detail: NewsDetailEntity)
}
