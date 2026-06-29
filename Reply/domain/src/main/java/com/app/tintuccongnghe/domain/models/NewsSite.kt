package com.app.tintuccongnghe.domain.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class NewsSite (
    val Id: Int,
    val Name: String,
    val Key: String,
    val Icon: String?,
    val Url: String?,
    val Bg_Color: String?,
    val Text_Color: String?,
    val Stt: String?,
) : Parcelable