package com.news.data.models

import com.google.gson.annotations.SerializedName

data class NewsTagsRequest(
    @SerializedName("newsId")
    val newsId: String
)
