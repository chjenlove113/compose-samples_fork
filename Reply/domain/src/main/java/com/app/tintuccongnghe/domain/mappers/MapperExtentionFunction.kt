package com.app.tintuccongnghe.domain.mappers

import com.app.tintuccongnghe.domain.models.NewsTag
import com.app.tintuccongnghe.domain.models.NewsTagResponse


fun NewsTagResponse.toNewsTag(): NewsTag {
return NewsTag(TagId, Title, Description, Slug)
}