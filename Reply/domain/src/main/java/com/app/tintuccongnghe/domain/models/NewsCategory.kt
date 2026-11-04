package com.app.tintuccongnghe.domain.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class NewsCategory(
    val id: String = "",
    val name: String = "",
    val url: String = "",
    val pageTemplate: String = "",
    val httpMethod: String = "GET",
    val postBodyTemplate: String? = null,
    val jsonResponseKey: String? = null,
    val headers: Map<String, String> = emptyMap(),
    val queryParams: Map<String, String> = emptyMap(),
    val isActive: Boolean = true,
    val parserRules: List<ParserRule> = emptyList()
) : Parcelable
