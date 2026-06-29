package com.app.tintuccongnghe.domain.models
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
@Parcelize
@Serializable
data class NewsTag (val TagId: Int,
                    val Title: String,
                    val Description: String?,
                    val Slug: String) : Parcelable