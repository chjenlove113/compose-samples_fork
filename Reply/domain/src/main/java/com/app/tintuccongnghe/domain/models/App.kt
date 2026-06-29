package com.app.tintuccongnghe.domain.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class App(
    val AppId: Int,
    val AppName: String,
    val Slug: String,
    val ShortDesc: String?
) : Parcelable

