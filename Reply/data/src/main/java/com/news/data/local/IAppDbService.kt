package com.news.data.local

import com.news.data.local.entities.News
import com.news.data.local.entities.News_Site
import com.news.data.local.entities.News_Tag
import kotlinx.coroutines.flow.Flow

interface IAppDbService {
    fun getNewsTag(): Flow<List<News_Tag>>
    fun deleteAllAndInsertAllNewsTag(newsTagList: List<News_Tag>)

    fun getNewsSite(): Flow<List<News_Site>>
    fun deleteAllAndInsertAllNewsSite(newsSiteList: List<News_Site>)

    fun isNewsSaved(id: Int): Flow<Boolean>
    suspend fun saveNews(news: News)
    suspend fun deleteNews(news: News)
}
