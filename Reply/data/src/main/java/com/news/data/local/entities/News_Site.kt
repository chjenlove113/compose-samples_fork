package com.news.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "news_site")
data class News_Site(
    @PrimaryKey
    val Id: Int,
    val Name: String,
    val Key: String,
    val Icon: String?,
    val Url: String?,
    val Bg_Color: String?,
    val Text_Color: String?,
    val Stt: String?,
)