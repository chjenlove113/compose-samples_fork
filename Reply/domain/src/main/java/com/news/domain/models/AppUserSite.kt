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
    val AllowEdit: Boolean,
    val OtherCanSee: Boolean,

    var IdentityId: String,
    var AppIdEncrypt: String,
    val lastRefreshTime: Long? = null,
    val nextRefreshTime: Long? = null,
    val itemCount: Int = 0
)

data class AppUserSiteCreateRequest(
    val Id: Int,
    val Name: String,
    val Url: String,
    val Icon: String,
    val Status: Boolean,
    val OtherCanSee: Boolean,
    val Kind: String,
    val IdentityId: String,
    val AppIdEncrypt: String
)

data class AppUserSiteCreateResponse(
    val Id: Int,
    val Mess: String
)
