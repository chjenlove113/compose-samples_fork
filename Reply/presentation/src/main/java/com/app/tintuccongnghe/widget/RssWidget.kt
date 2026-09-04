package com.app.tintuccongnghe.widget

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.Action
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.components.CircleIconButton
import androidx.glance.appwidget.components.FilledButton
import androidx.glance.appwidget.components.Scaffold
import androidx.glance.appwidget.components.TitleBar
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.background
import androidx.glance.currentState
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
import com.app.tintuccongnghe.data.local.AppDatabase
import com.app.tintuccongnghe.data.local.entities.AppUserSiteEntity
import com.app.tintuccongnghe.data.local.entities.RssItemEntity
import com.app.tintuccongnghe.presentation.R
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object RssWidgetPrefs {
    val SelectedSiteIdKey = intPreferencesKey("selected_site_id")
    val SelectedSiteGroupKey = stringPreferencesKey("selected_site_group")
    val SelectedSiteKindKey = stringPreferencesKey("selected_site_kind")

    val SiteIdParam = ActionParameters.Key<Int>("siteId")
    val SiteGroupParam = ActionParameters.Key<String>("siteGroup")
    val SiteKindParam = ActionParameters.Key<String>("siteKind")
}

class RssWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Responsive(
        setOf(CompactWidgetSize, MediumWidgetSize, ExpandedWidgetSize)
    )

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface WidgetEntryPoint {
        fun appDatabase(): AppDatabase
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val appContext = context.applicationContext
        val database = EntryPointAccessors.fromApplication(
            appContext,
            WidgetEntryPoint::class.java,
        ).appDatabase()
        val allSites = database.appUserSiteDao().getAllSites().filter { it.GROUP == "1" }
        val isLoggedIn = database.authDao().getAuthInfoDirect() != null

        provideContent {
            val preferences = currentState<Preferences>()
            val selectedSiteId = preferences[RssWidgetPrefs.SelectedSiteIdKey]
            val selectedSiteGroup = preferences[RssWidgetPrefs.SelectedSiteGroupKey]
            val selectedSiteKind = preferences[RssWidgetPrefs.SelectedSiteKindKey]
            val items = produceState<List<RssItemEntity>>(
                initialValue = emptyList(),
                selectedSiteId,
                selectedSiteGroup,
                selectedSiteKind,
            ) {
                value = if (
                    selectedSiteId != null &&
                    selectedSiteGroup != null &&
                    selectedSiteKind != null
                ) {
                    database.rssItemDao().getLatestRssItemsForSite(
                        selectedSiteId,
                        selectedSiteGroup,
                        selectedSiteKind,
                    )
                } else {
                    database.rssItemDao().getLatestRssItems()
                }
            }

            RssWidgetContent(
                items = items.value,
                sites = allSites,
                selectedSiteId = selectedSiteId,
                isLoggedIn = isLoggedIn,
            )
        }
    }

    @Composable
    private fun RssWidgetContent(
        items: List<RssItemEntity>,
        sites: List<AppUserSiteEntity>,
        selectedSiteId: Int?,
        isLoggedIn: Boolean,
    ) {
        val context = LocalContext.current
        val size = LocalSize.current
        val layout = WidgetLayout.fromHeight(size.height)
        val widgetTitle = if (size.width < 220.dp) "" else context.getString(R.string.rss_widget_name)

        GlanceTheme {
            Scaffold(
                titleBar = if (layout == WidgetLayout.Compact) {
                    null
                } else {
                    {
                        TitleBar(
                            startIcon = ImageProvider(R.drawable.ic_launcher_monochrome),
                            title = widgetTitle,
                            iconColor = GlanceTheme.colors.primary,
                            actions = {
                                CircleIconButton(
                                    imageProvider = ImageProvider(R.drawable.ic_refresh),
                                    contentDescription = context.getString(R.string.rss_widget_refresh),
                                    onClick = actionRunCallback<RefreshActionCallback>(),
                                    backgroundColor = null,
                                    contentColor = GlanceTheme.colors.primary,
                                )
                            },
                        )
                    }
                },
            ) {
                val contentModifier = if (layout == WidgetLayout.Compact) {
                    GlanceModifier.fillMaxSize().padding(vertical = 8.dp)
                } else {
                    GlanceModifier.fillMaxSize().padding(bottom = 12.dp)
                }
                Column(
                    modifier = contentModifier,
                ) {
                    if (layout != WidgetLayout.Compact && sites.isNotEmpty()) {
                        SiteFilters(
                            sites = sites,
                            selectedSiteId = selectedSiteId,
                            maxItems = if (layout == WidgetLayout.Expanded) 4 else 3,
                        )
                        Spacer(GlanceModifier.height(8.dp))
                    }

                    when {
                        items.isEmpty() -> EmptyContent(
                            isLoggedIn = isLoggedIn,
                            compact = layout == WidgetLayout.Compact,
                        )
                        layout == WidgetLayout.Compact -> CompactNewsList(items)
                        else -> NewsList(
                            items = items,
                            expanded = layout == WidgetLayout.Expanded,
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun CompactNewsList(items: List<RssItemEntity>) {
        val visibleItems = items.take(2)
        Column(modifier = GlanceModifier.fillMaxSize()) {
            visibleItems.forEachIndexed { index, item ->
                Box(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .padding(bottom = if (index < visibleItems.lastIndex) 6.dp else 0.dp),
                ) {
                    RssItemCard(
                        item = item,
                        emphasized = index == 0,
                        showDetails = false,
                        showImage = false,
                        compact = true,
                    )
                }
            }
        }
    }

    @Composable
    private fun SiteFilters(
        sites: List<AppUserSiteEntity>,
        selectedSiteId: Int?,
        maxItems: Int,
    ) {
        val context = LocalContext.current
        val options = filterOptions(sites, selectedSiteId, maxItems)

        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.Vertical.CenterVertically,
        ) {
            options.forEachIndexed { index, site ->
                if (index > 0) Spacer(GlanceModifier.width(6.dp))
                Box(GlanceModifier.defaultWeight()) {
                    FilterChip(
                        text = site?.Name ?: context.getString(R.string.rss_widget_all),
                        isSelected = if (site == null) selectedSiteId == null else selectedSiteId == site.Id,
                        onClick = if (site == null) {
                            actionRunCallback<SelectSiteActionCallback>()
                        } else {
                            actionRunCallback<SelectSiteActionCallback>(
                                parameters = actionParametersOf(
                                    RssWidgetPrefs.SiteIdParam to site.Id,
                                    RssWidgetPrefs.SiteGroupParam to site.GROUP,
                                    RssWidgetPrefs.SiteKindParam to site.Kind,
                                )
                            )
                        },
                    )
                }
            }
        }
    }

    @Composable
    private fun FilterChip(
        text: String,
        isSelected: Boolean,
        onClick: Action,
    ) {
        Box(
            modifier = GlanceModifier
                .fillMaxWidth()
                .background(
                    if (isSelected) {
                        GlanceTheme.colors.primaryContainer
                    } else {
                        GlanceTheme.colors.surfaceVariant
                    }
                )
                .cornerRadius(18.dp)
                .padding(horizontal = 10.dp, vertical = 7.dp)
                .clickable(onClick),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = text,
                style = TextStyle(
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) {
                        GlanceTheme.colors.onPrimaryContainer
                    } else {
                        GlanceTheme.colors.onSurfaceVariant
                    },
                    textAlign = TextAlign.Center,
                ),
                maxLines = 1,
            )
        }
    }

    @Composable
    private fun NewsList(items: List<RssItemEntity>, expanded: Boolean) {
        LazyColumn(modifier = GlanceModifier.fillMaxSize()) {
            item {
                Box(
                    modifier = GlanceModifier.fillMaxWidth().padding(bottom = 8.dp),
                ) {
                    RssItemCard(
                        item = items.first(),
                        emphasized = true,
                        showDetails = true,
                        showImage = expanded,
                    )
                }
            }
            items(items.drop(1).take(if (expanded) 11 else 5)) { item ->
                Box(
                    modifier = GlanceModifier.fillMaxWidth().padding(bottom = 8.dp),
                ) {
                    RssItemCard(
                        item = item,
                        emphasized = false,
                        showDetails = true,
                        showImage = false,
                    )
                }
            }
        }
    }

    @Composable
    private fun RssItemCard(
        item: RssItemEntity,
        emphasized: Boolean,
        showDetails: Boolean,
        showImage: Boolean,
        compact: Boolean = false,
    ) {
        val context = LocalContext.current
        val articleIntent = appIntent(context, "reply://rss_detail").apply {
            putExtra("rss_item", item)
        }
        val containerColor = if (emphasized) {
            GlanceTheme.colors.secondaryContainer
        } else {
            GlanceTheme.colors.surfaceVariant
        }
        val contentColor = if (emphasized) {
            GlanceTheme.colors.onSecondaryContainer
        } else {
            GlanceTheme.colors.onSurface
        }

        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .background(containerColor)
                .cornerRadius(if (compact) 16.dp else if (emphasized) 24.dp else 16.dp)
                .padding(if (compact) 8.dp else if (emphasized) 12.dp else 10.dp)
                .clickable(actionStartActivity(articleIntent)),
            verticalAlignment = Alignment.Vertical.CenterVertically,
        ) {
            Column(GlanceModifier.defaultWeight()) {
                Text(
                    text = item.title,
                    style = TextStyle(
                        color = contentColor,
                        fontSize = if (compact) 13.sp else if (emphasized) 15.sp else 14.sp,
                        fontWeight = if (emphasized) FontWeight.Bold else FontWeight.Medium,
                    ),
                    maxLines = if (compact) 1 else if (emphasized) 2 else 1,
                )
                if (showDetails) {
                    Spacer(GlanceModifier.height(4.dp))
                    Text(
                        text = item.metadata(),
                        style = TextStyle(
                            color = GlanceTheme.colors.onSurfaceVariant,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                        ),
                        maxLines = 1,
                    )
                }
            }

            if (showImage && item.imageUrl != null) {
                Spacer(GlanceModifier.width(10.dp))
                Box(
                    modifier = GlanceModifier
                        .size(56.dp)
                        .background(GlanceTheme.colors.tertiaryContainer)
                        .cornerRadius(16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Image(
                        provider = ImageProvider(R.drawable.ic_launcher_foreground),
                        contentDescription = context.getString(R.string.rss_widget_article_image),
                        modifier = GlanceModifier.size(28.dp),
                        colorFilter = ColorFilter.tint(GlanceTheme.colors.onTertiaryContainer),
                    )
                }
            }
        }
    }

    @Composable
    private fun EmptyContent(isLoggedIn: Boolean, compact: Boolean) {
        val context = LocalContext.current
        val message = context.getString(
            if (isLoggedIn) R.string.rss_widget_empty else R.string.rss_widget_login_required
        )

        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.surfaceVariant)
                .cornerRadius(24.dp)
                .padding(12.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = message,
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurface,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                    ),
                    maxLines = if (compact) 2 else 3,
                )
                if (!compact) {
                    Spacer(GlanceModifier.height(8.dp))
                    if (isLoggedIn) {
                        Text(
                            text = context.getString(R.string.rss_widget_empty_hint),
                            style = TextStyle(
                                color = GlanceTheme.colors.onSurfaceVariant,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                            ),
                            maxLines = 2,
                        )
                    } else {
                        FilledButton(
                            text = context.getString(R.string.rss_widget_sign_in),
                            onClick = actionStartActivity(appIntent(context, "reply://login")),
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }

    private companion object {
        val CompactWidgetSize = DpSize(180.dp, 110.dp)
        val MediumWidgetSize = DpSize(270.dp, 180.dp)
        val ExpandedWidgetSize = DpSize(270.dp, 280.dp)
    }
}

private enum class WidgetLayout {
    Compact,
    Medium,
    Expanded;

    companion object {
        fun fromHeight(height: androidx.compose.ui.unit.Dp): WidgetLayout = when {
            height < 180.dp -> Compact
            height < 280.dp -> Medium
            else -> Expanded
        }
    }
}

private fun filterOptions(
    sites: List<AppUserSiteEntity>,
    selectedSiteId: Int?,
    maxItems: Int,
): List<AppUserSiteEntity?> {
    val selectedIndex = sites.indexOfFirst { it.Id == selectedSiteId }
    val orderedSites = if (selectedIndex < 0) {
        sites
    } else {
        List(sites.size) { offset ->
            sites[(selectedIndex + offset) % sites.size]
        }
    }
    return listOf(null) + orderedSites.take(maxItems - 1)
}

private fun appIntent(context: Context, uri: String): Intent =
    Intent(Intent.ACTION_VIEW, Uri.parse(uri)).apply {
        setClassName(context.packageName, "com.app.tintuccongnghe.main.MainActivity")
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

private fun RssItemEntity.metadata(): String {
    val site = siteName ?: "RSS"
    val time = pubDate?.let {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(it))
    }
    return if (time == null) site else "$site · $time"
}

class SelectSiteActionCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        val siteId = parameters[RssWidgetPrefs.SiteIdParam]
        val siteGroup = parameters[RssWidgetPrefs.SiteGroupParam]
        val siteKind = parameters[RssWidgetPrefs.SiteKindParam]

        updateAppWidgetState(context, glanceId) { preferences ->
            if (siteId != null && siteGroup != null && siteKind != null) {
                preferences[RssWidgetPrefs.SelectedSiteIdKey] = siteId
                preferences[RssWidgetPrefs.SelectedSiteGroupKey] = siteGroup
                preferences[RssWidgetPrefs.SelectedSiteKindKey] = siteKind
            } else {
                preferences.remove(RssWidgetPrefs.SelectedSiteIdKey)
                preferences.remove(RssWidgetPrefs.SelectedSiteGroupKey)
                preferences.remove(RssWidgetPrefs.SelectedSiteKindKey)
            }
        }
        RssWidget().update(context, glanceId)
    }
}

class RefreshActionCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        RssWidget().update(context, glanceId)
    }
}
