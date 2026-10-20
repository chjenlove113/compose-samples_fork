package com.app.tintuccongnghe.data.repository

import com.app.tintuccongnghe.data.local.AppDatabase
import com.app.tintuccongnghe.data.local.entities.AuthEntity
import com.app.tintuccongnghe.domain.models.AccessToken
import com.app.tintuccongnghe.domain.models.DeleteAccountResponse
import com.app.tintuccongnghe.domain.models.LoginRequest
import com.app.tintuccongnghe.domain.models.LoginResponse
import com.app.tintuccongnghe.domain.models.RegisterRequest
import com.app.tintuccongnghe.domain.models.RegisterResponse
import com.app.tintuccongnghe.domain.repository.IAuthRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val appDatabase: AppDatabase
) : IAuthRepository {

    private val authDao = appDatabase.authDao()

    override fun getAuthInfo(): Flow<LoginResponse?> {
        return authDao.getAuthInfo().map { entity ->
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
        try {
            firebaseAuth.signOut()
        } finally {
            authDao.clearAuth()
        }
    }

    override suspend fun deleteAccount(): DeleteAccountResponse {
        return try {
            val user = firebaseAuth.currentUser
            user?.delete()?.await()
            logout()
            DeleteAccountResponse(Success = true, Message = "Account deleted successfully")
        } catch (e: Exception) {
            DeleteAccountResponse(Success = false, Message = e.message)
        }
    }

    override suspend fun register(request: RegisterRequest): RegisterResponse {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(request.Email, request.Password).await()
            val firebaseUser = result.user ?: throw Exception("Registration failed: user is null")
            val tokenResult = firebaseUser.getIdToken(true).await()
            val token = tokenResult.token ?: ""

            val authEntity = AuthEntity(
                IdentityId = firebaseUser.uid,
                Token = token,
                ExpiresIn = 3600,
                Email = firebaseUser.email ?: request.Email,
                RefreshToken = "",
                Username = request.UserName.ifBlank { firebaseUser.email ?: "User" }
            )
            authDao.insertAuth(authEntity)
            RegisterResponse(Id = 1, Errors = emptyList())
        } catch (e: Exception) {
            RegisterResponse(
                Id = 0,
                Errors = listOf(com.app.tintuccongnghe.domain.models.AuthError(Code = "REGISTER_ERROR", Description = e.localizedMessage ?: "Unknown error"))
            )
        }
    }

    override suspend fun login(request: LoginRequest): LoginResponse {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(request.Username, request.Password).await()
            val firebaseUser = result.user ?: throw Exception("Login failed: user is null")
            val tokenResult = firebaseUser.getIdToken(true).await()
            val token = tokenResult.token ?: ""

            val authEntity = AuthEntity(
                IdentityId = firebaseUser.uid,
                Token = token,
                ExpiresIn = 3600,
                Email = firebaseUser.email ?: request.Username,
                RefreshToken = "",
                Username = firebaseUser.displayName ?: firebaseUser.email ?: request.Username
            )
            authDao.insertAuth(authEntity)

            LoginResponse(
                UserName = authEntity.Username,
                AccessToken = AccessToken(token, 3600),
                RefreshToken = "",
                Errors = emptyList(),
                IdentityId = firebaseUser.uid,
                Email = authEntity.Email,
                Private_Code = ""
            )
        } catch (e: Exception) {
            LoginResponse(
                UserName = "",
                AccessToken = AccessToken("", 0),
                RefreshToken = "",
                Errors = listOf(com.app.tintuccongnghe.domain.models.AuthError(Code = "LOGIN_ERROR", Description = e.localizedMessage ?: "Unknown error")),
                IdentityId = "",
                Email = "",
                Private_Code = ""
            )
        }
    }
}
