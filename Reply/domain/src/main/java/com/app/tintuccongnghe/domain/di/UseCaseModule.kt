package com.app.tintuccongnghe.domain.di

import com.app.tintuccongnghe.domain.repository.INewsSiteRepository
import com.app.tintuccongnghe.domain.repository.INewsTagRepository
import com.app.tintuccongnghe.domain.repository.IShowHomeRepository
import com.app.tintuccongnghe.domain.usecases.GetNewsSiteUseCase
import com.app.tintuccongnghe.domain.usecases.GetNewsTagUseCase
import com.app.tintuccongnghe.domain.usecases.ShowHomeUseCase
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
    fun provideGetNewsSiteUseCase(newsSiteRepository: INewsSiteRepository): GetNewsSiteUseCase {
        return GetNewsSiteUseCase(newsSiteRepository)
    }

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