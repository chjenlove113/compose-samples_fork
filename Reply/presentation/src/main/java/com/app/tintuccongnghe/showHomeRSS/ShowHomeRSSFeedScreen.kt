@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.app.tintuccongnghe.showHomeRSS

import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.RssFeed
import androidx.compose.material.icons.outlined.Subscriptions
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldDefaults
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.calculateThreePaneScaffoldValue
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import androidx.window.core.layout.WindowWidthSizeClass
import coil3.compose.AsyncImage
import com.app.tintuccongnghe.data.local.entities.RssItemEntity
import com.app.tintuccongnghe.domain.models.AppUserSite
import com.app.tintuccongnghe.main.MainViewModel
import com.app.tintuccongnghe.main.RssJump
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ShowHomeRSSFeedScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToUserSites: () -> Unit,
    viewModel: ShowHomeRssViewModel = hiltViewModel(),
    mainViewModel: MainViewModel = hiltViewModel(viewModelStoreOwner = LocalActivity.current as ViewModelStoreOwner)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val rssJump by mainViewModel.rssJump.collectAsStateWithLifecycle()
    val rssItemJump by mainViewModel.rssItemJump.collectAsStateWithLifecycle()
    val navigator = rememberListDetailPaneScaffoldNavigator<Any>()
    val scope = rememberCoroutineScope()
    var isFullScreen by rememberSaveable { mutableStateOf(false) }

    // Handle RSS item jump from widget
    LaunchedEffect(rssItemJump) {
        val item = rssItemJump
        if (item != null) {
            isFullScreen = false
            navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, item)
            mainViewModel.clearRssItemJump()
        }
    }

    // Handle system back button
    BackHandler(navigator.canNavigateBack() || isFullScreen) {
        if (isFullScreen) {
            isFullScreen = false
        } else {
            scope.launch {
                navigator.navigateBack()
            }
        }
    }

    // Sync Navigator state back to ViewModel
    val currentDetail = navigator.currentDestination?.contentKey as? RssItemEntity
    LaunchedEffect(currentDetail) {
        viewModel.setSelectedRssItem(currentDetail)
    }

    // Determine if we should show List or Detail as the single pane when maxHorizontalPartitions = 1
    val currentScaffoldDirective = if (isFullScreen) {
        navigator.scaffoldDirective.copy(maxHorizontalPartitions = 1)
    } else {
        navigator.scaffoldDirective
    }

    val scaffoldValue = calculateThreePaneScaffoldValue(
        maxHorizontalPartitions = currentScaffoldDirective.maxHorizontalPartitions,
        adaptStrategies = ListDetailPaneScaffoldDefaults.adaptStrategies(),
        currentDestination = navigator.currentDestination
    )

    if (uiState.sites.isEmpty() && !uiState.isLoading) {
        EmptyStateWithButton(
            message = "No RSS sites found. Add some in User Sites.",
            buttonText = "Add RSS Site",
            onClick = onNavigateToUserSites
        )
        return
    }

    if (uiState.isLoading && uiState.sites.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            LoadingIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return
    }

    PullToRefreshBox(
        isRefreshing = uiState.isSyncing,
        onRefresh = { viewModel.syncRss() },
        modifier = Modifier.fillMaxSize()
    ) {
        val totalItems = uiState.sites.sumOf { it.itemCount }
        if (totalItems == 0 && !uiState.isLoading && !uiState.isSyncing) {
            EmptyStateWithButton(
                message = "Your RSS feeds are empty. Sync now to get latest news.",
                buttonText = "Sync RSS News",
                onClick = { viewModel.syncRss() }
            )
        } else {
            ListDetailPaneScaffold(
                directive = currentScaffoldDirective,
                value = scaffoldValue,
                listPane = {
                    AnimatedPane {
                        RSSListPane(
                            sites = uiState.sites,
                            rssJump = rssJump,
                            selectedItem = uiState.selectedItem,
                            onJumpHandled = { mainViewModel.clearRssJump() },
                            onNavigateToUserSites = onNavigateToUserSites,
                            onSyncRss = { viewModel.syncRss() },
                            onRssItemClick = { item ->
                                scope.launch {
                                    isFullScreen = false
                                    navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, item)
                                }
                            }
                        )
                    }
                },
                detailPane = {
                    AnimatedPane {
                        if (currentDetail != null) {
                            ShowHomeRSSFeedDetailScreen(
                                item = currentDetail,
                                onBack = {
                                    scope.launch {
                                        isFullScreen = false
                                        navigator.navigateBack()
                                    }
                                },
                                isFullScreen = isFullScreen,
                                onToggleFullScreen = {
                                    isFullScreen = !isFullScreen
                                }
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.surfaceContainerLowest),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier.padding(32.dp)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                        modifier = Modifier.size(72.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Outlined.Article,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(36.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "Select an RSS article to read details",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun EmptyStateWithButton(
    message: String,
    buttonText: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(80.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.RssFeed,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = message,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onClick,
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(buttonText)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun RSSListPane(
    sites: List<AppUserSite>,
    rssJump: RssJump? = null,
    selectedItem: RssItemEntity? = null,
    onJumpHandled: () -> Unit = {},
    onNavigateToUserSites: () -> Unit = {},
    onSyncRss: () -> Unit = {},
    onRssItemClick: (RssItemEntity) -> Unit
) {
    if (sites.isEmpty()) {
        EmptyStateWithButton(
            message = "No RSS sites found. Add some in User Sites.",
            buttonText = "Add RSS Site",
            onClick = onNavigateToUserSites
        )
        return
    }

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { sites.size }
    )
    val scope = rememberCoroutineScope()
    val adaptiveInfo = currentWindowAdaptiveInfo()
    val tabEdgePadding = if (
        adaptiveInfo.windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.COMPACT
    ) 12.dp else 16.dp

    val selectedTabIndex = if (sites.isEmpty()) 0 else pagerState.currentPage.coerceIn(0, sites.size - 1)

    // Ensure pager state stays within bounds if sites count changes
    LaunchedEffect(sites.size) {
        if (sites.isNotEmpty() && pagerState.currentPage >= sites.size) {
            pagerState.scrollToPage(sites.size - 1)
        }
    }

    // Sync pager state only if a specific site is requested via parameters
    LaunchedEffect(rssJump, sites) {
        if (rssJump != null && sites.isNotEmpty()) {
            val index = sites.indexOfFirst { it.Id == rssJump.siteId && it.GROUP == rssJump.siteGroup }
            if (index != -1) {
                if (pagerState.currentPage != index) {
                    pagerState.scrollToPage(index)
                }
                onJumpHandled()
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Modern Header Bar
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.RssFeed,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "RSS Feeds",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Text(
                                    text = "${sites.size}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = onSyncRss,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = "Refresh",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = onNavigateToUserSites,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Subscriptions,
                            contentDescription = "Manage Feeds",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Modern Tab Row
        SecondaryScrollableTabRow(
            selectedTabIndex = selectedTabIndex,
            edgePadding = tabEdgePadding,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            divider = {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            }
        ) {
            sites.forEachIndexed { index, site ->
                val isSelected = selectedTabIndex == index
                Tab(
                    selected = isSelected,
                    onClick = {
                        scope.launch { pagerState.animateScrollToPage(index) }
                    },
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = site.Name,
                                style = if (isSelected)
                                    MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                else
                                    MaterialTheme.typography.titleSmall,
                                maxLines = 1
                            )
                            if (site.itemCount > 0) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh
                                ) {
                                    Text(
                                        text = if (site.itemCount > 99) "99+" else "${site.itemCount}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    },
                    selectedContentColor = MaterialTheme.colorScheme.primary,
                    unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .background(MaterialTheme.colorScheme.surfaceContainerLowest),
            key = { index ->
                val site = sites.getOrNull(index)
                if (site != null) "${site.Id}_${site.GROUP}_${site.Kind}" else index
            }
        ) { page ->
            val site = sites.getOrNull(page) ?: return@HorizontalPager
            RSSFeedChildScreen(
                siteId = site.Id,
                siteGroup = site.GROUP,
                siteKind = site.Kind,
                selectedItem = selectedItem,
                onItemClick = onRssItemClick
            )
        }
    }
}

@Composable
fun RSSFeedChildScreen(
    siteId: Int,
    siteGroup: String,
    siteKind: String,
    selectedItem: RssItemEntity? = null,
    onItemClick: (RssItemEntity) -> Unit,
    viewModel: ShowHomeRssChildViewModel = hiltViewModel(
        key = "${siteId}_${siteGroup}_${siteKind}",
        creationCallback = { factory: ShowHomeRssChildViewModel.Factory ->
            factory.create(siteId, siteGroup, siteKind)
        }
    )
) {
    val pagingItems = viewModel.rssItemsPagingData.collectAsLazyPagingItems()
    val listState = rememberLazyListState()
    val adaptiveInfo = currentWindowAdaptiveInfo()
    val contentPadding = if (
        adaptiveInfo.windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.COMPACT
    ) 12.dp else 16.dp

    // Scroll to selected item when it changes from a jump or navigation
    LaunchedEffect(selectedItem) {
        if (selectedItem != null) {
            for (i in 0 until pagingItems.itemCount) {
                val item = pagingItems.peek(i)
                if (item != null && item.link == selectedItem.link) {
                    listState.animateScrollToItem(i)
                    break
                }
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
        contentPadding = PaddingValues(horizontal = contentPadding, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            count = pagingItems.itemCount,
            key = pagingItems.itemKey { "${it.link}_${it.siteId}_${it.siteGroup}_${it.siteKind}" },
            contentType = pagingItems.itemContentType { "rss_item" }
        ) { index ->
            val item = pagingItems[index]
            if (item != null) {
                val isSelected = selectedItem != null &&
                               selectedItem.link == item.link &&
                               selectedItem.siteId == item.siteId &&
                               selectedItem.siteGroup == item.siteGroup &&
                               selectedItem.siteKind == item.siteKind

                RssItemRow(
                    item = item,
                    isSelected = isSelected,
                    onClick = { onItemClick(item) },
                    onFavoriteClick = { viewModel.toggleFavorite(item) }
                )
            }
        }
    }
}

@Composable
fun RssItemRow(
    item: RssItemEntity,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit = {}
) {
    val adaptiveInfo = currentWindowAdaptiveInfo()
    val isCompact = adaptiveInfo.windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.COMPACT
    val isExpanded = adaptiveInfo.windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.EXPANDED

    val titleStyle = if (isExpanded) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleMedium
    val bodyStyle = if (isExpanded) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodySmall
    val padding = if (isCompact) 12.dp else 16.dp

    val cardContainerColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }

    val cardContentColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    ElevatedCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = cardContainerColor,
            contentColor = cardContentColor
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = if (isSelected) 4.dp else 1.dp,
            pressedElevation = 6.dp
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxWidth()
        ) {
            // Header Row: Site Chip + Relative Time + Favorite Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected)
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        else
                            MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = item.siteName ?: "RSS Feed",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isSelected)
                                MaterialTheme.colorScheme.onPrimaryContainer
                            else
                                MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    val dateStr = formatRelativeTime(item.pubDate)
                    if (dateStr.isNotEmpty()) {
                        Text(
                            text = "•  $dateStr",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected)
                                MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                IconButton(
                    onClick = onFavoriteClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (item.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (item.isFavorite) Color(0xFFE53935) else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Body Row: Title + Description + Image
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        style = titleStyle,
                        color = cardContentColor,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    val description = item.description
                    if (!description.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = description,
                            style = bodyStyle,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                            color = if (isSelected)
                                MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (!item.imageUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = item.imageUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(if (isExpanded) 96.dp else 84.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}

private fun formatRelativeTime(timestamp: Long?): String {
    if (timestamp == null || timestamp <= 0) return ""
    val diff = System.currentTimeMillis() - timestamp
    return when {
        diff < 0 -> "Just now"
        diff < 60_000L -> "Just now"
        diff < 3_600_000L -> "${diff / 60_000L}m ago"
        diff < 86_400_000L -> "${diff / 3_600_000L}h ago"
        diff < 172_800_000L -> "Yesterday"
        else -> SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(timestamp))
    }
}
