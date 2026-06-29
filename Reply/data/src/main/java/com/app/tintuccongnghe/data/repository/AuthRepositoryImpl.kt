package com.app.tintuccongnghe.data.repository

import com.app.tintuccongnghe.data.api.AuthService
import com.app.tintuccongnghe.data.local.AppDatabase
import com.app.tintuccongnghe.data.local.entities.AuthEntity
import com.app.tintuccongnghe.domain.models.AccessToken
import com.app.tintuccongnghe.domain.models.LoginRequest
import com.app.tintuccongnghe.domain.models.LoginResponse
import com.app.tintuccongnghe.domain.models.RegisterRequest
import com.app.tintuccongnghe.domain.models.RegisterResponse
import com.app.tintuccongnghe.domain.repository.IAuthRepository
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
                    UserName = it.Username,
                    AccessToken = AccessToken(it.Token, it.ExpiresIn),
                    RefreshToken = it.RefreshToken,
                    Errors = emptyList(),
                    IdentityId = it.IdentityId,
                    Email = it.Email,
                    Private_Code = ""
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
            login(LoginRequest(request.UserName, request.Password))
        }
        return response
    }

    override suspend fun login(request: LoginRequest): LoginResponse {
        val response = authService.login(request)
        if (response.Errors.isEmpty()) {
            val authEntity = AuthEntity(
                IdentityId = response.IdentityId,
                Token = response.AccessToken.Token,
                ExpiresIn = response.AccessToken.ExpiresIn,
                Email = response.Email,
                RefreshToken = response.RefreshToken,
                Username = response.UserName
            )
            appDatabase.authDao().insertAuth(authEntity)
        }
        return response
    }
}
