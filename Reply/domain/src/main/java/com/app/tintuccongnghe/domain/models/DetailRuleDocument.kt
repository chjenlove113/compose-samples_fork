package com.app.tintuccongnghe.domain.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class DetailRuleDocument(
    val id: String = "",
    val name: String = "",
    val isActive: Boolean = true,
    val targetField: Int = 1,
    val detailParserRules: List<ParserRule> = emptyList()
) : Parcelable
