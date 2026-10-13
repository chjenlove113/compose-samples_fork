package com.app.tintuccongnghe.data.api

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Url

interface HtmlScrapingService {
    @GET
    suspend fun fetchHtml(@Url url: String): ResponseBody
}
