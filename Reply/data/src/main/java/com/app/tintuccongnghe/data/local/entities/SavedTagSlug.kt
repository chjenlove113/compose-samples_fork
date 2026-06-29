package com.app.tintuccongnghe.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_tag_slugs")
data class SavedTagSlug(
    @PrimaryKey
    val slug: String
)
