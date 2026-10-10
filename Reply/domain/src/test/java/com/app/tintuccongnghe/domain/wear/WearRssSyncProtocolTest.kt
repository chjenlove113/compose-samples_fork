package com.app.tintuccongnghe.domain.wear

import com.app.tintuccongnghe.domain.models.RssItem
import org.junit.Assert.assertEquals
import org.junit.Test

class WearRssSyncProtocolTest {
    @Test
    fun encodeAndDecode_limitsPayloadAndPreservesDisplayFields() {
        val items = List(25) { index ->
            RssItem(
                title = "Title $index",
                link = "https://example.com/$index",
                description = "d".repeat(600),
                pubDate = index.toLong(),
                siteId = index,
                siteGroup = "1",
                siteKind = "rss",
                siteName = "Site $index"
            )
        }

        val decoded = WearRssSyncProtocol.decode(WearRssSyncProtocol.encode(items))

        assertEquals(20, decoded.size)
        assertEquals("Title 0", decoded.first().title)
        assertEquals("https://example.com/0", decoded.first().link)
        assertEquals("Site 0", decoded.first().siteName)
        assertEquals(500, decoded.first().description?.length)
    }
}
