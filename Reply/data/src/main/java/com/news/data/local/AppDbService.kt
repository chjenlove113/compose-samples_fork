package com.news.data.local

import com.news.data.local.entities.News_Tag
import kotlinx.coroutines.flow.Flow

class AppDbService(private val appDatabase: AppDatabase) : IAppDbService {
    override fun getNewsTag(): Flow<List<News_Tag>> {
        return appDatabase.newsTagDao().getAll()
    }

    override fun deleteAllAndInsertAllNewsTag(newsTagList: List<News_Tag>) {
        appDatabase.newsTagDao().deleteAllAndInsertAll(newsTagList)
    }
}