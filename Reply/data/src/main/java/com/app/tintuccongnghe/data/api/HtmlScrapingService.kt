package com.app.tintuccongnghe.data.api

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import retrofit2.http.POST
import retrofit2.http.Url

interface HtmlScrapingService {
    @GET
    suspend fun fetchGet(
        @HeaderMap headers: Map<String, String>,
        @Url url: String
    ): Response<ResponseBody>

    @POST
    suspend fun fetchPost(
        @HeaderMap headers: Map<String, String>,
        @Url url: String,
        @Body body: RequestBody
    ): Response<ResponseBody>
}
