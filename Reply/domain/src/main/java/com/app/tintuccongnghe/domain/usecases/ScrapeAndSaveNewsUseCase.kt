package com.app.tintuccongnghe.domain.usecases

import com.app.tintuccongnghe.domain.models.NewsCategory
import com.app.tintuccongnghe.domain.models.NewsItem
import com.app.tintuccongnghe.domain.repository.INewsScrapingRepository
import javax.inject.Inject

class ScrapeAndSaveNewsUseCase @Inject constructor(
    private val repository: INewsScrapingRepository
) {
    suspend operator fun invoke(websiteId: String, category: NewsCategory, page: Int = 1, append: Boolean = false): List<NewsItem> =
        repository.scrapeAndSaveNews(websiteId, category, page, append)
}
