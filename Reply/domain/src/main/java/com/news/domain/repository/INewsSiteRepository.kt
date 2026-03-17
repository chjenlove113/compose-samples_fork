package com.news.domain.repository

import androidx.paging.PagingData
import com.news.domain.models.NewsSite
import kotlinx.coroutines.flow.Flow

interface INewsSiteRepository {
    fun getListNewsSite(): Flow<List<NewsSite>>
    fun getListNewsSitePaging(): Flow<PagingData<NewsSite>>
}