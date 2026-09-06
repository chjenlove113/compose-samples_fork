package com.app.tintuccongnghe.domain.repository

import androidx.paging.PagingData
import com.app.tintuccongnghe.domain.models.News
import com.app.tintuccongnghe.domain.models.ShowHomeDataModel
import kotlinx.coroutines.flow.Flow

interface IShowHomeRepository {
    fun getShowHome(page: Int = 0, site_Slug: String = "", userId: String? = null): Flow<ShowHomeDataModel>

    fun getShowHomePaging(page: Int = 0, site_Slug: String = "", cat_Slug: String = "", userId: String? = null): Flow<PagingData<News>>

}