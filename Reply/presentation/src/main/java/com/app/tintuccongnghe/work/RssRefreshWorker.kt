package com.app.tintuccongnghe.work

import android.content.Context
import android.util.Log
import java.util.concurrent.TimeUnit
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.app.tintuccongnghe.data.local.AppDatabase
import com.app.tintuccongnghe.data.local.entities.RssItemEntity
import com.rometools.rome.feed.synd.SyndFeed
import com.rometools.rome.io.SyndFeedInput
import com.rometools.rome.io.XmlReader
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

@HiltWorker
class RssRefreshWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val appDatabase: AppDatabase
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        Log.d("RssRefreshWorker", "Worker started...")
        val siteDao = appDatabase.appUserSiteDao()
        val rssItemDao = appDatabase.rssItemDao()
        val sites = siteDao.getAllSites()
        Log.d("RssRefreshWorker", "Found ${sites.size} sites to check")

        val client = OkHttpClient()

        sites.forEach { site ->
            try {
                if(site.GROUP == "1" && site.Url != null && site.Url.isNotEmpty()){
                    Log.d("RssRefreshWorker", "Fetching RSS from: ${site.Url}")
                    val request = Request.Builder().url(site.Url).build()
                    client.newCall(request).execute().use { response ->
                        val currentTime = System.currentTimeMillis()
                        val nextTime = currentTime + TimeUnit.MINUTES.toMillis(15)

                        if (response.isSuccessful) {
                            val input = SyndFeedInput()
                            val feed: SyndFeed = input.build(XmlReader(response.body!!.byteStream()))
                            val itemsToInsert = mutableListOf<RssItemEntity>()
                            val totalItems = feed.entries.size

                            siteDao.update(site.copy(
                                lastRefreshTime = currentTime,
                                nextRefreshTime = nextTime,
                                itemCount = totalItems
                            ))
                            
                            feed.entries.forEach { entry ->
                                val link = entry.link ?: ""
                                if (link.isNotEmpty()) {
                                    val existingItem = rssItemDao.getItemByLink(link)
                                    if (existingItem != null) {
                                        Log.d("RssRefreshWorker", "Updating updDate for existing item: $link")
                                        rssItemDao.update(existingItem.copy(updDate = currentTime))
                                    } else {
                                        val contentValue = entry.contents?.firstOrNull()?.value
                                            ?: entry.modules?.filterIsInstance<com.rometools.rome.feed.module.DCModule>()?.firstOrNull()?.description
                                            ?: entry.description?.value

                                        itemsToInsert.add(
                                            RssItemEntity(
                                                title = entry.title ?: "",
                                                link = link,
                                                description = entry.description?.value,
                                                pubDate = entry.publishedDate?.time,
                                                siteId = site.Id,
                                                siteGroup = site.GROUP,
                                                siteKind = site.Kind,
                                                updDate = currentTime,
                                                content = contentValue,
                                                siteName = site.Name
                                            )
                                        )
                                    }
                                }
                            }
                            
                            if (itemsToInsert.isNotEmpty()) {
                                Log.d("RssRefreshWorker", "Inserting ${itemsToInsert.size} new items for site ${site.Id}")
                                rssItemDao.insertAll(itemsToInsert)
                            }
                        } else {
                            Log.e("RssRefreshWorker", "Failed to fetch ${site.Url}: ${response.code}")
                            siteDao.update(site.copy(
                                lastRefreshTime = currentTime, 
                                nextRefreshTime = nextTime,
                                itemCount = 0
                            ))
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("RssRefreshWorker", "Error processing site ${site.Id}", e)
            }
        }
        Log.d("RssRefreshWorker", "Finished RSS refresh")
        Result.success()
    }
}
