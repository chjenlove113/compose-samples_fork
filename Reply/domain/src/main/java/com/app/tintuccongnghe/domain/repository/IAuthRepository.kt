package com.app.tintuccongnghe.domain.repository

import com.app.tintuccongnghe.domain.models.DeleteAccountResponse
import com.app.tintuccongnghe.domain.models.LoginRequest
import com.app.tintuccongnghe.domain.models.LoginResponse
import com.app.tintuccongnghe.domain.models.RegisterRequest
import com.app.tintuccongnghe.domain.models.RegisterResponse

import kotlinx.coroutines.flow.Flow

interface IAuthRepository {
    suspend fun register(request: RegisterRequest): RegisterResponse
    suspend fun login(request: LoginRequest): LoginResponse
    fun getAuthInfo(): Flow<LoginResponse?>
    suspend fun logout()
    suspend fun deleteAccount(): DeleteAccountResponse
}

