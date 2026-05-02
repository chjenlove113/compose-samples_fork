package com.news.domain.repository

import kotlinx.coroutines.flow.Flow

interface INewsDetailRepository {
    fun fetchNewsDetailHtml(
        link: String,
        cat: String,
        id: Int,
        html: String,
        slug: String
    ): Flow<Result<String>>
}
