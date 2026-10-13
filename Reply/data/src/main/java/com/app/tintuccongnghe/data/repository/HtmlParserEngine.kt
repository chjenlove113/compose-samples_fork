package com.app.tintuccongnghe.data.repository

import com.app.tintuccongnghe.domain.models.EnumNews
import com.app.tintuccongnghe.domain.models.NewsItem
import com.app.tintuccongnghe.domain.models.ParserRule

object HtmlParserEngine {

    fun parse(html: String, rules: List<ParserRule>): List<NewsItem> {
        val sortedRules = rules.sortedBy { it.stt }
        
        // Step 3: Find CONTAINER rule (enumNews == 0)
        val containerRule = sortedRules.find { it.enumNews == EnumNews.CONTAINER.value } ?: return emptyList()
        
        val containerRegex = try {
            Regex(containerRule.strRegex)
        } catch (e: Exception) {
            return emptyList()
        }

        val itemHtmlList = if (containerRule.isMatches) {
            containerRegex.findAll(html).map { match ->
                val groups = match.groups
                val pos = containerRule.groupPos
                if (pos >= 0 && pos < groups.size) groups[pos]?.value ?: match.value else match.value
            }.toList()
        } else {
            val match = containerRegex.find(html)
            if (match != null) {
                val groups = match.groups
                val pos = containerRule.groupPos
                val block = if (pos >= 0 && pos < groups.size) groups[pos]?.value ?: match.value else match.value
                if (block.isNotEmpty()) listOf(block) else emptyList()
            } else {
                emptyList()
            }
        }

        val actionRules = sortedRules.filter { it.enumNews != EnumNews.CONTAINER.value }
        val newsItems = mutableListOf<NewsItem>()

        // Step 4: Process each itemHtml
        for (itemHtml in itemHtmlList) {
            var title = ""
            var image = ""
            var link = ""
            var shortDes = ""
            var date = ""
            var subCat = ""
            var isVideo = false

            for (rule in actionRules) {
                var workingText = itemHtml
                if (rule.isReplace) {
                    try {
                        workingText = Regex(rule.strRegex).replace(workingText, rule.textReplace)
                    } catch (_: Exception) {
                        // ignore regex replace error
                    }
                } else {
                    try {
                        val regex = Regex(rule.strRegex)
                        val extracted = if (rule.isMatches) {
                            val matches = regex.findAll(workingText).toList()
                            val match = matches.getOrNull(rule.isMatchesPos)
                            if (match != null) {
                                val groups = match.groups
                                val pos = rule.groupPos
                                if (pos >= 0 && pos < groups.size) groups[pos]?.value ?: match.value else match.value
                            } else ""
                        } else {
                            val match = regex.find(workingText)
                            if (match != null) {
                                val groups = match.groups
                                val pos = rule.groupPos
                                if (pos >= 0 && pos < groups.size) groups[pos]?.value ?: match.value else match.value
                            } else ""
                        }

                        if (extracted.isNotEmpty()) {
                            val finalVal = rule.strPrefix + extracted + rule.strSufix
                            when (EnumNews.fromValue(rule.enumNews)) {
                                EnumNews.TITLE -> title = finalVal
                                EnumNews.IMAGE -> image = finalVal
                                EnumNews.LINK -> link = finalVal
                                EnumNews.SHORT_DES -> shortDes = finalVal
                                EnumNews.DATE -> date = finalVal
                                EnumNews.SUB_CAT -> subCat = finalVal
                                EnumNews.IS_VIDEO -> isVideo = finalVal.lowercase() in listOf("true", "1", "yes")
                                else -> {}
                            }
                        }
                    } catch (_: Exception) {
                        // ignore parsing error
                    }
                }
            }

            if (title.isNotEmpty() || link.isNotEmpty()) {
                newsItems.add(
                    NewsItem(
                        title = title,
                        image = image,
                        link = link,
                        shortDes = shortDes,
                        date = date,
                        subCat = subCat,
                        isVideo = isVideo
                    )
                )
            }
        }

        return newsItems
    }
}
