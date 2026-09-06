package com.example.reply.wear.tile

import androidx.wear.protolayout.ResourceBuilders
import androidx.wear.protolayout.TimelineBuilders
import androidx.wear.protolayout.material3.Typography
import androidx.wear.protolayout.material3.materialScope
import androidx.wear.protolayout.material3.primaryLayout
import androidx.wear.protolayout.material3.text
import androidx.wear.protolayout.types.layoutString
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.TileBuilders
import com.app.tintuccongnghe.domain.repository.IRssRepository
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.tiles.SuspendingTileService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@OptIn(ExperimentalHorologistApi::class)
@AndroidEntryPoint
class RssTileService : SuspendingTileService() {

    @Inject
    lateinit var rssRepository: IRssRepository

    override suspend fun resourcesRequest(
        requestParams: RequestBuilders.ResourcesRequest
    ): ResourceBuilders.Resources {
        return ResourceBuilders.Resources.Builder().setVersion("1").build()
    }

    override suspend fun tileRequest(
        requestParams: RequestBuilders.TileRequest
    ): TileBuilders.Tile {
        val lastRssItems = rssRepository.fetchLatestRssItems()
        val firstItemTitle = lastRssItems.firstOrNull()?.title ?: "No RSS items"

        return TileBuilders.Tile.Builder()
            .setResourcesVersion("1")
            .setTileTimeline(
                TimelineBuilders.Timeline.fromLayoutElement(
                    materialScope(this, requestParams.deviceConfiguration) {
                        primaryLayout(
                            mainSlot = {
                                text(firstItemTitle.layoutString, typography = Typography.BODY_LARGE)
                            }
                        )
                    }
                )
            )
            .build()
    }
}
