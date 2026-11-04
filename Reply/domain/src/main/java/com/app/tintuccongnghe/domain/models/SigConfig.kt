package com.app.tintuccongnghe.domain.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class SigConfig(
    val pathname: String = "",
    val secretSalt: String = "",
    val apiKey: String = "",
    val version: String = "",
    val whitelistParams: List<String> = emptyList()
) : Parcelable
