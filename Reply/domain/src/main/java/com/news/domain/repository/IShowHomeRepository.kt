package com.news.domain.repository

import androidx.paging.PagingData
import com.news.domain.models.News
import com.news.domain.models.ShowHomeDataModel
import kotlinx.coroutines.flow.Flow

interface IShowHomeRepository {
    fun getShowHome(page: Int = 0, site_Slug: String = "", userId: String? = null): Flow<ShowHomeDataModel>

    fun getShowHomePaging(page: Int = 0, site_Slug: String = "", cat_Slug: String = "", userId: String? = null): Flow<PagingData<News>>

}