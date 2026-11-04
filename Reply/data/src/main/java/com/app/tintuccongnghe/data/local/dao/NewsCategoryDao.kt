package com.app.tintuccongnghe.data.local.dao

import androidx.room.*
import com.app.tintuccongnghe.data.local.entities.NewsCategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NewsCategoryDao {
    @Query("SELECT * FROM news_categories_table WHERE websiteId = :websiteId")
    fun getCategoriesForWebsiteFlow(websiteId: String): Flow<List<NewsCategoryEntity>>

    @Query("SELECT * FROM news_categories_table WHERE websiteId = :websiteId")
    suspend fun getCategoriesForWebsiteList(websiteId: String): List<NewsCategoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(categories: List<NewsCategoryEntity>)

    @Query("DELETE FROM news_categories_table WHERE websiteId = :websiteId")
    suspend fun deleteForWebsite(websiteId: String)
}
