package com.app.tintuccongnghe.domain.usecases

import com.app.tintuccongnghe.domain.models.NewsCategory
import com.app.tintuccongnghe.domain.repository.INewsScrapingRepository
import javax.inject.Inject

class GetCategoriesUseCase @Inject constructor(
    private val repository: INewsScrapingRepository
) {
    suspend operator fun invoke(websiteId: String): List<NewsCategory> = repository.getCategories(websiteId)
}
