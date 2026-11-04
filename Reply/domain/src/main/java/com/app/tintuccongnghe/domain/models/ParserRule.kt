package com.app.tintuccongnghe.domain.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class ParserRule(
    val stt: Int = 0,
    val enumNews: Int = 0,
    val strRegexName: String = "",
    val strRegex: String = "",
    val isMatches: Boolean = false,
    val isMatchesPos: Int = 0,
    val groupPos: Int = 3,
    val strPrefix: String = "",
    val strSufix: String = "",
    val isReplace: Boolean = false,
    val textReplace: String = ""
) : Parcelable
