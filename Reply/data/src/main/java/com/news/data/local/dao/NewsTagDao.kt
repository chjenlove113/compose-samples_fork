package com.news.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.news.data.local.entities.News_Tag
import kotlinx.coroutines.flow.Flow
@Dao
interface NewsTagDao {
    @Query("SELECT * FROM news_tag")
    fun getAll(): Flow<List<News_Tag>>

    @Insert
    fun insertAll(articles: List<News_Tag>)

    @Query("DELETE FROM news_tag")
    fun deleteAll()

    @Transaction
    fun deleteAllAndInsertAll(articles: List<News_Tag>) {
        deleteAll()
        return insertAll(articles)
    }
}