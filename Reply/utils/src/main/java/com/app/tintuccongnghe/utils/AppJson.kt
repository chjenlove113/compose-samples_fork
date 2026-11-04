package com.app.tintuccongnghe.utils

import kotlinx.serialization.json.Json

object AppJson {
    val instance = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }
}
