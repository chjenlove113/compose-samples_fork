package com.news.domain.models

data class AppSite(
    val Id :Int,
    val slug: String,
    val Key:String,
    val Name:String,
    val Icon:String?,
    val TextColor:String?,
    val BackgroundColor:String?,
    val ShortDesc:String?,
)
