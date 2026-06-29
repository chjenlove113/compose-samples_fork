package com.app.tintuccongnghe.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.app.tintuccongnghe.data.local.entities.News
import kotlinx.coroutines.flow.Flow

@Dao
interface NewsDao {
    @Query("SELECT * FROM news")
    fun getAll(): Flow<List<News>>

    @Query("SELECT * FROM news WHERE Id = :id LIMIT 1")
    suspend fun getNewsById(id: Int): News?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(news: News)

    @Delete
    suspend fun delete(news: News)

    @Query("SELECT EXISTS(SELECT 1 FROM news WHERE Id = :id)")
    fun isNewsSaved(id: Int): Flow<Boolean>
}
