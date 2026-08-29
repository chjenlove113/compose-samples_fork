package com.app.tintuccongnghe.widget

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.app.tintuccongnghe.data.local.AppDatabase
import com.app.tintuccongnghe.data.local.entities.AppUserSiteEntity
import com.app.tintuccongnghe.data.local.entities.RssItemEntity
import com.app.tintuccongnghe.presentation.R
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

object RssWidgetPrefs {
    val SelectedSiteIdKey = intPreferencesKey("selected_site_id")
    val SelectedSiteGroupKey = stringPreferencesKey("selected_site_group")
    val SelectedSiteKindKey = stringPreferencesKey("selected_site_kind")
    
    val SiteIdParam = ActionParameters.Key<Int>("siteId")
    val SiteGroupParam = ActionParameters.Key<String>("siteGroup")
    val SiteKindParam = ActionParameters.Key<String>("siteKind")
}

class RssWidget : GlanceAppWidget() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface WidgetEntryPoint {
        fun appDatabase(): AppDatabase
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val appContext = context.applicationContext
        val entryPoint = EntryPointAccessors.fromApplication(
            appContext,
            WidgetEntryPoint::class.java
        )
        
        val db = entryPoint.appDatabase()
        val authDao = db.authDao()
        val siteDao = db.appUserSiteDao()
        val rssItemDao = db.rssItemDao()

        // 1. Fetch sites and auth status once (fast queries)
        val allSites = siteDao.getAllSites().filter { it.GROUP == "1" }
        val isLoggedIn = authDao.getAuthInfoDirect() != null

        provideContent {
            // 2. Reactively read current state inside provideContent
            // This ensures the UI updates immediately when updateAppWidgetState is called
            val prefs = androidx.glance.currentState<Preferences>()
            val selectedSiteId = prefs[RssWidgetPrefs.SelectedSiteIdKey]
            val selectedSiteGroup = prefs[RssWidgetPrefs.SelectedSiteGroupKey]
            val selectedSiteKind = prefs[RssWidgetPrefs.SelectedSiteKindKey]

            // 3. Use produceState to fetch items without blocking composition
            val itemsState = androidx.compose.runtime.produceState<List<RssItemEntity>>(initialValue = emptyList(), selectedSiteId, selectedSiteGroup, selectedSiteKind) {
                value = if (selectedSiteId != null && selectedSiteGroup != null && selectedSiteKind != null) {
                    rssItemDao.getLatestRssItemsForSite(selectedSiteId, selectedSiteGroup, selectedSiteKind)
                } else {
                    rssItemDao.getLatestRssItems()
                }
            }

            RssWidgetContent(
                items = itemsState.value,
                sites = allSites,
                selectedSiteId = selectedSiteId,
                isLoggedIn = isLoggedIn
            )
        }
    }

    @Composable
    private fun RssWidgetContent(
        items: List<RssItemEntity>,
        sites: List<AppUserSiteEntity>,
        selectedSiteId: Int?,
        isLoggedIn: Boolean
    ) {
        GlanceTheme {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .appWidgetBackground()
                    .background(GlanceTheme.colors.surface)
                    .cornerRadius(16.dp)
                    .padding(12.dp),
                horizontalAlignment = Alignment.Horizontal.Start,
                verticalAlignment = Alignment.Vertical.Top
            ) {
                // Header
                Row(
                    modifier = GlanceModifier.fillMaxWidth().padding(bottom = 8.dp),
                    verticalAlignment = Alignment.Vertical.CenterVertically
                ) {
                    Image(
                        provider = ImageProvider(R.mipmap.ic_launcher),
                        contentDescription = "App Icon",
                        modifier = GlanceModifier.size(24.dp)
                    )
                    Spacer(modifier = GlanceModifier.width(8.dp))
                    Text(
                        text = "Tin tức",
                        style = TextStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = GlanceTheme.colors.onSurface
                        ),
                        modifier = GlanceModifier.defaultWeight()
                    )
                    Image(
                        provider = ImageProvider(R.drawable.ic_refresh),
                        contentDescription = "Refresh",
                        modifier = GlanceModifier
                            .size(24.dp)
                            .clickable(actionRunCallback<RefreshActionCallback>()),
                        colorFilter = ColorFilter.tint(GlanceTheme.colors.primary)
                    )
                }

                // Tabs
                Column(modifier = GlanceModifier.fillMaxWidth().padding(bottom = 12.dp)) {
                    val allTabItems = listOf<AppUserSiteEntity?>(null) + sites
                    val chunks = allTabItems.chunked(3)
                    
                    chunks.forEach { chunk ->
                        Row(
                            modifier = GlanceModifier.fillMaxWidth().padding(bottom = 4.dp),
                            verticalAlignment = Alignment.Vertical.CenterVertically
                        ) {
                            chunk.forEachIndexed { index, site ->
                                if (index > 0) {
                                    Spacer(modifier = GlanceModifier.width(4.dp))
                                }
                                Box(modifier = GlanceModifier.defaultWeight()) {
                                    if (site == null) {
                                        TabItem(
                                            text = "Tất cả",
                                            isSelected = selectedSiteId == null,
                                            onClick = actionRunCallback<SelectSiteActionCallback>(
                                                parameters = actionParametersOf()
                                            )
                                        )
                                    } else {
                                        TabItem(
                                            text = site.Name,
                                            isSelected = selectedSiteId == site.Id,
                                            onClick = actionRunCallback<SelectSiteActionCallback>(
                                                parameters = actionParametersOf(
                                                    RssWidgetPrefs.SiteIdParam to site.Id,
                                                    RssWidgetPrefs.SiteGroupParam to site.GROUP,
                                                    RssWidgetPrefs.SiteKindParam to site.Kind
                                                )
                                            )
                                        )
                                    }
                                }
                            }
                            // Fill remaining space if chunk is not full
                            if (chunk.size < 3) {
                                Spacer(modifier = GlanceModifier.defaultWeight())
                                if (chunk.size < 2) {
                                    Spacer(modifier = GlanceModifier.defaultWeight())
                                }
                            }
                        }
                    }
                }

                if (items.isEmpty()) {
                    Box(
                        modifier = GlanceModifier.fillMaxSize().defaultWeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (isLoggedIn) "Không có tin tức nào" else "Vui lòng đăng nhập để xem tin tức",
                                style = TextStyle(
                                    color = GlanceTheme.colors.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            )
                            
                            if (!isLoggedIn) {
                                Spacer(modifier = GlanceModifier.height(12.dp))
                                val loginIntent = Intent(Intent.ACTION_VIEW, Uri.parse("reply://login"))
                                loginIntent.setClassName("com.app.tintuccongnghe", "com.app.tintuccongnghe.main.MainActivity")
                                loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

                                Box(
                                    modifier = GlanceModifier
                                        .background(GlanceTheme.colors.primary)
                                        .cornerRadius(16.dp)
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                        .clickable(actionStartActivity(loginIntent))
                                ) {
                                    Text(
                                        text = "Đăng nhập",
                                        style = TextStyle(
                                            color = GlanceTheme.colors.onPrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }
                    }
                } else {
                    LazyColumn(modifier = GlanceModifier.fillMaxWidth().defaultWeight()) {
                        items(items) { item ->
                            Column {
                                RssItemRow(item)
                                Spacer(modifier = GlanceModifier.height(8.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun TabItem(
        text: String,
        isSelected: Boolean,
        onClick: androidx.glance.action.Action
    ) {
        Box(
            modifier = GlanceModifier
                .fillMaxWidth()
                .background(if (isSelected) GlanceTheme.colors.primary else GlanceTheme.colors.surfaceVariant)
                .cornerRadius(12.dp)
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .clickable(onClick)
        ) {
            Text(
                text = text,
                style = TextStyle(
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) GlanceTheme.colors.onPrimary else GlanceTheme.colors.onSurfaceVariant
                ),
                maxLines = 1,
                modifier = GlanceModifier.fillMaxWidth()
            )
        }
    }

    @Composable
    private fun RssItemRow(item: RssItemEntity) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("reply://rss_detail"))
        intent.setClassName("com.app.tintuccongnghe", "com.app.tintuccongnghe.main.MainActivity")
        intent.putExtra("rss_item", item)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .background(GlanceTheme.colors.secondaryContainer)
                .cornerRadius(12.dp)
                .padding(8.dp)
                .clickable(actionStartActivity(intent)),
            verticalAlignment = Alignment.Vertical.CenterVertically
        ) {
            Column(modifier = GlanceModifier.defaultWeight()) {
                Text(
                    text = item.title,
                    style = TextStyle(
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = GlanceTheme.colors.onSecondaryContainer
                    ),
                    maxLines = 2
                )
                Text(
                    text = item.siteName ?: "RSS",
                    style = TextStyle(
                        fontSize = 11.sp,
                        color = GlanceTheme.colors.onSurfaceVariant
                    )
                )
            }
            
            if (item.imageUrl != null) {
                Box(
                    modifier = GlanceModifier
                        .padding(start = 8.dp)
                        .size(48.dp)
                        .background(GlanceTheme.colors.surfaceVariant)
                        .cornerRadius(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        provider = ImageProvider(R.drawable.ic_launcher_foreground),
                        contentDescription = null,
                        modifier = GlanceModifier.size(24.dp),
                        colorFilter = ColorFilter.tint(GlanceTheme.colors.onSurfaceVariant)
                    )
                }
            }
        }
    }
}

class SelectSiteActionCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val siteId = parameters[RssWidgetPrefs.SiteIdParam]
        val siteGroup = parameters[RssWidgetPrefs.SiteGroupParam]
        val siteKind = parameters[RssWidgetPrefs.SiteKindParam]

        updateAppWidgetState(context, glanceId) { prefs ->
            if (siteId != null && siteGroup != null && siteKind != null) {
                prefs[RssWidgetPrefs.SelectedSiteIdKey] = siteId
                prefs[RssWidgetPrefs.SelectedSiteGroupKey] = siteGroup
                prefs[RssWidgetPrefs.SelectedSiteKindKey] = siteKind
            } else {
                prefs.remove(RssWidgetPrefs.SelectedSiteIdKey)
                prefs.remove(RssWidgetPrefs.SelectedSiteGroupKey)
                prefs.remove(RssWidgetPrefs.SelectedSiteKindKey)
            }
        }
        // In Glance 1.1.0+, updateAppWidgetState should automatically trigger an update.
        // We'll call update() once just to be sure, but we won't block the action on it if possible.
        RssWidget().update(context, glanceId)
    }
}

class RefreshActionCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        RssWidget().update(context, glanceId)
    }
}
