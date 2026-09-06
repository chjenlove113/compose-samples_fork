package com.app.tintuccongnghe.domain.models

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
    val Password: String,
    val Email: String = "",
    val Provider: String = "",
    val IdToken: String = "",
    val RemoteIpAddress: String = "",
    val Externalauth: Boolean = false
)

data class LoginResponse(
    val UserName: String,

    val AccessToken: AccessToken,
    val RefreshToken:String,
    val Errors: List<AuthError>,
    val IdentityId:String,
    val Email:String,
    val Private_Code: String
)

data class AccessToken(
    val Token:String,
    val  ExpiresIn : Int
)

data class DeleteAccountRequest(
    val IdentityId: String,
    val Email: String,
    val UserName: String
)

data class DeleteAccountResponse(
    val Success: Boolean,
    val Message: String? = null
)
