package com.news.domain.repository

import com.news.domain.models.LoginRequest
import com.news.domain.models.LoginResponse
import com.news.domain.models.RegisterRequest
import com.news.domain.models.RegisterResponse

import kotlinx.coroutines.flow.Flow

interface IAuthRepository {
    suspend fun register(request: RegisterRequest): RegisterResponse
    suspend fun login(request: LoginRequest): LoginResponse
    fun getAuthInfo(): Flow<LoginResponse?>
    suspend fun logout()
}
