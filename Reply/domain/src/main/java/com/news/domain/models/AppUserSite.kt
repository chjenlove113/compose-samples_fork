package com.news.domain.models

data class AppUserSiteRequest(
    val IdentityId: String,
    val UserIdEncrypt: String,
    val AppIdEncrypt: String
)

data class AppUserSite(
    val Id: Int,
    val Key: String,
    val Name: String,
    val TextColor: String?,
    val BackgroundColor: String?,
    val Url: String,
    val IsActive: Boolean,
    val Kind: String,
    val GROUP: String,
    val Stt: Int,
    val LimitEdit: Boolean,
    val AllowEdit: Boolean
)
