package com.news.data.api

import com.news.domain.models.NewsSiteReponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsSiteService {
    @GET("api/app/getmenu")
    suspend fun fetchNewsSiteList(
        @Query("app_Id") app_Id: String,
    ): Response<NewsSiteReponse>
}