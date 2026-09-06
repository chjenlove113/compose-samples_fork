package com.app.tintuccongnghe.data.api

import com.app.tintuccongnghe.domain.models.ShowHomeDataModel
import retrofit2.http.GET
import retrofit2.http.Query

interface ShowHomeService {
    @GET("api/tincongnghe/getappshowhome")
    suspend fun getShowHome(
        @Query("app_Slug") app_Slug: String,
        @Query("page") page: Int,
        @Query("site_Slug") site_Slug: String,
        @Query("cat_Slug") cat_Slug: String,
        @Query("Identity") userId: String? = null
    ): ShowHomeDataModel
}