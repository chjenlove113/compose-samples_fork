package com.news.domain.models
import kotlinx.serialization.Serializable
@Serializable
data class NewsTag (val TagId: Int,
                    val Title: String,
                    val Description: String?,
                    val Slug: String)