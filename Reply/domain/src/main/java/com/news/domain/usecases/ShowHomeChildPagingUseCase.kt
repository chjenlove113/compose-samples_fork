package com.news.domain.usecases

import androidx.paging.PagingData
import com.news.domain.models.News
import com.news.domain.repository.IShowHomeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShowHomeChildPagingUseCase @Inject constructor(private val showHomeRepository: IShowHomeRepository) {
    operator fun invoke(page: Int, site_Slug: String, cat_Slug: String, userId: String? = null): Flow<PagingData<News>>{
        return showHomeRepository.getShowHomePaging(page, site_Slug, cat_Slug, userId)
    }
}
