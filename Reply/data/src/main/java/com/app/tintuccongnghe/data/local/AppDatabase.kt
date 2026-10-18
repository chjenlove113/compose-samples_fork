package com.app.tintuccongnghe.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.app.tintuccongnghe.data.local.converter.Converters
import com.app.tintuccongnghe.data.local.dao.*
import com.app.tintuccongnghe.data.local.entities.*

@Database(
    entities = [
        News_Tag::class,
        News_Site::class,
        News::class,
        AuthEntity::class,
        SavedTagSlug::class,
        AppUserSiteEntity::class,
        RssItemEntity::class,
        NotificationEntity::class,
        ScrapedNewsEntity::class,
        NewsDetailEntity::class
    ],
    version = 19
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun newsTagDao(): NewsTagDao
    abstract fun newsSiteDao(): NewsSiteDao
    abstract fun newsDao(): NewsDao
    abstract fun authDao(): AuthDao
    abstract fun appUserSiteDao(): AppUserSiteDao
    abstract fun rssItemDao(): RssItemDao
    abstract fun notificationDao(): NotificationDao
    abstract fun scrapedNewsDao(): ScrapedNewsDao
    abstract fun newsDetailDao(): NewsDetailDao
}
