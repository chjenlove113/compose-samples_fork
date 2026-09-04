package com.app.tintuccongnghe.domain.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ShowHomeDataModel(
    // Gson can legally produce null when the server omits this object or returns it as null.
    val CategoryViewModel: CategoryViewModel?,
    val LstNewsHeader: ArrayList<News>?,
) : Parcelable

