package com.news.data.mappers

import com.news.data.local.entities.News_Site
import com.news.data.local.entities.News_Tag
import com.news.domain.models.NewsSite
import com.news.domain.models.NewsTag
import java.util.Calendar
import java.util.Date

fun NewsTag.toNewsTagEntity(): News_Tag {
    return News_Tag(TagId, Title, Description, Slug, "TOP10", Calendar.getInstance().time)

}
fun News_Tag.toNewsTagModel(): NewsTag {
    return NewsTag(TagId, Title, Description, Slug)

}


fun News_Site.toNewsSiteModel(): NewsSite {
    return NewsSite(Id, Name, Key, Icon, Url, Bg_Color, Text_Color, Stt)
}

fun NewsSite.toNewsSiteEntity(): News_Site {
    return News_Site(Id, Name, Key, Icon, Url, Bg_Color, Text_Color, Stt)
}


