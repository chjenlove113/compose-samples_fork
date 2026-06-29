package com.app.tintuccongnghe.data.api

import com.app.tintuccongnghe.data.models.NewsByTagRequest
import com.app.tintuccongnghe.data.models.PostBaseModel
import com.app.tintuccongnghe.domain.models.News
import com.app.tintuccongnghe.domain.models.NewsTagResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface NewsTagService {

    @POST("api/TopTagByApp")
    suspend fun fetchNewsTagList(
        @Body AppModel: PostBaseModel
    ): Response<List<NewsTagResponse>>

    @POST("api/NewsByTag")
    suspend fun fetchNewsByTag(
        @Body request: NewsByTagRequest
    ): Response<List<News>>

}