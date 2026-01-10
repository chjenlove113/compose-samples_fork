package com.news.domain.di

import com.news.domain.repository.INewsTagRepository
import com.news.domain.repository.IShowHomeRepository
import com.news.domain.usecases.GetNewsTagUseCase
import com.news.domain.usecases.ShowHomeUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class UseCaseModule {

    @Provides
    @Singleton
    fun provideGetNewsTagUseCase(newsTagRepository: INewsTagRepository): GetNewsTagUseCase {
        return GetNewsTagUseCase(newsTagRepository)
    }
    @Provides
    @Singleton
    fun provideShowHomeUseCase(showHomeRepository: IShowHomeRepository): ShowHomeUseCase {
        return ShowHomeUseCase(showHomeRepository)
    }
}