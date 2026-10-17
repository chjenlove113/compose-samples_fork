package com.app.tintuccongnghe.data.repository

import android.text.Html
import android.util.Log
import com.app.tintuccongnghe.domain.models.EnumNews
import com.app.tintuccongnghe.domain.models.NewsItem
import com.app.tintuccongnghe.domain.models.ParserRule

fun String.decodeHtml(): String {
    return try {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            Html.fromHtml(this, Html.FROM_HTML_MODE_LEGACY).toString()
        } else {
            @Suppress("DEPRECATION")
            Html.fromHtml(this).toString()
        }
    } catch (_: Exception) {
        this
    }
}

object HtmlParserEngine {

    fun parse(html: String, rules: List<ParserRule>): List<NewsItem> {
        val sortedRules = rules.sortedBy { it.stt }
        
        // Find all CONTAINER rules (enumNews == 0) sorted by stt
        val containerRules = sortedRules.filter { it.enumNews == EnumNews.CONTAINER.value }

        var currentHtmlChunks: String = html

        // Iterate through all CONTAINER rules sequentially by stt
        for (containerRule in containerRules) {
            val containerRegex = try {
                Regex(containerRule.strRegex, RegexOption.DOT_MATCHES_ALL)
            } catch (_: Exception) {
                continue
            }

            if (containerRule.isReplace) {
                try {
                    currentHtmlChunks = containerRegex.replace(currentHtmlChunks, containerRule.textReplace)
                } catch (_: Exception) {}
            } else if (containerRule.isMatches) {
                val matches = containerRegex.findAll(currentHtmlChunks).toList()
                val pos = containerRule.isMatchesPos
                val block = if (pos >= 0 && pos < matches.size) {
                    val m = matches[pos]
                    val groups = m.groups
                    val gPos = containerRule.groupPos
                    val raw = if (gPos >= 0 && gPos < groups.size) groups[gPos]?.value ?: m.value else m.value
                    containerRule.strPrefix + raw + containerRule.strSufix
                } else ""
                currentHtmlChunks = block
            } else {
                val match = containerRegex.find(currentHtmlChunks)
                if (match != null) {
                    val groups = match.groups
                    val pos = containerRule.groupPos
                    val raw = if (pos >= 0 && pos < groups.size) groups[pos]?.value ?: match.value else match.value
                    currentHtmlChunks = containerRule.strPrefix + raw + containerRule.strSufix
                } else {
                    currentHtmlChunks = ""
                }
            }
        }

        // Find MATCH_LIST rules
        val isListRules = sortedRules.filter { it.enumNews == EnumNews.MATCH_LIST.value }

        val itemHtmlList = if (isListRules.isNotEmpty()) {
            val listRule = isListRules.first()
            try {
                val listRegex = Regex(listRule.strRegex, RegexOption.DOT_MATCHES_ALL)
                listRegex.findAll(currentHtmlChunks).map { match ->
                    val groups = match.groups
                    val pos = listRule.groupPos
                    val raw = if (pos >= 0 && pos < groups.size) groups[pos]?.value ?: match.value else match.value
                    listRule.strPrefix + raw + listRule.strSufix
                }.toList()
            } catch (ex: Exception) {
                Log.d("TAG", "parse: Exception: ${ex.message}")
                emptyList()
            }
        } else {
            emptyList()
        }

        val actionRules = sortedRules.filter { it.enumNews != EnumNews.CONTAINER.value && it.enumNews != EnumNews.MATCH_LIST.value }
        val newsItems = mutableListOf<NewsItem>()

        // Group action rules by EnumNews so multiple sequential regex steps can be run per target field
        val groupedActionRules = actionRules.groupBy { it.enumNews }

        // Process each itemHtml
        for (itemHtml in itemHtmlList) {
            var title = ""
            var image = ""
            var link = ""
            var shortDes = ""
            var date = ""
            var subCat = ""
            var isVideo = false

            for ((enumKey, rulesList) in groupedActionRules) {
                val sortedFieldRules = rulesList.sortedBy { it.stt }
                var fieldWorkingText = itemHtml

                for (rule in sortedFieldRules) {
                    if (rule.isReplace) {
                        try {
                            fieldWorkingText = Regex(rule.strRegex, RegexOption.DOT_MATCHES_ALL).replace(fieldWorkingText, rule.textReplace)
                        } catch (_: Exception) {
                            // ignore regex replace error
                        }
                    } else {
                        try {
                            val regex = Regex(rule.strRegex, RegexOption.DOT_MATCHES_ALL)
                            val extracted = if (rule.isMatches) {
                                val matches = regex.findAll(fieldWorkingText).toList()
                                val match = matches.getOrNull(rule.isMatchesPos)
                                if (match != null) {
                                    val groups = match.groups
                                    val pos = rule.groupPos
                                    if (pos >= 0 && pos < groups.size) groups[pos]?.value ?: match.value else match.value
                                } else ""
                            } else {
                                val match = regex.find(fieldWorkingText)
                                if (match != null) {
                                    val groups = match.groups
                                    val pos = rule.groupPos
                                    if (pos >= 0 && pos < groups.size) groups[pos]?.value ?: match.value else match.value
                                } else ""
                            }

                            if (extracted.isNotEmpty()) {
                                fieldWorkingText = rule.strPrefix + extracted + rule.strSufix
                            }
                        } catch (_: Exception) {
                            // ignore parsing error
                        }
                    }
                }

                val finalVal = fieldWorkingText.trim().decodeHtml()
                if (finalVal.isNotEmpty()) {
                    when (EnumNews.fromValue(enumKey)) {
                        EnumNews.TITLE -> title = finalVal
                        EnumNews.IMAGE -> image = fieldWorkingText.trim()
                        EnumNews.LINK -> link = fieldWorkingText.trim().decodeHtml()
                        EnumNews.SHORT_DES -> shortDes = finalVal
                        EnumNews.DATE -> date = finalVal
                        EnumNews.SUB_CAT -> subCat = finalVal
                        EnumNews.IS_VIDEO -> isVideo = finalVal.lowercase() in listOf("true", "1", "yes")
                        else -> {}
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
