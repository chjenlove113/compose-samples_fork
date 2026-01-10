package com.news.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.news.data.local.converter.Converters
import com.news.data.local.dao.NewsTagDao
import com.news.data.local.entities.News_Tag

@Database(entities = [News_Tag::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun newsTagDao(): NewsTagDao
}