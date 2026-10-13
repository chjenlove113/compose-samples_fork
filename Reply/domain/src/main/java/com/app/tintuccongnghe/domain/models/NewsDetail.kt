package com.app.tintuccongnghe.domain.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class NewsDetail(
    val link: String = "",
    val websiteId: String = "",
    val title: String = "",
    val author: String = "",
    val publishDate: String = "",
    val contentHtml: String = ""
) : Parcelable
