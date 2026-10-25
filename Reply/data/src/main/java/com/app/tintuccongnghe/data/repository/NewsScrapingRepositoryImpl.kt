package com.app.tintuccongnghe.data.repository

import android.content.Context
import android.content.pm.ApplicationInfo
import com.app.tintuccongnghe.data.api.HtmlScrapingService
import com.app.tintuccongnghe.data.local.AppDatabase
import com.app.tintuccongnghe.data.local.entities.NewsDetailEntity
import com.app.tintuccongnghe.data.local.entities.ScrapedNewsEntity
import com.app.tintuccongnghe.data.local.entities.toDomain
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
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

class NewsScrapingRepositoryImpl @Inject constructor(
    private val htmlScrapingService: HtmlScrapingService,
    private val firestore: FirebaseFirestore,
    appDatabase: AppDatabase,
    @param:ApplicationContext private val context: Context
) : INewsScrapingRepository {

    private val scrapedNewsDao = appDatabase.scrapedNewsDao()
    private val newsDetailDao = appDatabase.newsDetailDao()
    private val newsWebsiteDao = appDatabase.newsWebsiteDao()
    private val newsCategoryDao = appDatabase.newsCategoryDao()
    private val detailRuleDao = appDatabase.detailRuleDao()

    companion object {
        private val ANDROID_USER_AGENT: String by lazy {
            val androidVersion = android.os.Build.VERSION.RELEASE ?: "14"
            val deviceModel = android.os.Build.MODEL ?: "Android"
            "Mozilla/5.0 (Linux; Android $androidVersion; $deviceModel) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.6261.64 Mobile Safari/537.36"
        }
    }

    override suspend fun getWebsites(): List<NewsWebsite> {
        val isDebug = (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        val prefs = context.getSharedPreferences("news_scraping_prefs", Context.MODE_PRIVATE)
        val lastFetchTime = prefs.getLong("last_firebase_websites_fetch", 0L)
        val currentTime = System.currentTimeMillis()
        val oneDayMillis = 24 * 60 * 60 * 1000L

        val localWebsites = try { newsWebsiteDao.getAllWebsitesList() } catch (_: Exception) { emptyList() }

        // Fetch from Firestore in Debug mode OR at most once per day in Release mode OR when local DB is empty
        if (isDebug || currentTime - lastFetchTime >= oneDayMillis || localWebsites.isEmpty()) {
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

                remoteWebsites.forEach { remote ->
                    val existing = localWebsites.find { it.id == remote.id }
                    val activeStatus = existing?.isActive ?: remote.isActive
                    newsWebsiteDao.insert(remote.copy(isActive = activeStatus).toEntity())
                }

                prefs.edit().putLong("last_firebase_websites_fetch", currentTime).apply()
            } catch (_: Exception) {}
        }

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
        val isDebug = (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        val prefs = context.getSharedPreferences("news_scraping_prefs", Context.MODE_PRIVATE)
        val lastFetchTime = prefs.getLong("last_firebase_categories_${websiteId}_fetch", 0L)
        val currentTime = System.currentTimeMillis()
        val oneDayMillis = 24 * 60 * 60 * 1000L

        val localCategories = try { newsCategoryDao.getCategoriesForWebsiteList(websiteId) } catch (_: Exception) { emptyList() }

        // Fetch from Firestore in Debug mode OR at most once per day in Release mode OR when local DB is empty
        if (isDebug || currentTime - lastFetchTime >= oneDayMillis || localCategories.isEmpty()) {
            try {
                val snapshot = firestore.collection("news_websites")
                    .document(websiteId)
                    .collection("categories")
                    .get()
                    .await()

                val remoteCategories = snapshot.documents.mapNotNull { doc ->
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

                if (remoteCategories.isNotEmpty()) {
                    val entities = remoteCategories.map { it.toEntity(websiteId) }
                    newsCategoryDao.insertAll(entities)
                }

                prefs.edit().putLong("last_firebase_categories_${websiteId}_fetch", currentTime).apply()
            } catch (_: Exception) {}
        }

        return try {
            newsCategoryDao.getCategoriesForWebsiteList(websiteId).map { it.toDomain() }
        } catch (_: Exception) {
            emptyList()
        }
    }

    override suspend fun scrapeAndSaveNews(websiteId: String, category: NewsCategory, page: Int, append: Boolean): List<NewsItem> = withContext(Dispatchers.IO) {
        try {
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
                put("User-Agent", ANDROID_USER_AGENT)
                put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                put("Accept-Language", "vi-VN,vi;q=0.9,en-US;q=0.8,en;q=0.7")
                putAll(category.headers)
            }

            val htmlString = if (category.httpMethod.equals("POST", ignoreCase = true)) {
                val bodyStr = (category.postBodyTemplate ?: "{}").replace("{page}", page.toString())
                val requestBody = bodyStr.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
                val response = htmlScrapingService.fetchPost(finalHeaders, targetUrl, requestBody)
                
                if (!response.isSuccessful) {
                    return@withContext emptyList()
                }

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
                if (targetUrl.isEmpty()) return@withContext emptyList()
                val response = htmlScrapingService.fetchGet(finalHeaders, targetUrl)
                if (!response.isSuccessful) {
                    return@withContext emptyList()
                }
                response.body()?.string() ?: ""
            }

            if (htmlString.isEmpty()) return@withContext emptyList()

            val items = HtmlParserEngine.parse(htmlString, category.parserRules, websiteId, category.id)

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

    override suspend fun getNewsDetail(websiteId: String, link: String): NewsDetail = withContext(Dispatchers.IO) {
        val cached = newsDetailDao.getDetailByLink(link)
        if (cached != null) {
            return@withContext NewsDetail(
                link = cached.link,
                websiteId = cached.websiteId,
                title = cached.title,
                author = cached.author,
                publishDate = cached.publishDate,
                description = cached.description,
                contentHtml = cached.contentHtml
            )
        }

        val isDebug = (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        val prefs = context.getSharedPreferences("news_scraping_prefs", Context.MODE_PRIVATE)
        val lastFetchTime = prefs.getLong("last_firebase_detail_rules_${websiteId}_fetch", 0L)
        val currentTime = System.currentTimeMillis()
        val threeHoursMillis = 3 * 60 * 60 * 1000L

        val localRuleEntities = try { detailRuleDao.getRulesForWebsite(websiteId) } catch (_: Exception) { emptyList() }

        // Fetch detail_rules from Firestore in Debug mode OR at most once per 3 hours in Release mode OR when local DB is empty
        if (isDebug || currentTime - lastFetchTime >= threeHoursMillis || localRuleEntities.isEmpty()) {
            try {
                val rulesSnapshot = firestore.collection("news_websites")
                    .document(websiteId)
                    .collection("detail_rules")
                    .get()
                    .await()

                val remoteDetailRuleDocs = rulesSnapshot.documents.mapNotNull { doc ->
                    val id = doc.id
                    val name = doc.getString("name") ?: ""
                    val isActive = doc.getBoolean("isActive") ?: true
                    val targetField = (doc.get("targetField") as? Number)?.toInt() ?: 1
                    val getContentBy = doc.getString("getContentBy") ?: doc.getString("strGetContentBy") ?: ""

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
                        getContentBy = getContentBy,
                        detailParserRules = rulesList
                    )
                }

                if (remoteDetailRuleDocs.isNotEmpty()) {
                    val entities = remoteDetailRuleDocs.map { doc -> doc.toEntity(websiteId) }
                    detailRuleDao.insertAll(entities)
                }

                prefs.edit().putLong("last_firebase_detail_rules_${websiteId}_fetch", currentTime).apply()
            } catch (_: Exception) {}
        }

        val ruleEntities = try { detailRuleDao.getRulesForWebsite(websiteId) } catch (_: Exception) { emptyList() }
        val detailRuleDocs = ruleEntities.map { it.toDomain() }

        val defaultHeaders = mapOf(
            "User-Agent" to ANDROID_USER_AGENT,
            "Accept" to "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
            "Accept-Language" to "vi-VN,vi;q=0.9,en-US;q=0.8,en;q=0.7"
        )
        val response = htmlScrapingService.fetchGet(defaultHeaders, link)
        val htmlString = if (response.isSuccessful) response.body()?.string() ?: "" else ""

        val detail = HtmlDetailParserEngine.parse(link, websiteId, htmlString, detailRuleDocs)

        newsDetailDao.insertDetail(
            NewsDetailEntity(
                link = detail.link,
                websiteId = detail.websiteId,
                title = detail.title,
                author = detail.author,
                publishDate = detail.publishDate,
                description = detail.description,
                contentHtml = detail.contentHtml
            )
        )

        detail
    }

    override fun getHomeNews(): Flow<List<NewsItem>> {
        return scrapedNewsDao.getHomeNews().map { entities ->
            val activeWebsiteIds = try {
                newsWebsiteDao.getAllWebsitesList().filter { it.isActive }.map { it.id }.toSet()
            } catch (_: Exception) {
                emptySet()
            }

            entities.filter { activeWebsiteIds.isEmpty() || it.websiteId in activeWebsiteIds }
                .map {
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
             .flatMap { unbroken ->
                 unbroken.value.shuffled().take(10)
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
