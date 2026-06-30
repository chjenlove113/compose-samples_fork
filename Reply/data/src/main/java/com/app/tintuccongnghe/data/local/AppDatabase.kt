package com.app.tintuccongnghe.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.app.tintuccongnghe.data.local.converter.Converters
import com.app.tintuccongnghe.data.local.dao.NewsDao
import com.app.tintuccongnghe.data.local.dao.NewsSiteDao
import com.app.tintuccongnghe.data.local.dao.NewsTagDao
import com.app.tintuccongnghe.data.local.dao.AuthDao
import com.app.tintuccongnghe.data.local.dao.AppUserSiteDao
import com.app.tintuccongnghe.data.local.dao.RssItemDao
import com.app.tintuccongnghe.data.local.entities.AuthEntity
import com.app.tintuccongnghe.data.local.entities.News
import com.app.tintuccongnghe.data.local.entities.News_Site
import com.app.tintuccongnghe.data.local.entities.News_Tag
import com.app.tintuccongnghe.data.local.entities.SavedTagSlug
import com.app.tintuccongnghe.data.local.entities.AppUserSiteEntity
import com.app.tintuccongnghe.data.local.entities.RssItemEntity

@Database(entities = [News_Tag::class, News_Site::class, News::class, AuthEntity::class, SavedTagSlug::class, AppUserSiteEntity::class, RssItemEntity::class], version = 12)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun newsTagDao(): NewsTagDao
    abstract fun newsSiteDao(): NewsSiteDao
    abstract fun newsDao(): NewsDao
    abstract fun authDao(): AuthDao
    abstract fun appUserSiteDao(): AppUserSiteDao
    abstract fun rssItemDao(): RssItemDao
}
