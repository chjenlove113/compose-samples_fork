package com.news.presentation.showHomeRSS

import androidx.activity.ComponentActivity
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
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.news.data.local.entities.RssItemEntity
import com.news.domain.models.AppUserSite
import com.news.presentation.main.MainViewModel
import com.news.presentation.main.RssJump
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun ShowHomeRSSFeedScreen(
    viewModel: ShowHomeRssViewModel = hiltViewModel(),
    mainViewModel: MainViewModel = hiltViewModel(viewModelStoreOwner = LocalContext.current as ComponentActivity)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val rssJump by mainViewModel.rssJump.collectAsStateWithLifecycle()
    val navigator = rememberListDetailPaneScaffoldNavigator<Any>()
    val scope = rememberCoroutineScope()

    ListDetailPaneScaffold(
        directive = navigator.scaffoldDirective,
        value = navigator.scaffoldValue,
        listPane = {
            AnimatedPane {
                RSSListPane(
                    sites = uiState.sites,
                    rssJump = rssJump,
                    onJumpHandled = { mainViewModel.clearRssJump() },
                    onRssItemClick = { item ->
                        scope.launch {
                            navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, item)
                        }
                    }
                )
            }
        },
        detailPane = {
            AnimatedPane {
                val selectedItem = navigator.currentDestination?.contentKey as? RssItemEntity
                if (selectedItem != null) {
                    ShowHomeRSSFeedDetailScreen(
                        item = selectedItem,
                        onBack = {
                            scope.launch {
                                if (navigator.canNavigateBack()) {
                                    navigator.navigateBack()
                                }
                            }
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RSSListPane(
    sites: List<AppUserSite>,
    rssJump: RssJump? = null,
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
            modifier = Modifier.weight(1f)
        ) { page ->
            val site = sites[page]
            RSSFeedChildScreen(
                siteId = site.Id,
                siteGroup = site.GROUP,
                siteKind = site.Kind,
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
    onItemClick: (RssItemEntity) -> Unit,
    viewModel: ShowHomeRssChildViewModel = hiltViewModel(
        key = "${siteId}_${siteGroup}_${siteKind}",
        creationCallback = { factory: ShowHomeRssChildViewModelFactory ->
            factory.create(siteId, siteGroup, siteKind)
        }
    )
) {
    val pagingItems = viewModel.rssItemsPagingData.collectAsLazyPagingItems()

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(
            count = pagingItems.itemCount,
            key = pagingItems.itemKey { it.link },
            contentType = pagingItems.itemContentType { "rss_item" }
        ) { index ->
            val item = pagingItems[index]
            if (item != null) {
                RssItemRow(item, onClick = { onItemClick(item) })
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            }
        }
    }
}

@Composable
fun RssItemRow(item: RssItemEntity, onClick: () -> Unit) {
    ListItem(
        modifier = Modifier.clickable { onClick() },
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
