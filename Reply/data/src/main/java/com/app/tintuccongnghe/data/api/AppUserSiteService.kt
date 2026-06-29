package com.app.tintuccongnghe.data.api

import com.app.tintuccongnghe.domain.models.AppUserSite
import com.app.tintuccongnghe.domain.models.AppUserSiteCreateRequest
import com.app.tintuccongnghe.domain.models.AppUserSiteCreateResponse
import com.app.tintuccongnghe.domain.models.AppUserSiteRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface AppUserSiteService {
    @POST("api/AppUserSiteList")
    suspend fun getAppUserSiteList(@Body request: AppUserSiteRequest): List<AppUserSite>

    // Assuming an update endpoint exists based on requirements
    @POST("api/AppUserSiteBlockOrUnBlock")
    suspend fun updateAppUserSite(@Body site: AppUserSite): Any

    @POST("api/AppUserSiteCreateOrUpdate")
    suspend fun createOrUpdateAppUserSite(@Body request: AppUserSiteCreateRequest): AppUserSiteCreateResponse

    @POST("api/AppUserSiteDelete")
    suspend fun deleteAppUserSite(@Body request: AppUserSiteCreateRequest): AppUserSiteCreateResponse
}
