package com.app.tintuccongnghe.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.app.tintuccongnghe.domain.models.DetailRuleDocument
import com.app.tintuccongnghe.domain.models.ParserRule
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity(tableName = "detail_rules_table")
data class DetailRuleEntity(
    @PrimaryKey
    val id: String,
    val websiteId: String,
    val name: String,
    val isActive: Boolean = true,
    val targetField: Int = 1,
    val getContentBy: String = "",
    val detailParserRulesJson: String = ""
)

fun DetailRuleEntity.toDomain(gson: Gson = Gson()): DetailRuleDocument {
    val rulesList: List<ParserRule> = try {
        if (detailParserRulesJson.isNotBlank()) {
            val type = object : TypeToken<List<ParserRule>>() {}.type
            gson.fromJson(detailParserRulesJson, type)
        } else emptyList()
    } catch (_: Exception) { emptyList() }

    return DetailRuleDocument(
        id = id,
        name = name,
        isActive = isActive,
        targetField = targetField,
        getContentBy = getContentBy,
        detailParserRules = rulesList
    )
}

fun DetailRuleDocument.toEntity(websiteId: String, gson: Gson = Gson()): DetailRuleEntity {
    return DetailRuleEntity(
        id = id,
        websiteId = websiteId,
        name = name,
        isActive = isActive,
        targetField = targetField,
        getContentBy = getContentBy,
        detailParserRulesJson = gson.toJson(detailParserRules)
    )
}
