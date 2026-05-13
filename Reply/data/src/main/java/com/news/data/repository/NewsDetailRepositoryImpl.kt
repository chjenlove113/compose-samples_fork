package com.news.data.repository

import com.news.data.api.NewsDetailService
import com.news.data.models.NewsDetailRequest
import com.news.data.models.NewsTagsRequest
import com.news.domain.models.NewsTag
import com.news.domain.repository.INewsDetailRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class NewsDetailRepositoryImpl @Inject constructor(
    private val newsDetailService: NewsDetailService
) : INewsDetailRepository {

    override fun fetchNewsDetailHtml(
        link: String,
        cat: String,
        id: Int,
        html: String,
        slug: String
    ): Flow<Result<String>> = flow {
        try {
            val response = newsDetailService.fetchNewsDetailHtml(
                NewsDetailRequest(link, cat, id, html, slug)
            )
            if (response.isSuccessful && response.body() != null) {
                emit(Result.success(response.body()!!.html))
            } else {
                emit(Result.failure(Exception("Failed to fetch news detail html: ${response.message()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override fun fetchNewsTags(newsId: Int): Flow<Result<List<NewsTag>>> = flow {
        try {
            val response = newsDetailService.fetchNewsTags(NewsTagsRequest(newsId.toString()))
            if (response.isSuccessful && response.body() != null) {
                emit(Result.success(response.body()!!))
            } else {
                emit(Result.failure(Exception("Failed to fetch news tags: ${response.message()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}
