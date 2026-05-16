package com.news.domain.models

data class RegisterRequest(
    val AppId: Int = 0,
    val Email: String,
    val UserName: String,
    val Password: String
)

data class RegisterResponse(
    val Id: Int,
    val Errors: List<AuthError>
)

data class AuthError(
    val Code: String,
    val Description: String
)

data class LoginRequest(
    val Username: String,
    val Password: String
)

data class LoginResponse(
    val Result: Boolean,
    val Token: String?,
    val Username: String?,
    val IsLockedOut: Boolean,
    val IsNotAllowed: Boolean,
    val RequiresTwoFactor: Boolean
)
