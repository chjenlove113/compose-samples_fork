package com.app.tintuccongnghe.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.app.tintuccongnghe.domain.models.NewsCategory
import com.app.tintuccongnghe.domain.models.ParserRule
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity(tableName = "news_categories_table")
data class NewsCategoryEntity(
    @PrimaryKey
    val id: String,
    val websiteId: String,
    val name: String,
    val url: String,
    val pageTemplate: String = "",
    val httpMethod: String = "GET",
    val postBodyTemplate: String? = null,
    val jsonResponseKey: String? = null,
    val headersJson: String = "",
    val queryParamsJson: String = "",
    val parserRulesJson: String = "",
    val isActive: Boolean = true
)

fun NewsCategoryEntity.toDomain(gson: Gson = Gson()): NewsCategory {
    val headersMap: Map<String, String> = try {
        if (headersJson.isNotBlank()) {
            val type = object : TypeToken<Map<String, String>>() {}.type
            gson.fromJson(headersJson, type)
        } else emptyMap()
    } catch (_: Exception) { emptyMap() }

    val queryParamsMap: Map<String, String> = try {
        if (queryParamsJson.isNotBlank()) {
            val type = object : TypeToken<Map<String, String>>() {}.type
            gson.fromJson(queryParamsJson, type)
        } else emptyMap()
    } catch (_: Exception) { emptyMap() }

    val rulesList: List<ParserRule> = try {
        if (parserRulesJson.isNotBlank()) {
            val type = object : TypeToken<List<ParserRule>>() {}.type
            gson.fromJson(parserRulesJson, type)
        } else emptyList()
    } catch (_: Exception) { emptyList() }

    return NewsCategory(
        id = id,
        name = name,
        url = url,
        pageTemplate = pageTemplate,
        httpMethod = httpMethod,
        postBodyTemplate = postBodyTemplate,
        jsonResponseKey = jsonResponseKey,
        headers = headersMap,
        queryParams = queryParamsMap,
        isActive = isActive,
        parserRules = rulesList
    )
}

fun NewsCategory.toEntity(websiteId: String, gson: Gson = Gson()): NewsCategoryEntity {
    return NewsCategoryEntity(
        id = id,
        websiteId = websiteId,
        name = name,
        url = url,
        pageTemplate = pageTemplate,
        httpMethod = httpMethod,
        postBodyTemplate = postBodyTemplate,
        jsonResponseKey = jsonResponseKey,
        headersJson = gson.toJson(headers),
        queryParamsJson = gson.toJson(queryParams),
        parserRulesJson = gson.toJson(parserRules),
        isActive = isActive
    )
}
