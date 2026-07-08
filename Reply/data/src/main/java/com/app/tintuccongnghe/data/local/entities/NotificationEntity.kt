package com.app.tintuccongnghe.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val body: String?,
    val newsJson: String?, // Store encoded News object for deep linking
    val timestamp: Long,
    val isRead: Boolean = false
)
