package com.app.tintuccongnghe.domain.usecases

import com.app.tintuccongnghe.domain.models.NewsWebsite
import com.app.tintuccongnghe.domain.repository.INewsScrapingRepository
import javax.inject.Inject

class GetWebsitesUseCase @Inject constructor(
    private val repository: INewsScrapingRepository
) {
    suspend operator fun invoke(): List<NewsWebsite> = repository.getWebsites()
}
