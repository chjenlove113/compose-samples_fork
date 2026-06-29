package com.app.tintuccongnghe.data.local

import com.app.tintuccongnghe.data.local.entities.News
import com.app.tintuccongnghe.data.local.entities.News_Site
import com.app.tintuccongnghe.data.local.entities.News_Tag
import kotlinx.coroutines.flow.Flow

interface IAppDbService {
    fun getNewsTag(): Flow<List<News_Tag>>
    fun deleteAllAndInsertAllNewsTag(newsTagList: List<News_Tag>)

    fun getNewsSite(): Flow<List<News_Site>>
    fun deleteAllAndInsertAllNewsSite(newsSiteList: List<News_Site>)

    fun isNewsSaved(id: Int): Flow<Boolean>
    suspend fun saveNews(news: News)
    suspend fun deleteNews(news: News)

    fun isTagSaved(slug: String): Flow<Boolean>
    suspend fun saveTagSlug(slug: String)
    suspend fun deleteTagSlug(slug: String)
}
