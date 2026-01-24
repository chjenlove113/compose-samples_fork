package com.news.data.di

import com.news.data.api.NewsSiteService
import com.news.data.api.NewsTagService
import com.news.data.api.ShowHomeService
import com.news.data.local.IAppDbService
import com.news.data.repository.NewsSiteRepository
import com.news.data.repository.NewsTagRepository
import com.news.data.repository.ShowHomeRepository
import com.news.domain.repository.INewsSiteRepository
import com.news.domain.repository.INewsTagRepository
import com.news.domain.repository.IShowHomeRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class RepositoryModule {

    @Provides
    @Singleton
    fun provideNewsSiteRepository(
        newsSiteService: NewsSiteService,
        appDbService: IAppDbService
    ): INewsSiteRepository {
        return NewsSiteRepository(newsSiteService, appDbService)
    }

    @Provides
    @Singleton
    fun provideNewsTagRepository(
        newsTagService: NewsTagService,
        appDbService: IAppDbService
    ): INewsTagRepository {
        return NewsTagRepository(newsTagService, appDbService)
    }

    @Provides
    @Singleton
    fun provideShowHomeRepository(showHomeService: ShowHomeService): IShowHomeRepository {
        return ShowHomeRepository(showHomeService)
    }
}