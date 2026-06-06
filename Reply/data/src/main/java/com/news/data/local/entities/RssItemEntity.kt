package com.news.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "rss_items",
    primaryKeys = ["link", "siteId", "siteGroup", "siteKind"],
    foreignKeys = [
        ForeignKey(
            entity = AppUserSiteEntity::class,
            parentColumns = ["Id", "GROUP", "Kind"],
            childColumns = ["siteId", "siteGroup", "siteKind"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["siteId", "siteGroup", "siteKind"])]
)
data class RssItemEntity(
    val title: String,
    val link: String,
    val description: String?,
    val pubDate: Long?,
    val siteId: Int,
    val siteGroup: String,
    val siteKind: String,
    val updDate: Long? = null
)
