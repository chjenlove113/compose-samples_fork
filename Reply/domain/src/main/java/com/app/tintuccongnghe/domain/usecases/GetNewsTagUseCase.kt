package com.app.tintuccongnghe.domain.usecases

import com.app.tintuccongnghe.domain.models.NewsTag
import com.app.tintuccongnghe.domain.repository.INewsTagRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetNewsTagUseCase @Inject constructor(private val newsTagRepository: INewsTagRepository) {
    operator fun invoke(): Flow<List<NewsTag>>{
        return newsTagRepository.getListNewsTag()
    }
}