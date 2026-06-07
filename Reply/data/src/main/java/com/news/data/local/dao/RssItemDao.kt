package com.news.data.local.dao

import androidx.room.*
import com.news.data.local.entities.RssItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RssItemDao {
    @Query("SELECT * FROM rss_items WHERE siteId = :siteId AND siteGroup = :siteGroup AND siteKind = :siteKind ORDER BY pubDate DESC")
    fun getRssItemsForSite(siteId: Int, siteGroup: String, siteKind: String): Flow<List<RssItemEntity>>

    @Query("SELECT * FROM rss_items WHERE siteId = :siteId AND siteGroup = :siteGroup AND siteKind = :siteKind ORDER BY pubDate DESC")
    fun getRssItemsForSitePaging(siteId: Int, siteGroup: String, siteKind: String): androidx.paging.PagingSource<Int, RssItemEntity>

    @Query("SELECT * FROM rss_items WHERE link = :link LIMIT 1")
    suspend fun getItemByLink(link: String): RssItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<RssItemEntity>)

    @Update
    suspend fun update(item: RssItemEntity)

    @Query("DELETE FROM rss_items WHERE siteId = :siteId AND siteGroup = :siteGroup AND siteKind = :siteKind")
    suspend fun deleteItemsForSite(siteId: Int, siteGroup: String, siteKind: String)
}
