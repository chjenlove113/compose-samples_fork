package com.news.data.local

import com.news.data.local.entities.News
import com.news.data.local.entities.News_Site
import com.news.data.local.entities.News_Tag
import kotlinx.coroutines.flow.Flow

class AppDbService(private val appDatabase: AppDatabase) : IAppDbService {
    override fun getNewsTag(): Flow<List<News_Tag>> {
        return appDatabase.newsTagDao().getAll()
    }

    override fun deleteAllAndInsertAllNewsTag(newsTagList: List<News_Tag>) {
        appDatabase.newsTagDao().deleteAllAndInsertAll(newsTagList)
    }

    override fun getNewsSite(): Flow<List<News_Site>> {
        return appDatabase.newsSiteDao().getAll()
    }

    override fun deleteAllAndInsertAllNewsSite(newsSiteList: List<News_Site>) {
        return appDatabase.newsSiteDao().deleteAllAndInsertAll(newsSiteList)
    }

    override fun isNewsSaved(id: Int): Flow<Boolean> {
        return appDatabase.newsDao().isNewsSaved(id)
    }

    override suspend fun saveNews(news: News) {
        appDatabase.newsDao().insert(news)
    }

    override suspend fun deleteNews(news: News) {
        appDatabase.newsDao().delete(news)
    }
}
