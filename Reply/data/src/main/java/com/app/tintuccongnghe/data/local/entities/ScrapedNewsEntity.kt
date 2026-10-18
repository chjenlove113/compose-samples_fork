package com.app.tintuccongnghe.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "scraped_news")
data class ScrapedNewsEntity(
    @PrimaryKey
    val link: String,
    val websiteId: String,
    val categoryId: String,
    val title: String,
    val image: String,
    val shortDes: String,
    val date: String,
    val subCat: String,
    val isVideo: Boolean,
    val timestamp: Long = 0L,
    val pageNumber: Int = 1,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) : Parcelable
