package com.app.tintuccongnghe.domain.repository

import com.app.tintuccongnghe.domain.models.NewsTag
import kotlinx.coroutines.flow.Flow

interface INewsDetailRepository {
    fun fetchNewsDetailHtml(
        link: String,
        cat: String,
        id: Int,
        html: String,
        slug: String
    ): Flow<Result<String>>

    fun fetchNewsTags(newsId: Int): Flow<Result<List<NewsTag>>>
}
