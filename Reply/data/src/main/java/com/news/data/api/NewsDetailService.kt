package com.news.data.api

import com.news.data.models.NewsDetailRequest
import com.news.data.models.NewsDetailResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

import com.news.data.models.NewsTagsRequest
import com.news.domain.models.NewsTag

interface NewsDetailService {
    @POST("api/getnewsdetail")
    suspend fun fetchNewsDetailHtml(
        @Body request: NewsDetailRequest
    ): Response<NewsDetailResponse>

    @POST("api/GetListTags")
    suspend fun fetchNewsTags(
        @Body request: NewsTagsRequest
    ): Response<List<NewsTag>>
}
