@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.app.tintuccongnghe.showHomeRSS

import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
    mainViewModel: MainViewModel = hiltViewModel(viewModelStoreOwner = LocalContext.current as ViewModelStoreOwner)
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

    // Sync Navigator state back to ViewModel
    val currentDetail = navigator.currentDestination?.contentKey as? RssItemEntity
    LaunchedEffect(currentDetail) {
        viewModel.setSelectedRssItem(currentDetail)
    }

    val canGoBack = isFullScreen || navigator.canNavigateBack() || (currentDetail != null)

    val handleBack: () -> Unit = {
        if (isFullScreen) {
            isFullScreen = false
        } else if (navigator.canNavigateBack()) {
            scope.launch {
                navigator.navigateBack()
            }
        } else if (currentDetail != null) {
            scope.launch {
                navigator.navigateTo(ListDetailPaneScaffoldRole.List)
            }
        }
    }

    // Handle system back button
    BackHandler(canGoBack) {
        handleBack()
    }

    // Determine if we should show List or Detail as the single pane when maxHorizontalPartitions = 1
    val currentScaffoldDirective = if (isFullScreen || currentDetail == null) {
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
                    onSyncRss = { site -> viewModel.syncRssForSite(site) },
                    isRefreshing = uiState.isSyncing,
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
                        onBack = handleBack,
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

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun RSSListPane(
    sites: List<AppUserSite>,
    rssJump: RssJump? = null,
    selectedItem: RssItemEntity? = null,
    onJumpHandled: () -> Unit = {},
    onNavigateToUserSites: () -> Unit = {},
    onSyncRss: (AppUserSite?) -> Unit = {},
    onRssItemClick: (RssItemEntity) -> Unit,
    isRefreshing: Boolean = false
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

    val currentSite = sites.getOrNull(pagerState.currentPage)

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

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { onSyncRss(currentSite) },
        modifier = Modifier.fillMaxSize()
    ) {
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
                                    text = "RSS Reader",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Surface(
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "${sites.size} feeds",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Swipe or tap tabs to explore",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { onSyncRss(currentSite) }) {
                            Icon(imageVector = Icons.Default.Sync, contentDescription = "Sync RSS")
                        }
                        IconButton(onClick = onNavigateToUserSites) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Manage User Sites")
                        }
                    }
                }
            }

            // Modern Scrollable Tab Row
            ScrollableTabRow(
                selectedTabIndex = pagerState.currentPage,
                edgePadding = tabEdgePadding,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                divider = {
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                }
            ) {
                sites.forEachIndexed { index, site ->
                    val selected = pagerState.currentPage == index
                    Tab(
                        selected = selected,
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = site.Name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (site.itemCount > 0) {
                                    Badge(
                                        containerColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                        contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    ) {
                                        Text(text = "${site.itemCount}", fontSize = 10.sp)
                                    }
                                }
                            }
                        }
                    )
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
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
            val index = rssItemsPagingDataMatch(pagingItems, selectedItem)
            if (index != -1) {
                listState.animateScrollToItem(index)
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

fun rssItemsPagingDataMatch(pagingItems: androidx.paging.compose.LazyPagingItems<RssItemEntity>, selectedItem: RssItemEntity): Int {
    for (i in 0 until pagingItems.itemCount) {
        val item = pagingItems.peek(i)
        if (item != null && item.link == selectedItem.link) {
            return i
        }
    }
    return -1
}

@Composable
fun RssItemRow(
    item: RssItemEntity,
    isSelected: Boolean = false,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!item.imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = item.imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                val desc = item.description
                if (!desc.isNullOrBlank()) {
                    Text(
                        text = desc,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            IconButton(onClick = onFavoriteClick) {
                Icon(
                    imageVector = if (item.isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (item.isFavorite) Color.Red else LocalContentColor.current
                )
            }
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
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier.size(80.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.Subscriptions,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onClick) {
                Text(buttonText)
            }
        }
    }
}
