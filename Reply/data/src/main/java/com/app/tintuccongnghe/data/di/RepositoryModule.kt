package com.app.tintuccongnghe.data.di

import com.app.tintuccongnghe.data.api.AppUserSiteService
import com.app.tintuccongnghe.data.api.AuthService
import com.app.tintuccongnghe.data.api.NewsDetailService
import com.app.tintuccongnghe.data.api.NewsSiteService
import com.app.tintuccongnghe.data.api.NewsTagService
import com.app.tintuccongnghe.data.api.ShowHomeService
import com.app.tintuccongnghe.data.local.AppDatabase
import com.app.tintuccongnghe.data.local.IAppDbService
import com.app.tintuccongnghe.data.repository.AppUserSiteRepositoryImpl
import com.app.tintuccongnghe.data.repository.AuthRepositoryImpl
import com.app.tintuccongnghe.data.repository.NewsDetailRepositoryImpl
import com.app.tintuccongnghe.data.repository.NewsSiteRepository
import com.app.tintuccongnghe.data.repository.NewsTagRepository
import com.app.tintuccongnghe.data.repository.ShowHomeRepository
import com.app.tintuccongnghe.data.repository.SettingsRepositoryImpl
import com.app.tintuccongnghe.domain.repository.IAppUserSiteRepository
import com.app.tintuccongnghe.domain.repository.IAuthRepository
import com.app.tintuccongnghe.domain.repository.INewsDetailRepository
import com.app.tintuccongnghe.domain.repository.INewsSiteRepository
import com.app.tintuccongnghe.domain.repository.INewsTagRepository
import com.app.tintuccongnghe.domain.repository.ISettingsRepository
import com.app.tintuccongnghe.domain.repository.IShowHomeRepository
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