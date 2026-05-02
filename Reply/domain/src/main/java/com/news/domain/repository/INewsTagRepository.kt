package com.news.domain.repository

import com.news.domain.models.News
import com.news.domain.models.NewsTag
import kotlinx.coroutines.flow.Flow

interface INewsTagRepository {
    fun getListNewsTag(): Flow<List<NewsTag>>
    fun getNewsByTag(tagSlug: String, pageNumber: Int, rowsOfPage: Int): Flow<List<News>>
}