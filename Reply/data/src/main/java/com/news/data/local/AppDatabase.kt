package com.news.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.news.data.local.converter.Converters
import com.news.data.local.dao.NewsDao
import com.news.data.local.dao.NewsSiteDao
import com.news.data.local.dao.NewsTagDao
import com.news.data.local.dao.AuthDao
import com.news.data.local.entities.AuthEntity
import com.news.data.local.entities.News
import com.news.data.local.entities.News_Site
import com.news.data.local.entities.News_Tag

@Database(entities = [News_Tag::class, News_Site::class, News::class, AuthEntity::class], version = 4)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun newsTagDao(): NewsTagDao
    abstract fun newsSiteDao(): NewsSiteDao
    abstract fun newsDao(): NewsDao
    abstract fun authDao(): AuthDao
}
