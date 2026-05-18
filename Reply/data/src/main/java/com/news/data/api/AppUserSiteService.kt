package com.news.data.api

import com.news.domain.models.AppUserSite
import com.news.domain.models.AppUserSiteRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface AppUserSiteService {
    @POST("api/AppUserSiteList")
    suspend fun getAppUserSiteList(@Body request: AppUserSiteRequest): List<AppUserSite>

    // Assuming an update endpoint exists based on requirements
    @POST("api/UpdateAppUserSite")
    suspend fun updateAppUserSite(@Body site: AppUserSite): Any 
}
