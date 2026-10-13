package com.app.tintuccongnghe.domain.models

enum class EnumDetail(val value: Int) {
    TITLE(1),
    AUTHOR(2),
    PUBLISH_DATE(3),
    CONTENT(4);

    companion object {
        fun fromValue(value: Int): EnumDetail = entries.find { it.value == value } ?: CONTENT
    }
}
