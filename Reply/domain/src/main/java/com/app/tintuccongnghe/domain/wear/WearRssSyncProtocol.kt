package com.app.tintuccongnghe.domain.wear

import com.app.tintuccongnghe.domain.models.RssItem
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object WearRssSyncProtocol {
    const val REQUEST_PATH = "/rss/request"
    const val ITEMS_PATH = "/rss/items"

    private const val MAX_ITEMS = 20
    private const val MAX_DESCRIPTION_LENGTH = 500

    private val json = Json {
        ignoreUnknownKeys = true
    }

    fun encode(items: List<RssItem>): ByteArray {
        val payload = items.take(MAX_ITEMS).map { item ->
            SyncedRssItem(
                title = item.title,
                link = item.link,
                description = item.description?.take(MAX_DESCRIPTION_LENGTH),
                pubDate = item.pubDate,
                siteId = item.siteId,
                siteGroup = item.siteGroup,
                siteKind = item.siteKind,
                siteName = item.siteName
            )
        }
        return json.encodeToString(payload).encodeToByteArray()
    }

    fun decode(payload: ByteArray): List<RssItem> {
        return json.decodeFromString<List<SyncedRssItem>>(payload.decodeToString()).map { item ->
            RssItem(
                title = item.title,
                link = item.link,
                description = item.description,
                pubDate = item.pubDate,
                siteId = item.siteId,
                siteGroup = item.siteGroup,
                siteKind = item.siteKind,
                siteName = item.siteName
            )
        }
    }

    @Serializable
    private data class SyncedRssItem(
        val title: String,
        val link: String,
        val description: String?,
        val pubDate: Long?,
        val siteId: Int,
        val siteGroup: String,
        val siteKind: String,
        val siteName: String?
    )
}
