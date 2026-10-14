package com.app.tintuccongnghe.data.repository

import android.text.Html
import com.app.tintuccongnghe.domain.models.DetailRuleDocument
import com.app.tintuccongnghe.domain.models.EnumDetail
import com.app.tintuccongnghe.domain.models.NewsDetail

fun String.decodeHtmlDetail(): String {
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

object HtmlDetailParserEngine {

    fun parse(link: String, websiteId: String, html: String, ruleDocs: List<DetailRuleDocument>): NewsDetail {
        var title = ""
        var author = ""
        var publishDate = ""
        var contentHtml = ""

        val activeRules = ruleDocs.filter { it.isActive }

        for (ruleDoc in activeRules) {
            val sortedSteps = ruleDoc.detailParserRules.sortedBy { it.stt }
            var workingText = html

            for (step in sortedSteps) {
                if (step.isReplace) {
                    try {
                        workingText = Regex(step.strRegex, RegexOption.DOT_MATCHES_ALL).replace(workingText, step.textReplace)
                    } catch (_: Exception) {}
                } else {
                    try {
                        val regex = Regex(step.strRegex, RegexOption.DOT_MATCHES_ALL)
                        val extracted = if (step.isMatches) {
                            val matches = regex.findAll(workingText).toList()
                            val match = matches.getOrNull(step.isMatchesPos)
                            if (match != null) {
                                val groups = match.groups
                                val pos = step.groupPos
                                if (pos >= 0 && pos < groups.size) groups[pos]?.value ?: match.value else match.value
                            } else ""
                        } else {
                            val match = regex.find(workingText)
                            if (match != null) {
                                val groups = match.groups
                                val pos = step.groupPos
                                if (pos >= 0 && pos < groups.size) groups[pos]?.value ?: match.value else match.value
                            } else ""
                        }

                        if (extracted.isNotEmpty()) {
                            workingText = step.strPrefix + extracted + step.strSufix
                        }
                    } catch (_: Exception) {}
                }
            }

            val finalValue = workingText.trim()
            when (EnumDetail.fromValue(ruleDoc.targetField)) {
                EnumDetail.TITLE -> title = finalValue.decodeHtmlDetail()
                EnumDetail.AUTHOR -> author = finalValue.decodeHtmlDetail()
                EnumDetail.PUBLISH_DATE -> publishDate = finalValue.decodeHtmlDetail()
                EnumDetail.CONTENT -> contentHtml = workingText.trim()
            }
        }

        return NewsDetail(
            link = link,
            websiteId = websiteId,
            title = title,
            author = author,
            publishDate = publishDate,
            contentHtml = contentHtml
        )
    }
}
