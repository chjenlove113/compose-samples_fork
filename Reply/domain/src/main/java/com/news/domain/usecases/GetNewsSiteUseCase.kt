package com.news.domain.usecases

import com.news.domain.models.NewsSite
import com.news.domain.repository.INewsSiteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetNewsSiteUseCase @Inject constructor(private val newsSiteRepository: INewsSiteRepository) {
    operator fun invoke(): Flow<List<NewsSite>> {
        return newsSiteRepository.getListNewsSite()
    }
}