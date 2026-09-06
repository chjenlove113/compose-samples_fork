package com.app.tintuccongnghe.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "news_tag")
data class News_Tag(
    @PrimaryKey
    val TagId: Int,
    val Title: String,
    val Description: String?,
    val Slug: String,

    val Kind:String,
    val CreatedAt: Date?,

)
