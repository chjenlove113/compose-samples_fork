package com.news.domain.repository

import com.news.domain.models.ShowHomeDataModel
import kotlinx.coroutines.flow.Flow

interface IShowHomeRepository {
    fun getShowHome(page: Int = 0, site_Slug: String = ""): Flow<ShowHomeDataModel>
}