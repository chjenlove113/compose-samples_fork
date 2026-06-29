package com.app.tintuccongnghe.data.models

data class NewsByTagRequest(
    val tagSlug: String,
    val userId: String = "",
    val pageNumber: Int,
    val rowsOfPage: Int
)
