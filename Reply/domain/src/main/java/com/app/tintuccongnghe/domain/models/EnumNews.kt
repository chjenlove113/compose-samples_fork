package com.app.tintuccongnghe.domain.models

enum class EnumNews(val value: Int) {
    CONTAINER(0),
    TITLE(1),
    IMAGE(2),
    LINK(3),
    SHORT_DES(4),
    DATE(5),
    SUB_CAT(6),
    IS_VIDEO(7),
    MATCH_LIST(9);


    companion object {
        fun fromValue(value: Int): EnumNews = entries.find { it.value == value } ?: CONTAINER
    }
}
