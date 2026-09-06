package com.app.tintuccongnghe.domain.usecases

import com.app.tintuccongnghe.domain.models.NewsSite
import com.app.tintuccongnghe.domain.repository.INewsSiteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetNewsSiteUseCase @Inject constructor(private val newsSiteRepository: INewsSiteRepository) {
    operator fun invoke(): Flow<List<NewsSite>> {
        return newsSiteRepository.getListNewsSite()
    }
}