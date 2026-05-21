package com.news.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "auth_info")
data class AuthEntity(
    @PrimaryKey
    val IdentityId: String,
    val Token: String,
    val ExpiresIn: Int,
    val Email: String,
    val RefreshToken: String,
    val Username: String
)
