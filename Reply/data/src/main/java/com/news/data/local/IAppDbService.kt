package com.news.data.local

import com.news.data.local.entities.News_Tag
import kotlinx.coroutines.flow.Flow

interface IAppDbService {
    fun getNewsTag():Flow<List<News_Tag>>
    fun deleteAllAndInsertAllNewsTag(newsTagList: List<News_Tag>)
}