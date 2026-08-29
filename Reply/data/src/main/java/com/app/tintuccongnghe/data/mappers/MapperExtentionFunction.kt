package com.app.tintuccongnghe.data.mappers

import com.app.tintuccongnghe.data.local.entities.News_Site
import com.app.tintuccongnghe.data.local.entities.News_Tag
import com.app.tintuccongnghe.domain.models.NewsSite
import com.app.tintuccongnghe.domain.models.NewsTag
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

fun com.app.tintuccongnghe.domain.models.News.toNewsEntity(): com.app.tintuccongnghe.data.local.entities.News {
    return com.app.tintuccongnghe.data.local.entities.News(
        Id = Id,
        Title = Title,
        ShortDes = ShortDes,
        Slug = Slug ?: "",
        Image = Image ?: "",
        Date = Date ?: "",
        Source = Source,
        Link = Link ?: "",
        SiteName = App_Site_Name,
        Kind = Kind ?: "",
        CreatedAt = Calendar.getInstance().time
    )
}

fun com.app.tintuccongnghe.data.local.entities.News.toNewsModel(): com.app.tintuccongnghe.domain.models.News {
    return com.app.tintuccongnghe.domain.models.News(
        Id = Id,
        Title = Title,
        Image = Image,
        Slug = Slug,
        ShortDes = ShortDes,
        Date = Date,
        Source = Source,
        Link = Link,
        Is_Video = false, // Default value as not in entity
        Is_Album = false, // Default value as not in entity
        Html = "",
        SubCat = "",
        App_Category_Name = "",
        App_Category_Icon = "",
        App_Category_Slug = "",
        App_Category_TextColor = "",
        App_Category_BackgroundColor = "",
        App_Site_Slug = "",
        App_Site_Name = SiteName ?: "",
        Icon = "",
        App_Site_TextColor = "",
        App_Site_BackgroundColor = "",
        TextColor = "",
        BackgroundColor = "",
        BackgroundColor2 = "",
        Kind = Kind,
        NewsItemChilds = arrayListOf()
    )
}

fun com.app.tintuccongnghe.data.local.entities.RssItemEntity.toRssItem(): com.app.tintuccongnghe.domain.models.RssItem {
    return com.app.tintuccongnghe.domain.models.RssItem(
        title = title,
        link = link,
        description = description,
        pubDate = pubDate,
        siteId = siteId,
        siteGroup = siteGroup,
        siteKind = siteKind,
        updDate = updDate,
        content = content,
        siteName = siteName,
        imageUrl = imageUrl,
        isFavorite = isFavorite
    )
}

fun com.app.tintuccongnghe.domain.models.RssItem.toRssItemEntity(): com.app.tintuccongnghe.data.local.entities.RssItemEntity {
    return com.app.tintuccongnghe.data.local.entities.RssItemEntity(
        title = title,
        link = link,
        description = description,
        pubDate = pubDate,
        siteId = siteId,
        siteGroup = siteGroup,
        siteKind = siteKind,
        updDate = updDate,
        content = content,
        siteName = siteName,
        imageUrl = imageUrl,
        isFavorite = isFavorite
    )
}


