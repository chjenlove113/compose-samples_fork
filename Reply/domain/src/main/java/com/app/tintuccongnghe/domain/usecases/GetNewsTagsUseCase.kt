package com.app.tintuccongnghe.domain.usecases

import com.app.tintuccongnghe.domain.models.NewsTag
import com.app.tintuccongnghe.domain.repository.INewsDetailRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetNewsTagsUseCase @Inject constructor(
    private val repository: INewsDetailRepository
) {
    operator fun invoke(newsId: Int): Flow<Result<List<NewsTag>>> {
        return repository.fetchNewsTags(newsId)
    }
}
