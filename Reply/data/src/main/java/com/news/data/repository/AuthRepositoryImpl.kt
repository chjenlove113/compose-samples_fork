package com.news.data.repository

import com.news.data.api.AuthService
import com.news.data.local.AppDatabase
import com.news.data.local.entities.AuthEntity
import com.news.domain.models.LoginRequest
import com.news.domain.models.LoginResponse
import com.news.domain.models.RegisterRequest
import com.news.domain.models.RegisterResponse
import com.news.domain.repository.IAuthRepository
import javax.inject.Inject

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AuthRepositoryImpl @Inject constructor(
    private val authService: AuthService,
    private val appDatabase: AppDatabase
) : IAuthRepository {

    override fun getAuthInfo(): Flow<LoginResponse?> {
        return appDatabase.authDao().getAuthInfo().map { entity ->
            entity?.let {
                LoginResponse(
                    Result = true,
                    Token = it.Token,
                    Username = it.Username,
                    IsLockedOut = it.IsLockedOut,
                    IsNotAllowed = it.IsNotAllowed,
                    RequiresTwoFactor = it.RequiresTwoFactor
                )
            }
        }
    }

    override suspend fun logout() {
        appDatabase.authDao().clearAuth()
    }

    override suspend fun register(request: RegisterRequest): RegisterResponse {
        val response = authService.register(request)
        if (response.Errors.isEmpty()) {
            // If registration successful, automatically login
            val loginResponse = login(LoginRequest(request.UserName, request.Password))
            // We can return the register response as requested, 
            // but the login response is handled internally to store data.
        }
        return response
    }

    override suspend fun login(request: LoginRequest): LoginResponse {
        val response = authService.login(request)
        if (response.Result && response.Token != null) {
            val authEntity = AuthEntity(
                Username = response.Username ?: request.Username,
                Token = response.Token,
                IsLockedOut = response.IsLockedOut,
                IsNotAllowed = response.IsNotAllowed,
                RequiresTwoFactor = response.RequiresTwoFactor
            )
            appDatabase.authDao().insertAuth(authEntity)
        }
        return response
    }
}
