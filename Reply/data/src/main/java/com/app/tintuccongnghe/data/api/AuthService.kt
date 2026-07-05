package com.app.tintuccongnghe.data.api

import com.app.tintuccongnghe.domain.models.DeleteAccountRequest
import com.app.tintuccongnghe.domain.models.LoginRequest
import com.app.tintuccongnghe.domain.models.LoginResponse
import com.app.tintuccongnghe.domain.models.RegisterRequest
import com.app.tintuccongnghe.domain.models.RegisterResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {
    @POST("api/register")
    suspend fun register(@Body request: RegisterRequest): RegisterResponse

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("api/Auth/Delete")
    suspend fun deleteAccount(@Body request: DeleteAccountRequest): Any
}
