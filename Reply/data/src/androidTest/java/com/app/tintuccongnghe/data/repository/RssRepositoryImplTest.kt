package com.app.tintuccongnghe.data.repository

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.app.tintuccongnghe.data.local.AppDatabase
import com.app.tintuccongnghe.domain.models.RssItem
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RssRepositoryImplTest {
    private lateinit var database: AppDatabase
    private lateinit var repository: RssRepositoryImpl

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = RssRepositoryImpl(database, OkHttpClient())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun saveSyncedRssItems_insertsParentSiteAndPublishesRoomFlow() = runBlocking {
        repository.saveSyncedRssItems(
            listOf(
                RssItem(
                    title = "Synced title",
                    link = "https://example.com/synced",
                    description = "Synced description",
                    pubDate = 123L,
                    siteId = 7,
                    siteGroup = "1",
                    siteKind = "rss",
                    siteName = "Example"
                )
            )
        )

        val sites = database.appUserSiteDao().getAllSites()
        val items = repository.getLatestRssItems().first()

        assertEquals(1, sites.size)
        assertEquals("Example", sites.single().Name)
        assertEquals(1, items.size)
        assertEquals("Synced title", items.single().title)
    }
}
