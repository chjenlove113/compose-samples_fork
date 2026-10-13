package com.app.tintuccongnghe.domain.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class NewsSource(
    val id: String = "",
    val name: String = "",
    val url: String = "",
    val parserRules: List<ParserRule> = emptyList()
) : Parcelable
