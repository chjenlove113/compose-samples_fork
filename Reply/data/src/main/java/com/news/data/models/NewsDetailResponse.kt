package com.news.data.models

import com.google.gson.annotations.SerializedName

data class NewsDetailResponse(
    @SerializedName("Html")
    val html: String
)
