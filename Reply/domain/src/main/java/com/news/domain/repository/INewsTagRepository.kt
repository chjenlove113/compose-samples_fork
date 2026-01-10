package com.news.domain.repository

import com.news.domain.models.NewsTag
import kotlinx.coroutines.flow.Flow

interface INewsTagRepository {
    fun getListNewsTag(): Flow<List<NewsTag>>
}