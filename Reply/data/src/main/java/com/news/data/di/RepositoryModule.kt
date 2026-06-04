package com.news.data.di

import com.news.data.api.AppUserSiteService
import com.news.data.api.AuthService
import com.news.data.api.NewsDetailService
import com.news.data.api.NewsSiteService
import com.news.data.api.NewsTagService
import com.news.data.api.ShowHomeService
import com.news.data.local.AppDatabase
import com.news.data.local.IAppDbService
import com.news.data.repository.AppUserSiteRepositoryImpl
import com.news.data.repository.AuthRepositoryImpl
import com.news.data.repository.NewsDetailRepositoryImpl
import com.news.data.repository.NewsSiteRepository
import com.news.data.repository.NewsTagRepository
import com.news.data.repository.ShowHomeRepository
import com.news.data.repository.SettingsRepositoryImpl
import com.news.domain.repository.IAppUserSiteRepository
import com.news.domain.repository.IAuthRepository
import com.news.domain.repository.INewsDetailRepository
import com.news.domain.repository.INewsSiteRepository
import com.news.domain.repository.INewsTagRepository
import com.news.domain.repository.ISettingsRepository
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

    @Provides
    @Singleton
    fun provideNewsDetailRepository(newsDetailService: NewsDetailService): INewsDetailRepository {
        return NewsDetailRepositoryImpl(newsDetailService)
    }

    @Provides
    @Singleton
    fun provideAuthRepository(
        authService: AuthService,
        appDatabase: AppDatabase
    ): IAuthRepository {
        return AuthRepositoryImpl(authService, appDatabase)
    }

    @Provides
    @Singleton
    fun provideSettingsRepository(settingsRepositoryImpl: SettingsRepositoryImpl): ISettingsRepository {
        return settingsRepositoryImpl
    }

    @Provides
    @Singleton
    fun provideAppUserSiteRepository(
        appUserSiteService: AppUserSiteService,
        appDatabase: AppDatabase
    ): IAppUserSiteRepository {
        return AppUserSiteRepositoryImpl(appUserSiteService, appDatabase)
    }
}