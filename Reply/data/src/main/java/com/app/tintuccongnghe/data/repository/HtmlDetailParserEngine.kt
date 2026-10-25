package com.app.tintuccongnghe.data.repository

import android.text.Html
import com.app.tintuccongnghe.domain.models.DetailRuleDocument
import com.app.tintuccongnghe.domain.models.EnumDetail
import com.app.tintuccongnghe.domain.models.NewsDetail
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken

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

// Next.js __NEXT_DATA__ JSON Data Classes
data class NextDataWrapper(
    @SerializedName("props") val props: NextProps? = null
)

data class NextProps(
    @SerializedName("pageProps") val pageProps: NextPageProps? = null
)

data class NextPageProps(
    @SerializedName("resp") val resp: NextResp? = null
)

data class NextResp(
    @SerializedName("data") val data: NextDataContent? = null
)

data class NextDataContent(
    @SerializedName("content") val content: NextDetailContent? = null
)

data class NextDetailContent(
    @SerializedName("title") val title: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("publishedDate") val publishedDate: Long? = null,
    @SerializedName("publisher") val publisher: NextPublisher? = null,
    @SerializedName("bodys") val bodys: List<NextBodyBlock>? = null
)

data class NextPublisher(
    @SerializedName("name") val name: String? = null
)

data class NextBodyBlock(
    @SerializedName("type") val type: String? = null,
    @SerializedName("subType") val subType: String? = null,
    @SerializedName("content") val content: String? = null,
    @SerializedName("contentOrigin") val contentOrigin: String? = null
)

fun parseNextDataJsonToHtml(jsonString: String): String {
    if (jsonString.isBlank()) return ""
    return try {
        val gson = Gson()
        val bodys = try {
            val wrapper = gson.fromJson(jsonString, NextDataWrapper::class.java)
            wrapper?.props?.pageProps?.resp?.data?.content?.bodys
        } catch (_: Exception) {
            null
        } ?: try {
            val content = gson.fromJson(jsonString, NextDetailContent::class.java)
            content?.bodys
        } catch (_: Exception) {
            null
        } ?: try {
            val listType = object : TypeToken<List<NextBodyBlock>>() {}.type
            gson.fromJson<List<NextBodyBlock>>(jsonString, listType)
        } catch (_: Exception) {
            null
        } ?: emptyList()

        val sb = StringBuilder()
        for (block in bodys) {
            when (block.type) {
                "text" -> {
                    val rawContent = block.content ?: ""
                    if (rawContent.isNotBlank()) {
                        when (block.subType) {
                            "media-caption" -> {
                                sb.append("<p class=\"media-caption\" style=\"font-style: italic; font-size: 0.9em; opacity: 0.85; text-align: center; margin-top: 4px; margin-bottom: 16px;\">")
                                  .append(rawContent)
                                  .append("</p>\n")
                            }
                            "body-author" -> {
                                sb.append("<p class=\"body-author\" style=\"text-align: right; font-weight: bold; margin-top: 16px;\">")
                                  .append(rawContent)
                                  .append("</p>\n")
                            }
                            else -> {
                                sb.append("<p class=\"body-text\" style=\"margin-bottom: 12px;\">")
                                  .append(rawContent)
                                  .append("</p>\n")
                            }
                        }
                    }
                }
                "image" -> {
                    val imgUrl = block.contentOrigin?.takeIf { it.isNotBlank() } ?: block.content
                    if (!imgUrl.isNullOrBlank()) {
                        sb.append("<div class=\"image-wrapper\" style=\"margin: 16px 0; text-align: center;\">")
                          .append("<img src=\"").append(imgUrl).append("\" style=\"max-width: 100%; height: auto; border-radius: 12px; display: block; margin: 0 auto;\" />")
                          .append("</div>\n")
                    }
                }
            }
        }
        sb.toString()
    } catch (e: Exception) {
        FirebaseCrashlytics.getInstance().recordException(e)
        jsonString
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
        var description = ""
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

            if (EnumDetail.fromValue(ruleDoc.targetField) == EnumDetail.CONTENT && ruleDoc.getContentBy.equals("JSON", ignoreCase = true)) {
                workingText = parseNextDataJsonToHtml(workingText)
            }

            val finalValue = workingText.trim()
            when (EnumDetail.fromValue(ruleDoc.targetField)) {
                EnumDetail.TITLE -> title = finalValue.decodeHtmlDetail()
                EnumDetail.AUTHOR -> author = finalValue.decodeHtmlDetail()
                EnumDetail.PUBLISH_DATE -> publishDate = finalValue.decodeHtmlDetail()
                EnumDetail.CONTENT -> contentHtml = workingText.trim()
                EnumDetail.DESCRIPTION -> description = finalValue.decodeHtmlDetail()
            }
        }

        return NewsDetail(
            link = link,
            websiteId = websiteId,
            title = title,
            author = author,
            publishDate = publishDate,
            description = description,
            contentHtml = contentHtml
        )
    }
}
