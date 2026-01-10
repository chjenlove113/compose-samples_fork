package com.news.domain.models

data class AppSiteCateByGroup(
    val Id: Int,
    val Name: String,

    val LstNewsItem : ArrayList<News>?,
    val LstNews : ArrayList<News>?,

    val Slug: String,
    val Key: String,
    val Icon: String?,
    val TextColor: String?,
    val BackgroundColor: String?,
    val ShortDesc: String?,
    val Title: String?,
    val Link: String?,

    val LstNewsHeader : ArrayList<News>?,

)