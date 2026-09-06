package com.app.tintuccongnghe.data.models

import com.google.gson.annotations.SerializedName

data class NewsDetailRequest(
    @SerializedName("link")
    val link: String,
    @SerializedName("cat")
    val cat: String,
    @SerializedName("id")
    val id: Int,
    @SerializedName("html")
    val html: String,
    @SerializedName("slug")
    val slug: String
)
