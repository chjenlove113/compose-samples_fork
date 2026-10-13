package com.app.tintuccongnghe.domain.repository

import com.app.tintuccongnghe.domain.models.NewsCategory
import com.app.tintuccongnghe.domain.models.NewsDetail
import com.app.tintuccongnghe.domain.models.NewsItem
import com.app.tintuccongnghe.domain.models.NewsWebsite
import kotlinx.coroutines.flow.Flow

interface INewsScrapingRepository {
    suspend fun getWebsites(): List<NewsWebsite>
    suspend fun getCategories(websiteId: String): List<NewsCategory>
    suspend fun scrapeAndSaveNews(websiteId: String, category: NewsCategory, page: Int = 1, append: Boolean = false): List<NewsItem>
    suspend fun getNewsDetail(websiteId: String, link: String): NewsDetail
    fun getHomeNews(): Flow<List<NewsItem>>
    fun getNewsForWebsite(websiteId: String): Flow<List<NewsItem>>
    fun getCachedNews(categoryId: String): Flow<List<NewsItem>>
}
