package com.app.tintuccongnghe.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.app.tintuccongnghe.data.local.entities.News_Tag
import com.app.tintuccongnghe.data.local.entities.SavedTagSlug
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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedTag(savedTag: SavedTagSlug)

    @Delete
    suspend fun deleteSavedTag(savedTag: SavedTagSlug)

    @Query("SELECT EXISTS(SELECT 1 FROM saved_tag_slugs WHERE slug = :slug)")
    fun isTagSaved(slug: String): Flow<Boolean>
}