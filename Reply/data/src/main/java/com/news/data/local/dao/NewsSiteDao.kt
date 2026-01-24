package com.news.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.news.data.local.entities.News_Site
import kotlinx.coroutines.flow.Flow
@Dao
interface NewsSiteDao {
    @Query("SELECT * FROM news_site")
    fun getAll(): Flow<List<News_Site>>

    @Insert
    fun insertAll(articles: List<News_Site>)

    @Query("DELETE FROM news_site")
    fun deleteAll()

    @Transaction
    fun deleteAllAndInsertAll(articles: List<News_Site>) {
        deleteAll()
        return insertAll(articles)
    }
}