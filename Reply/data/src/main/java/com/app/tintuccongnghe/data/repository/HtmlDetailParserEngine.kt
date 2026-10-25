package com.app.tintuccongnghe.data.repository

import android.text.Html
import com.app.tintuccongnghe.domain.models.DetailRuleDocument
import com.app.tintuccongnghe.domain.models.EnumDetail
import com.app.tintuccongnghe.domain.models.NewsDetail
import com.google.firebase.crashlytics.FirebaseCrashlytics

fun String.decodeHtmlDetail(): String {
    return try {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            Html.fromHtml(this, Html.FROM_HTML_MODE_LEGACY).toString()
        } else {
            @Suppress("DEPRECATION")
            Html.fromHtml(this).toString()
        }
    } catch (e: Exception) {
        FirebaseCrashlytics.getInstance().recordException(e)
        this
    }
}

object HtmlDetailParserEngine {

    fun parse(link: String, websiteId: String, html: String, ruleDocs: List<DetailRuleDocument>): NewsDetail {
        val crashlytics = FirebaseCrashlytics.getInstance()
        if (websiteId.isNotEmpty()) crashlytics.setCustomKey("website_id", websiteId)
        crashlytics.setCustomKey("detail_link", link)

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
                    } catch (e: Exception) {
                        crashlytics.setCustomKey("failed_regex", step.strRegex)
                        crashlytics.setCustomKey("rule_name", step.strRegexName)
                        crashlytics.setCustomKey("detail_rule_id", ruleDoc.id)
                        crashlytics.setCustomKey("parsing_stage", "detail_replace")
                        crashlytics.recordException(e)
                    }
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
                    } catch (e: Exception) {
                        crashlytics.setCustomKey("failed_regex", step.strRegex)
                        crashlytics.setCustomKey("rule_name", step.strRegexName)
                        crashlytics.setCustomKey("detail_rule_id", ruleDoc.id)
                        crashlytics.setCustomKey("parsing_stage", "detail_extract")
                        crashlytics.recordException(e)
                    }
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
