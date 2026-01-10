package com.news.data.api

import com.news.data.models.PostBaseModel
import com.news.domain.models.NewsTagResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface NewsTagService {

    @POST("api/TopTagByApp")
    suspend fun fetchNewsTagList(
        @Body AppModel: PostBaseModel
    ): Response<List<NewsTagResponse>>

}