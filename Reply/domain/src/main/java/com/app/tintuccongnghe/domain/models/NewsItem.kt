package com.app.tintuccongnghe.domain.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class NewsItem(
    val websiteId: String = "",
    val title: String = "",
    val image: String = "",
    val link: String = "",
    val shortDes: String = "",
    val date: String = "",
    val subCat: String = "",
    val isVideo: Boolean = false,
    val timestamp: Long = 0L
) : Parcelable
