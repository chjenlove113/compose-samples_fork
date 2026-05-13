package com.news.domain.usecases

import com.news.domain.models.NewsTag
import com.news.domain.repository.INewsDetailRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetNewsTagsUseCase @Inject constructor(
    private val repository: INewsDetailRepository
) {
    operator fun invoke(newsId: Int): Flow<Result<List<NewsTag>>> {
        return repository.fetchNewsTags(newsId)
    }
}
