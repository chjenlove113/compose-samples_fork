package com.app.tintuccongnghe.domain.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class NewsTagResponse(
    val AppName: String,
    val TagId: Int,
    val Title: String,
    val Slug: String,

    val Description: String,
    val IsVisible: Boolean,
    val Is_Home: Boolean,
    val Stt: Int,
    val App_Id: Int
) : Parcelable