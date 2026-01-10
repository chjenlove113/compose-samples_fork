package com.news.domain.usecases

import com.news.domain.models.NewsTag
import com.news.domain.repository.INewsTagRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetNewsTagUseCase @Inject constructor(private val newsTagRepository: INewsTagRepository) {
    operator fun invoke(): Flow<List<NewsTag>>{
        return newsTagRepository.getListNewsTag()
    }
}