package com.app.tintuccongnghe.data.repository

import com.app.tintuccongnghe.domain.models.DetailRuleDocument
import com.app.tintuccongnghe.domain.models.EnumDetail
import com.app.tintuccongnghe.domain.models.NewsDetail

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
                EnumDetail.TITLE -> title = finalValue
                EnumDetail.AUTHOR -> author = finalValue
                EnumDetail.PUBLISH_DATE -> publishDate = finalValue
                EnumDetail.CONTENT -> contentHtml = finalValue
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
