package com.news.data.repository

import com.news.data.api.ShowHomeService
import com.news.domain.models.ShowHomeDataModel
import com.news.domain.repository.IShowHomeRepository
import com.news.utils.AppContants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShowHomeRepository @Inject constructor(private  val showHomeService: ShowHomeService) : IShowHomeRepository {
    override fun getShowHome(page: Int, site_Slug: String): Flow<ShowHomeDataModel> {
        return flow {
            emit(showHomeService.getShowHome(AppContants.app_Slug, page, site_Slug))
        }
    }
}