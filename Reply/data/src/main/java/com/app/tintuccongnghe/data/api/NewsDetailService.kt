package com.app.tintuccongnghe.data.api

import com.app.tintuccongnghe.data.models.NewsDetailRequest
import com.app.tintuccongnghe.data.models.NewsDetailResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

import com.app.tintuccongnghe.data.models.NewsTagsRequest
import com.app.tintuccongnghe.domain.models.NewsTag

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
