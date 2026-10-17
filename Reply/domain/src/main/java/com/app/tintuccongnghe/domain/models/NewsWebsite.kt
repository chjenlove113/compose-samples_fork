package com.app.tintuccongnghe.domain.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class NewsWebsite(
    val id: String = "",
    val name: String = "",
    val baseUrl: String = "",
    val isActive: Boolean = true,
    val sigConfig: SigConfig? = null
) : Parcelable
