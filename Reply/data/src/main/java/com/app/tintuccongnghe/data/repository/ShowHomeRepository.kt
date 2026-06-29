package com.app.tintuccongnghe.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.app.tintuccongnghe.data.api.ShowHomeService
import com.app.tintuccongnghe.domain.models.News
import com.app.tintuccongnghe.domain.models.ShowHomeDataModel
import com.app.tintuccongnghe.domain.repository.IShowHomeRepository
import com.app.tintuccongnghe.utils.AppContants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShowHomeRepository @Inject constructor(private  val showHomeService: ShowHomeService) : IShowHomeRepository {
    override fun getShowHome(page: Int, site_Slug: String, userId: String?): Flow<ShowHomeDataModel> {
        return flow {
            emit(showHomeService.getShowHome(AppContants.app_Slug, page, site_Slug, "", userId))
        }
    }

    override fun getShowHomePaging(
        page: Int,
        site_Slug: String,
        cat_Slug: String,
        userId: String?
    ): Flow<PagingData<News>> {
        return Pager(config = PagingConfig(pageSize = 10, enablePlaceholders = false)
                , pagingSourceFactory = { ShowHomePagingSource(showHomeService, site_Slug, cat_Slug, userId) }
        ).flow
    }

}

 class ShowHomePagingSource(
    val showHomeService: ShowHomeService,
    val siteSlug: String, val catSlug: String,
    val userId: String? = null
): PagingSource<Int, News>() {
     override fun getRefreshKey(state: PagingState<Int, News>): Int? {
         // Return the most recently accessed page key
         return state.anchorPosition?.let { anchorPosition ->
             state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1) ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
         }
     }

     override suspend fun load(params: LoadParams<Int>): LoadResult<Int, News> {
         return try {
             val currentPage = params.key ?: 1 // Start page
             val response = showHomeService.getShowHome(AppContants.app_Slug, currentPage, siteSlug, catSlug, userId)
             val items =   response.CategoryViewModel.LstNewsItem

             LoadResult.Page(
                 data = items as List<News>,
                 prevKey = if (currentPage == 1) null else currentPage - 1,
                 nextKey = if (items?.isEmpty() ?: true) null else currentPage + 1)


         }catch (e: Exception) {
             LoadResult.Error(e)
         }
     }
 }
