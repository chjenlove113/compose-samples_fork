package com.app.tintuccongnghe.data.local.entities

import androidx.room.Entity
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(
    tableName = "news_details",
    primaryKeys = ["link"]
)
data class NewsDetailEntity(
    val link: String,
    val websiteId: String,
    val title: String,
    val author: String,
    val publishDate: String,
    val contentHtml: String
) : Parcelable
