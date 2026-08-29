package com.app.tintuccongnghe.domain.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class RssItem(
    val title: String,
    val link: String,
    val description: String?,
    val pubDate: Long?,
    val siteId: Int,
    val siteGroup: String,
    val siteKind: String,
    val updDate: Long? = null,
    val content: String? = null,
    val siteName: String? = null,
    val imageUrl: String? = null,
    val isFavorite: Boolean = false
) : Parcelable
