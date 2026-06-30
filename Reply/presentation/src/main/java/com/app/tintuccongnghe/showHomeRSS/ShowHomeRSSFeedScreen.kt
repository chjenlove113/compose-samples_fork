package com.app.tintuccongnghe.showHomeRSS

import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldDefaults
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.calculateThreePaneScaffoldValue
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.app.tintuccongnghe.data.local.entities.RssItemEntity
import com.app.tintuccongnghe.domain.models.AppUserSite
import com.app.tintuccongnghe.main.MainViewModel
import com.app.tintuccongnghe.main.RssJump
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun ShowHomeRSSFeedScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToUserSites: () -> Unit,
    viewModel: ShowHomeRssViewModel = hiltViewModel(),
    mainViewModel: MainViewModel = hiltViewModel(viewModelStoreOwner = LocalContext.current as ComponentActivity)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val rssJump by mainViewModel.rssJump.collectAsStateWithLifecycle()
    val navigator = rememberListDetailPaneScaffoldNavigator<Any>()
    val scope = rememberCoroutineScope()
    var isFullScreen by rememberSaveable { mutableStateOf(false) }

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

    if (!uiState.isLoggedIn) {
        EmptyStateWithButton(
            message = "Please login to view RSS feeds",
            buttonText = "Go to Login",
            onClick = onNavigateToLogin
        )
        return
    }

    if (uiState.sites.isEmpty() && !uiState.isLoading) {
        EmptyStateWithButton(
            message = "No RSS sites found. Add some in User Sites.",
            buttonText = "Add RSS Site",
            onClick = onNavigateToUserSites
        )
        return
    }

    val totalItems = uiState.sites.sumOf { it.itemCount }
    if (totalItems == 0 && !uiState.isLoading && !uiState.isSyncing) {
        EmptyStateWithButton(
            message = "Your RSS feeds are empty. Sync now to get latest news.",
            buttonText = "Sync RSS News",
            onClick = { viewModel.syncRss() }
        )
        return
    }

    if (uiState.isLoading || uiState.isSyncing) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
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
                    onRssItemClick = { item ->
                        scope.launch {
                            // When clicking an item, we usually want to see both panes on large screens
                            // so we ensure isFullScreen is false unless the user explicitly expands it later.
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
                                // 1. Exit full screen mode
                                isFullScreen = false
                                // 2. Navigate back to List role in the navigator
                                navigator.navigateBack()
                            }
                        },
                        isFullScreen = isFullScreen,
                        onToggleFullScreen = {
                            isFullScreen = !isFullScreen
                        }
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Select an RSS item to view details")
                    }
                }
            }
        }
    )
}

@Composable
fun EmptyStateWithButton(
    message: String,
    buttonText: String,
    onClick: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = message,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onClick) {
                Text(buttonText)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RSSListPane(
    sites: List<AppUserSite>,
    rssJump: RssJump? = null,
    selectedItem: RssItemEntity? = null,
    onJumpHandled: () -> Unit = {},
    onRssItemClick: (RssItemEntity) -> Unit
) {
    if (sites.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No RSS sites found. Add some in User Sites.")
        }
        return
    }

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { sites.size }
    )
    val scope = rememberCoroutineScope()

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
        ScrollableTabRow(selectedTabIndex = pagerState.currentPage) {
            sites.forEachIndexed { index, site ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = {
                        scope.launch { pagerState.animateScrollToPage(index) }
                    },
                    text = { Text(text = site.Name, maxLines = 1) }
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f),
            key = { index -> 
                val site = sites.getOrNull(index)
                if (site != null) "${site.Id}_${site.GROUP}_${site.Kind}" else index
            }
        ) { page ->
            val site = sites[page]
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

    LazyColumn(modifier = Modifier.fillMaxSize()) {
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
                
                RssItemRow(item, isSelected = isSelected, onClick = { onItemClick(item) })
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            }
        }
    }
}

@Composable
fun RssItemRow(item: RssItemEntity, isSelected: Boolean = false, onClick: () -> Unit) {
    ListItem(
        modifier = Modifier.clickable { onClick() },
        colors = ListItemDefaults.colors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        ),
        headlineContent = {
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        },
        supportingContent = {
            item.description?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    )
}
