package com.news.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "auth_info")
data class AuthEntity(
    @PrimaryKey
    val Username: String,
    val Token: String?,
    val IsLockedOut: Boolean,
    val IsNotAllowed: Boolean,
    val RequiresTwoFactor: Boolean
)
