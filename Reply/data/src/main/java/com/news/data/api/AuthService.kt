package com.news.data.api

import com.news.domain.models.LoginRequest
import com.news.domain.models.LoginResponse
import com.news.domain.models.RegisterRequest
import com.news.domain.models.RegisterResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {
    @POST("api/register")
    suspend fun register(@Body request: RegisterRequest): RegisterResponse

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse
}
