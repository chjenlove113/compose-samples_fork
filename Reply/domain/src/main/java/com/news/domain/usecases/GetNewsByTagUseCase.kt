package com.news.domain.usecases

import com.news.domain.models.News
import com.news.domain.repository.INewsTagRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetNewsByTagUseCase @Inject constructor(private val newsTagRepository: INewsTagRepository) {
    operator fun invoke(tagSlug: String, pageNumber: Int, rowsOfPage: Int): Flow<List<News>> {
        return newsTagRepository.getNewsByTag(tagSlug, pageNumber, rowsOfPage)
    }
}
