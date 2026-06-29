package com.app.tintuccongnghe.domain.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class NewsSiteReponse(
    val dataModel: ArrayList<NewsSite>,
) : Parcelable