package com.app.tintuccongnghe.data.local.entities

import androidx.room.Entity
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(
    tableName = "scraped_news",
    primaryKeys = ["link", "categoryId"]
)
data class ScrapedNewsEntity(
    val websiteId: String,
    val categoryId: String,
    val title: String,
    val image: String,
    val link: String,
    val shortDes: String,
    val date: String,
    val subCat: String,
    val isVideo: Boolean
) : Parcelable
