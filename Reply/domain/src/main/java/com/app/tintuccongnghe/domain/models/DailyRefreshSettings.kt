package com.app.tintuccongnghe.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class RefreshTime(
    val hour: Int,
    val minute: Int
)

data class DailyRefreshSettings(
    val enabled: Boolean,
    val times: List<RefreshTime>
)
