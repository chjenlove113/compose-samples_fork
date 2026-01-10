package com.news.domain.models

data class ShowHomeDataModel(
    val CategoryViewModel : CategoryViewModel,
    val LstNewsHeader: ArrayList<News>?,
)
