package com.news.data.di

import android.content.Context
import androidx.room.Room
import com.news.data.local.AppDatabase
import com.news.data.local.AppDbService
import com.news.data.local.IAppDbService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DatabaseModule {

    @DatabaseName
    @Provides
    fun provideDatabaseName(): String = "news-database"

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context,
        @DatabaseName databaseName: String,
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            databaseName
        ).fallbackToDestructiveMigration(dropAllTables = true)
            .build()
    }

    @Provides
    @Singleton
    fun provideDatabaseService(appDatabase: AppDatabase): IAppDbService {
        return AppDbService(appDatabase)
    }
}