package com.app.tintuccongnghe.domain.usecases

import androidx.paging.PagingData
import com.app.tintuccongnghe.domain.models.News
import com.app.tintuccongnghe.domain.repository.IShowHomeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShowHomeChildPagingUseCase @Inject constructor(private val showHomeRepository: IShowHomeRepository) {
    operator fun invoke(page: Int, site_Slug: String, cat_Slug: String, userId: String? = null): Flow<PagingData<News>>{
        return showHomeRepository.getShowHomePaging(page, site_Slug, cat_Slug, userId)
    }
}
