package com.news.domain.mappers

import com.news.domain.models.NewsTag
import com.news.domain.models.NewsTagResponse


fun NewsTagResponse.toNewsTag(): NewsTag {
return NewsTag(TagId, Title, Description, Slug)
}