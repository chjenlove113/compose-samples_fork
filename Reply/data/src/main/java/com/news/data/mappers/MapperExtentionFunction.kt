package com.news.data.mappers

import com.news.data.local.entities.News_Tag
import com.news.domain.models.NewsTag
import java.util.Calendar
import java.util.Date

fun NewsTag.toNewsTagEntity(): News_Tag {
    return News_Tag(TagId, Title, Description, Slug, "TOP10", Calendar.getInstance().time)

}
fun News_Tag.toNewsTagModel(): NewsTag {
    return NewsTag(TagId, Title, Description, Slug)

}