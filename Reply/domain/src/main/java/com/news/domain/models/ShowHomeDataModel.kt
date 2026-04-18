package com.news.domain.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ShowHomeDataModel(
    val CategoryViewModel : CategoryViewModel,
    val LstNewsHeader: ArrayList<News>?,
) : Parcelable

