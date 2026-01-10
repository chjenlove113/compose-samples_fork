package com.news.domain.models

data class CategoryViewModel(
    val App: App,
    val App_Site: AppSite,
    val LstNewsItem: ArrayList<News>?,
    val LstNewsHeader: ArrayList<News>?,
    val AppSiteCateByGroup: ArrayList<AppSiteCateByGroup>?
)
