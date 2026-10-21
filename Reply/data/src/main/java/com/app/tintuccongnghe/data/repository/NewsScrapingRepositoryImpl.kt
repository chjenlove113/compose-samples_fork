package com.app.tintuccongnghe.data.repository

import com.app.tintuccongnghe.data.api.HtmlScrapingService
import com.app.tintuccongnghe.data.local.AppDatabase
import com.app.tintuccongnghe.data.local.entities.NewsDetailEntity
import com.app.tintuccongnghe.data.local.entities.ScrapedNewsEntity
import com.app.tintuccongnghe.data.local.entities.toEntity
import com.app.tintuccongnghe.domain.models.DetailRuleDocument
import com.app.tintuccongnghe.domain.models.NewsCategory
import com.app.tintuccongnghe.domain.models.NewsDetail
import com.app.tintuccongnghe.domain.models.NewsItem
import com.app.tintuccongnghe.domain.models.NewsWebsite
import com.app.tintuccongnghe.domain.models.ParserRule
import com.app.tintuccongnghe.domain.models.SigConfig
import com.app.tintuccongnghe.domain.repository.INewsScrapingRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

class NewsScrapingRepositoryImpl @Inject constructor(
    private val htmlScrapingService: HtmlScrapingService,
    private val firestore: FirebaseFirestore,
    appDatabase: AppDatabase
) : INewsScrapingRepository {

    private val scrapedNewsDao = appDatabase.scrapedNewsDao()
    private val newsDetailDao = appDatabase.newsDetailDao()
    private val newsWebsiteDao = appDatabase.newsWebsiteDao()

    override suspend fun getWebsites(): List<NewsWebsite> {
        try {
            val snapshot = firestore.collection("news_websites").get().await()
            val remoteWebsites = snapshot.documents.mapNotNull { doc ->
                val id = doc.id
                val name = doc.getString("name") ?: doc.getString("strName") ?: ""
                val baseUrl = doc.getString("baseUrl") ?: doc.getString("url") ?: ""
                val remoteIsActive = doc.getBoolean("isActive") ?: true

                val sigMap = doc.get("sigConfig") as? Map<*, *>
                val sigConfig = if (sigMap != null) {
                    SigConfig(
                        pathname = sigMap["pathname"] as? String ?: "",
                        secretSalt = sigMap["secretSalt"] as? String ?: "",
                        apiKey = sigMap["apiKey"] as? String ?: "",
                        version = sigMap["version"] as? String ?: "",
                        whitelistParams = (sigMap["whitelistParams"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                    )
                } else null

                NewsWebsite(
                    id = id,
                    name = name,
                    baseUrl = baseUrl,
                    isActive = remoteIsActive,
                    sigConfig = sigConfig
                )
            }

            val localWebsites = newsWebsiteDao.getAllWebsitesList()
            remoteWebsites.forEach { remote ->
                val existing = localWebsites.find { it.id == remote.id }
                val activeStatus = existing?.isActive ?: remote.isActive
                newsWebsiteDao.insert(remote.copy(isActive = activeStatus).toEntity())
            }
        } catch (_: Exception) {}

        return try {
            newsWebsiteDao.getAllWebsitesList().map { entity ->
                NewsWebsite(
                    id = entity.id,
                    name = entity.name,
                    baseUrl = "",
                    isActive = entity.isActive,
                    sigConfig = null
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    override suspend fun updateWebsiteActive(id: String, isActive: Boolean) {
        newsWebsiteDao.updateActiveStatus(id, isActive)
    }

    override suspend fun getCategories(websiteId: String): List<NewsCategory> {
        return try {
            val snapshot = firestore.collection("news_websites")
                .document(websiteId)
                .collection("categories")
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                val id = doc.id
                val name = doc.getString("name") ?: ""
                val url = doc.getString("url") ?: ""
                val pageTemplate = doc.getString("pageTemplate") ?: doc.getString("strPageTemplate") ?: ""
                val httpMethod = doc.getString("httpMethod") ?: doc.getString("strHttpMethod") ?: "GET"
                val postBodyTemplate = doc.getString("postBodyTemplate") ?: doc.getString("strPostBodyTemplate")
                val jsonResponseKey = doc.getString("jsonResponseKey") ?: doc.getString("strJsonResponseKey")
                val isActive = doc.getBoolean("isActive") ?: true

                val headersMap = mutableMapOf<String, String>()
                val rawHeaders = doc.get("headers")
                if (rawHeaders is Map<*, *>) {
                    for ((k, v) in rawHeaders) {
                        if (k is String && v is String) {
                            headersMap[k] = v
                        }
                    }
                }

                val queryParamsMap = mutableMapOf<String, String>()
                val rawQueryParams = doc.get("queryParams")
                if (rawQueryParams is Map<*, *>) {
                    for ((k, v) in rawQueryParams) {
                        if (k is String && v is String) {
                            queryParamsMap[k] = v
                        }
                    }
                }

                val rulesList = mutableListOf<ParserRule>()
                val rawRules = doc.get("parserRules") ?: doc.get("rules")
                if (rawRules is List<*>) {
                    for (item in rawRules) {
                        if (item is Map<*, *>) {
                            val stt = (item["stt"] as? Number)?.toInt() ?: 0
                            val enumNews = (item["enumNews"] as? Number)?.toInt() ?: 0
                            val strRegexName = item["strRegexName"] as? String ?: ""
                            val strRegex = item["strRegex"] as? String ?: ""
                            val isMatches = item["isMatches"] as? Boolean ?: false
                            val isMatchesPos = (item["isMatchesPos"] as? Number)?.toInt() ?: 0
                            val groupPos = (item["groupPos"] as? Number)?.toInt() ?: 3
                            val strPrefix = item["strPrefix"] as? String ?: ""
                            val strSufix = item["strSufix"] as? String ?: ""
                            val isReplace = item["isReplace"] as? Boolean ?: false
                            val textReplace = item["textReplace"] as? String ?: ""

                            rulesList.add(
                                ParserRule(
                                    stt = stt,
                                    enumNews = enumNews,
                                    strRegexName = strRegexName,
                                    strRegex = strRegex,
                                    isMatches = isMatches,
                                    isMatchesPos = isMatchesPos,
                                    groupPos = groupPos,
                                    strPrefix = strPrefix,
                                    strSufix = strSufix,
                                    isReplace = isReplace,
                                    textReplace = textReplace
                                )
                            )
                        }
                    }
                }

                NewsCategory(
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
        } catch (_: Exception) {
            emptyList()
        }
    }

    override suspend fun scrapeAndSaveNews(websiteId: String, category: NewsCategory, page: Int, append: Boolean): List<NewsItem> {
        return try {
            val websiteDoc = firestore.collection("news_websites").document(websiteId).get().await()
            val sigMap = websiteDoc.get("sigConfig") as? Map<*, *>
            val sigConfig = if (sigMap != null) {
                SigConfig(
                    pathname = sigMap["pathname"] as? String ?: "",
                    secretSalt = sigMap["secretSalt"] as? String ?: "",
                    apiKey = sigMap["apiKey"] as? String ?: "",
                    version = sigMap["version"] as? String ?: "",
                    whitelistParams = (sigMap["whitelistParams"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                )
            } else null

            val targetUrl = if (sigConfig != null && sigConfig.secretSalt.isNotEmpty()) {
                val websiteBaseUrl = websiteDoc.getString("baseUrl") ?: websiteDoc.getString("url") ?: category.url
                val customParams = mutableMapOf<String, String>().apply {
                    putAll(category.queryParams)
                    put("page", page.toString())
                }
                DynamicUrlGenerator.generateUrl(websiteBaseUrl, sigConfig, customParams)
            } else {
                if (page > 1 && category.pageTemplate.isNotEmpty()) {
                    if (category.pageTemplate.contains("{page}")) {
                        category.pageTemplate.replace("{page}", page.toString())
                    } else {
                        "${category.pageTemplate}?page=$page"
                    }
                } else if (page > 1) {
                    if (category.url.contains("?")) "${category.url}&page=$page" else "${category.url}?page=$page"
                } else {
                    category.url
                }
            }

            val finalHeaders = mutableMapOf<String, String>().apply {
                put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36")
                putAll(category.headers)
            }

            val htmlString = if (category.httpMethod.equals("POST", ignoreCase = true)) {
                val bodyStr = (category.postBodyTemplate ?: "{}").replace("{page}", page.toString())
                val requestBody = bodyStr.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
                val response = htmlScrapingService.fetchPost(finalHeaders, targetUrl, requestBody)
                val rawResponse = response.body()?.string() ?: ""
                if (!category.jsonResponseKey.isNullOrEmpty()) {
                    try {
                        val jsonObject = org.json.JSONObject(rawResponse)
                        jsonObject.optString(category.jsonResponseKey, rawResponse)
                    } catch (_: Exception) {
                        rawResponse
                    }
                } else {
                    rawResponse
                }
            } else {
                if (targetUrl.isEmpty()) return emptyList()
                val response = htmlScrapingService.fetchGet(finalHeaders, targetUrl)
                response.body()?.string() ?: ""
            }

            if (htmlString.isEmpty()) return emptyList()

            val items = HtmlParserEngine.parse(htmlString, category.parserRules)

            if (items.isNotEmpty()) {
                val entities = items.map {
                    ScrapedNewsEntity(
                        link = it.link,
                        websiteId = websiteId,
                        categoryId = category.id,
                        title = it.title,
                        image = it.image,
                        shortDes = it.shortDes,
                        date = it.date,
                        subCat = it.subCat,
                        isVideo = it.isVideo,
                        pageNumber = page
                    )
                }
                if (!append || page == 1) {
                    scrapedNewsDao.deleteForCategory(category.id)
                }
                scrapedNewsDao.upsertAll(entities)
            }

            items.map { it.copy(websiteId = websiteId) }
        } catch (_: Exception) {
            emptyList()
        }
    }

    override suspend fun getNewsDetail(websiteId: String, link: String): NewsDetail {
        val cached = newsDetailDao.getDetailByLink(link)
        if (cached != null) {
            return NewsDetail(
                link = cached.link,
                websiteId = cached.websiteId,
                title = cached.title,
                author = cached.author,
                publishDate = cached.publishDate,
                contentHtml = cached.contentHtml
            )
        }

        val rulesSnapshot = firestore.collection("news_websites")
            .document(websiteId)
            .collection("detail_rules")
            .get()
            .await()

        val detailRuleDocs = rulesSnapshot.documents.mapNotNull { doc ->
            val id = doc.id
            val name = doc.getString("name") ?: ""
            val isActive = doc.getBoolean("isActive") ?: true
            val targetField = (doc.get("targetField") as? Number)?.toInt() ?: 1

            val rulesList = mutableListOf<ParserRule>()
            val rawRules = doc.get("detailParserRules") ?: doc.get("rules")
            if (rawRules is List<*>) {
                for (item in rawRules) {
                    if (item is Map<*, *>) {
                        val stt = (item["stt"] as? Number)?.toInt() ?: 0
                        val enumNews = (item["enumNews"] as? Number)?.toInt() ?: 0
                        val strRegexName = item["strRegexName"] as? String ?: ""
                        val strRegex = item["strRegex"] as? String ?: ""
                        val isMatches = item["isMatches"] as? Boolean ?: false
                        val isMatchesPos = (item["isMatchesPos"] as? Number)?.toInt() ?: 0
                        val groupPos = (item["groupPos"] as? Number)?.toInt() ?: 3
                        val strPrefix = item["strPrefix"] as? String ?: ""
                        val strSufix = item["strSufix"] as? String ?: ""
                        val isReplace = item["isReplace"] as? Boolean ?: false
                        val textReplace = item["textReplace"] as? String ?: ""

                        rulesList.add(
                            ParserRule(
                                stt = stt,
                                enumNews = enumNews,
                                strRegexName = strRegexName,
                                strRegex = strRegex,
                                isMatches = isMatches,
                                isMatchesPos = isMatchesPos,
                                groupPos = groupPos,
                                strPrefix = strPrefix,
                                strSufix = strSufix,
                                isReplace = isReplace,
                                textReplace = textReplace
                            )
                        )
                    }
                }
            } else if (rawRules is Map<*, *>){
                val stt = (rawRules["stt"] as? Number)?.toInt() ?: 0
                val enumNews = (rawRules["enumNews"] as? Number)?.toInt() ?: 0
                val strRegexName = rawRules["strRegexName"] as? String ?: ""
                val strRegex = rawRules["strRegex"] as? String ?: ""
                val isMatches = rawRules["isMatches"] as? Boolean ?: false
                val isMatchesPos = (rawRules["isMatchesPos"] as? Number)?.toInt() ?: 0
                val groupPos = (rawRules["groupPos"] as? Number)?.toInt() ?: 3
                val strPrefix = rawRules["strPrefix"] as? String ?: ""
                val strSufix = rawRules["strSufix"] as? String ?: ""
                val isReplace = rawRules["isReplace"] as? Boolean ?: false
                val textReplace = rawRules["textReplace"] as? String ?: ""

                rulesList.add(
                    ParserRule(
                        stt = stt,
                        enumNews = enumNews,
                        strRegexName = strRegexName,
                        strRegex = strRegex,
                        isMatches = isMatches,
                        isMatchesPos = isMatchesPos,
                        groupPos = groupPos,
                        strPrefix = strPrefix,
                        strSufix = strSufix,
                        isReplace = isReplace,
                        textReplace = textReplace
                    )
                )
            }

            DetailRuleDocument(
                id = id,
                name = name,
                isActive = isActive,
                targetField = targetField,
                detailParserRules = rulesList
            )
        }

        val defaultHeaders = mapOf("User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36")
        val response = htmlScrapingService.fetchGet(defaultHeaders, link)
        val htmlString = response.body()?.string() ?: ""
        val detail = HtmlDetailParserEngine.parse(link, websiteId, htmlString, detailRuleDocs)

        newsDetailDao.insertDetail(
            NewsDetailEntity(
                link = detail.link,
                websiteId = detail.websiteId,
                title = detail.title,
                author = detail.author,
                publishDate = detail.publishDate,
                contentHtml = detail.contentHtml
            )
        )

        return detail
    }

    override fun getHomeNews(): Flow<List<NewsItem>> {
        return scrapedNewsDao.getHomeNews().map { entities ->
            entities.map {
                NewsItem(
                    websiteId = it.websiteId,
                    title = it.title,
                    image = it.image,
                    link = it.link,
                    shortDes = it.shortDes,
                    date = it.date,
                    subCat = it.subCat,
                    isVideo = it.isVideo
                )
            }.groupBy { it.websiteId }
             .flatMap { (_, items) ->
                 items.shuffled().take(10)
             }
        }
    }

    override fun getNewsForWebsite(websiteId: String): Flow<List<NewsItem>> {
        return scrapedNewsDao.getNewsForWebsite(websiteId).map { entities ->
            entities.map {
                NewsItem(
                    websiteId = it.websiteId,
                    title = it.title,
                    image = it.image,
                    link = it.link,
                    shortDes = it.shortDes,
                    date = it.date,
                    subCat = it.subCat,
                    isVideo = it.isVideo
                )
            }
        }
    }

    override fun getCachedNews(categoryId: String): Flow<List<NewsItem>> {
        return scrapedNewsDao.getNewsForCategory(categoryId).map { entities ->
            entities.map {
                NewsItem(
                    websiteId = it.websiteId,
                    title = it.title,
                    image = it.image,
                    link = it.link,
                    shortDes = it.shortDes,
                    date = it.date,
                    subCat = it.subCat,
                    isVideo = it.isVideo
                )
            }
        }
    }
}
