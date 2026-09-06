package com.app.tintuccongnghe.domain.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CategoryViewModel(
    val App: App,
    val App_Site: AppSite,
    val LstNewsItem: ArrayList<News>?,
    val LstNewsHeader: ArrayList<News>?,
    val AppSiteCateByGroup: ArrayList<AppSiteCateByGroup>?
) : Parcelable

