package com.news.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "news")
data class News(
    @PrimaryKey
    val Id: Int,
    val Title: String,
    val ShortDes: String?,
    val Slug: String,
    val Image: String,
    val Date: String,
    val Source: String,
    val Link: String,


    val Kind: String,
    val CreatedAt: Date?,
)