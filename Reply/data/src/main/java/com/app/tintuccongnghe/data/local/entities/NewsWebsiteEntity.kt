package com.app.tintuccongnghe.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.app.tintuccongnghe.domain.models.NewsWebsite

@Entity(tableName = "news_websites_table")
data class NewsWebsiteEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val isActive: Boolean = true
)

fun NewsWebsiteEntity.toDomain() = NewsWebsite(
    id = id,
    name = name,
    baseUrl = "",
    isActive = isActive,
    sigConfig = null
)

fun NewsWebsite.toEntity() = NewsWebsiteEntity(
    id = id,
    name = name,
    isActive = isActive
)
