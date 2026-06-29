package com.app.tintuccongnghe.domain.repository

import com.app.tintuccongnghe.domain.models.News
import com.app.tintuccongnghe.domain.models.NewsTag
import kotlinx.coroutines.flow.Flow

interface INewsTagRepository {
    fun getListNewsTag(): Flow<List<NewsTag>>
    fun getNewsByTag(tagSlug: String, pageNumber: Int, rowsOfPage: Int): Flow<List<News>>
}