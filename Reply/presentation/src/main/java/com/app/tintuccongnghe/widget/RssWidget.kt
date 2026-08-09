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
import androidx.glance.appwidget.state.getAppWidgetState
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
import androidx.glance.text.TextStyle
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.app.tintuccongnghe.data.local.AppDatabase
import com.app.tintuccongnghe.data.local.entities.AppUserSiteEntity
import com.app.tintuccongnghe.data.local.entities.RssItemEntity
import com.app.tintuccongnghe.presentation.R
import com.app.tintuccongnghe.work.WorkScheduler
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

class RssWidget : GlanceAppWidget() {

    companion object {
        val SelectedSiteIdKey = intPreferencesKey("selected_site_id")
        val SelectedSiteGroupKey = stringPreferencesKey("selected_site_group")
        val SelectedSiteKindKey = stringPreferencesKey("selected_site_kind")
        
        val SiteIdParam = ActionParameters.Key<Int>("siteId")
        val SiteGroupParam = ActionParameters.Key<String>("siteGroup")
        val SiteKindParam = ActionParameters.Key<String>("siteKind")
    }

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
        
        // In Glance 1.1.x, getAppWidgetState<Preferences> is used for standard state
        val prefs = getAppWidgetState<Preferences>(context, id)
        val selectedSiteId = prefs[SelectedSiteIdKey]
        val selectedSiteGroup = prefs[SelectedSiteGroupKey]
        val selectedSiteKind = prefs[SelectedSiteKindKey]

        val siteDao = entryPoint.appDatabase().appUserSiteDao()
        val rssItemDao = entryPoint.appDatabase().rssItemDao()
        
        val allSites = siteDao.getAllSites().filter { it.GROUP == "1" }
        
        val items = if (selectedSiteId != null && selectedSiteGroup != null && selectedSiteKind != null) {
            rssItemDao.getLatestRssItemsForSite(selectedSiteId, selectedSiteGroup, selectedSiteKind)
        } else {
            rssItemDao.getLatestRssItems()
        }

        provideContent {
            RssWidgetContent(
                items = items,
                sites = allSites,
                selectedSiteId = selectedSiteId
            )
        }
    }

    @Composable
    private fun RssWidgetContent(
        items: List<RssItemEntity>,
        sites: List<AppUserSiteEntity>,
        selectedSiteId: Int?
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
                Row(
                    modifier = GlanceModifier.fillMaxWidth().padding(bottom = 12.dp),
                    verticalAlignment = Alignment.Vertical.CenterVertically
                ) {
                    // "All" tab
                    TabItem(
                        text = "Tất cả",
                        isSelected = selectedSiteId == null,
                        onClick = actionRunCallback<SelectSiteActionCallback>(
                            parameters = actionParametersOf()
                        )
                    )
                    
                    sites.take(3).forEach { site ->
                        Spacer(modifier = GlanceModifier.width(4.dp))
                        TabItem(
                            text = site.Name,
                            isSelected = selectedSiteId == site.Id,
                            onClick = actionRunCallback<SelectSiteActionCallback>(
                                parameters = actionParametersOf(
                                    SiteIdParam to site.Id,
                                    SiteGroupParam to site.GROUP,
                                    SiteKindParam to site.Kind
                                )
                            )
                        )
                    }
                }

                if (items.isEmpty()) {
                    Box(
                        modifier = GlanceModifier.fillMaxSize().defaultWeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Không có tin tức nào",
                            style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant)
                        )
                    }
                } else {
                    LazyColumn(modifier = GlanceModifier.fillMaxWidth().defaultWeight()) {
                        items(items) { item ->
                            RssItemRow(item)
                            Spacer(modifier = GlanceModifier.height(8.dp))
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
                maxLines = 1
            )
        }
    }

    @Composable
    private fun RssItemRow(item: RssItemEntity) {
        val intent = Intent()
        intent.setAction(Intent.ACTION_VIEW)
        intent.setData(Uri.parse("reply://rss_detail"))
        intent.setClassName("com.app.tintuccongnghe", "com.app.tintuccongnghe.main.MainActivity")
        intent.putExtra("rss_item", item)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        Column(
            modifier = GlanceModifier
                .fillMaxWidth()
                .background(GlanceTheme.colors.secondaryContainer)
                .cornerRadius(12.dp)
                .padding(8.dp)
                .clickable(actionStartActivity(intent))
        ) {
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
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
                    Spacer(modifier = GlanceModifier.height(4.dp))
                    Text(
                        text = item.siteName ?: "RSS",
                        style = TextStyle(
                            fontSize = 11.sp,
                            color = GlanceTheme.colors.onSurfaceVariant
                        )
                    )
                }
                
                if (item.imageUrl != null) {
                    Spacer(modifier = GlanceModifier.width(8.dp))
                    Box(
                        modifier = GlanceModifier
                            .size(50.dp)
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
}

class SelectSiteActionCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val siteId = parameters[RssWidget.SiteIdParam]
        val siteGroup = parameters[RssWidget.SiteGroupParam]
        val siteKind = parameters[RssWidget.SiteKindParam]

        updateAppWidgetState(context, glanceId) { prefs ->
            if (siteId != null && siteGroup != null && siteKind != null) {
                prefs[RssWidget.SelectedSiteIdKey] = siteId
                prefs[RssWidget.SelectedSiteGroupKey] = siteGroup
                prefs[RssWidget.SelectedSiteKindKey] = siteKind
            } else {
                prefs.remove(RssWidget.SelectedSiteIdKey)
                prefs.remove(RssWidget.SelectedSiteGroupKey)
                prefs.remove(RssWidget.SelectedSiteKindKey)
            }
        }
        RssWidget().update(context, glanceId)
    }
}

class RefreshActionCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        WorkScheduler.refreshRssNow(context)
    }
}
