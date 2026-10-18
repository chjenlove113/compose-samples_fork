package com.app.tintuccongnghe.newsScraping

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.core.layout.WindowWidthSizeClass
import coil3.compose.AsyncImage
import com.app.tintuccongnghe.domain.models.NewsCategory
import com.app.tintuccongnghe.domain.models.NewsItem
import com.app.tintuccongnghe.domain.models.NewsWebsite
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsScrapingScreen(
    onItemClick: (String, String) -> Unit = { _, _ -> },
    viewModel: NewsScrapingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val adaptiveInfo = currentWindowAdaptiveInfo()
    val isCompact = adaptiveInfo.windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.COMPACT

    val handleItemClick: (NewsItem) -> Unit = { item ->
        viewModel.selectOrToggleNewsItem(item)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Firestore Optimized Master-Detail") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is NewsScrapingUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is NewsScrapingUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(text = "Lỗi: ${state.message}", color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.refresh() }) { Text("Thử lại") }
                    }
                }
                is NewsScrapingUiState.Empty -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(text = "Không tìm thấy dữ liệu website.", style = MaterialTheme.typography.bodyLarge)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.refresh() }) { Text("Tải lại") }
                    }
                }
                is NewsScrapingUiState.Success -> {
                    val mainTabs = listOf("Home") + state.websites.map { it.name.ifEmpty { it.baseUrl } }
                    val mainPagerState = rememberPagerState(pageCount = { mainTabs.size })

                    LaunchedEffect(mainPagerState.currentPage) {
                        viewModel.selectMainTab(mainPagerState.currentPage)
                    }
                    LaunchedEffect(state.selectedMainTab) {
                        if (mainPagerState.currentPage != state.selectedMainTab) {
                            mainPagerState.animateScrollToPage(state.selectedMainTab)
                        }
                    }

                    val mainContent = @Composable {
                        Column(modifier = Modifier.fillMaxSize()) {
                            ScrollableTabRow(
                                selectedTabIndex = state.selectedMainTab,
                                edgePadding = 16.dp
                            ) {
                                mainTabs.forEachIndexed { index, title ->
                                    Tab(
                                        selected = state.selectedMainTab == index,
                                        onClick = { viewModel.selectMainTab(index) },
                                        text = { Text(text = title, fontWeight = FontWeight.Bold) }
                                    )
                                }
                            }

                            HorizontalPager(
                                state = mainPagerState,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                            ) { mainPageIndex ->
                                if (mainPageIndex == 0) {
                                    HomeNewsContent(
                                        websites = state.websites,
                                        newsItems = state.homeNews,
                                        selectedItem = state.selectedNewsItem,
                                        isLoading = state.isLoading,
                                        onRefresh = { viewModel.refresh() },
                                        onWebsiteHeaderClick = { websiteId ->
                                            val websiteIndex = state.websites.indexOfFirst { it.id == websiteId }
                                            if (websiteIndex != -1) {
                                                val targetTab = websiteIndex + 1
                                                viewModel.selectMainTab(targetTab)
                                                scope.launch {
                                                    mainPagerState.animateScrollToPage(targetTab)
                                                }
                                            }
                                        },
                                        onItemClick = handleItemClick
                                    )
                                } else {
                                    val website = state.websites.getOrNull(mainPageIndex - 1)
                                    if (website != null) {
                                        WebsiteContainerScreen(
                                            website = website,
                                            categories = state.websiteCategories[website.id] ?: emptyList(),
                                            websiteNews = state.websiteNews[website.id] ?: emptyList(),
                                            categoryNews = state.categoryNews,
                                            selectedSubIndex = state.selectedSubTabMap[website.id] ?: 0,
                                            selectedItem = state.selectedNewsItem,
                                            onSubTabSelected = { subIndex ->
                                                viewModel.selectSubTab(website.id, subIndex)
                                            },
                                            isLoading = state.isLoading,
                                            isLoadingMore = state.isLoadingMore,
                                            onRefresh = { viewModel.refresh() },
                                            onLoadMore = { viewModel.loadMore() },
                                            onItemClick = handleItemClick
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (isCompact) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            mainContent()
                        }
                        val selected = state.selectedNewsItem
                        if (selected != null) {
                            LaunchedEffect(selected) {
                                val encoded = java.net.URLEncoder.encode(selected.link, "UTF-8")
                                onItemClick(selected.websiteId, encoded)
                                viewModel.clearSelection()
                            }
                        }
                    } else {
                        if (state.selectedNewsItem == null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .animateContentSize()
                            ) {
                                mainContent()
                            }
                        } else {
                            if (state.isDetailExpanded) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .animateContentSize()
                                ) {
                                    NewsDetailScreen(
                                        websiteId = state.selectedNewsItem.websiteId,
                                        link = state.selectedNewsItem.link,
                                        isExpanded = true,
                                        onToggleExpand = { viewModel.toggleDetailExpansion() },
                                        onBack = {
                                            viewModel.selectOrToggleNewsItem(state.selectedNewsItem)
                                        }
                                    )
                                }
                            } else {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .animateContentSize()
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                    ) {
                                        mainContent()
                                    }
                                    VerticalDivider()
                                    Box(
                                        modifier = Modifier
                                            .weight(1.2f)
                                            .fillMaxHeight()
                                    ) {
                                        NewsDetailScreen(
                                            websiteId = state.selectedNewsItem.websiteId,
                                            link = state.selectedNewsItem.link,
                                            isExpanded = false,
                                            onToggleExpand = { viewModel.toggleDetailExpansion() },
                                            onBack = {
                                                viewModel.selectOrToggleNewsItem(state.selectedNewsItem)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeNewsContent(
    websites: List<NewsWebsite>,
    newsItems: List<NewsItem>,
    selectedItem: NewsItem?,
    isLoading: Boolean,
    onRefresh: () -> Unit,
    onWebsiteHeaderClick: (String) -> Unit,
    onItemClick: (NewsItem) -> Unit
) {
    val websiteMap = remember(websites) { websites.associateBy { it.id } }
    val groupedNews = remember(newsItems) { newsItems.groupBy { it.websiteId } }
    val adaptiveInfo = currentWindowAdaptiveInfo()
    val isCompact = adaptiveInfo.windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.COMPACT
    val isSplitView = !isCompact && selectedItem != null

    PullToRefreshBox(
        isRefreshing = isLoading,
        onRefresh = onRefresh,
        modifier = Modifier.fillMaxSize()
    ) {
        if (newsItems.isEmpty() && !isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Chưa có tin tức tổng hợp nào.")
            }
        } else {
            val listState = rememberLazyListState()
            val gridState = rememberLazyGridState()

            if (isCompact || isSplitView) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    for ((websiteId, items) in groupedNews) {
                        val website = websiteMap[websiteId]
                        val websiteName = website?.name?.ifEmpty { website.baseUrl } ?: "Website $websiteId"

                        stickyHeader(key = "header_$websiteId") {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onWebsiteHeaderClick(websiteId) },
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                tonalElevation = 4.dp
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = websiteName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                    Text(
                                        text = "Xem chi tiết →",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        items(items, key = { it.link }) { item ->
                            val isSelected = selectedItem?.link == item.link
                            NewsItemCard(item = item, isSelected = isSelected, onItemClick = onItemClick)
                        }
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    state = gridState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    for ((websiteId, items) in groupedNews) {
                        val website = websiteMap[websiteId]
                        val websiteName = website?.name?.ifEmpty { website.baseUrl } ?: "Website $websiteId"

                        item(span = { GridItemSpan(2) }, key = "header_$websiteId") {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onWebsiteHeaderClick(websiteId) },
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                tonalElevation = 4.dp
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = websiteName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                    Text(
                                        text = "Xem chi tiết →",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        items(items, key = { it.link }) { item ->
                            val isSelected = selectedItem?.link == item.link
                            NewsItemCard(item = item, isSelected = isSelected, onItemClick = onItemClick)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WebsiteContainerScreen(
    website: NewsWebsite,
    categories: List<NewsCategory>,
    websiteNews: List<NewsItem>,
    categoryNews: Map<String, List<NewsItem>>,
    selectedSubIndex: Int,
    selectedItem: NewsItem?,
    onSubTabSelected: (Int) -> Unit,
    isLoading: Boolean,
    isLoadingMore: Boolean,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    onItemClick: (NewsItem) -> Unit
) {
    when {
        categories.isEmpty() -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Website đang cập nhật chuyên mục...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
        categories.size == 1 -> {
            val singleCategory = categories.first()
            val currentNews = categoryNews[singleCategory.id] ?: emptyList()
            AdaptiveNewsList(
                newsItems = currentNews,
                selectedItem = selectedItem,
                isLoading = isLoading,
                isLoadingMore = isLoadingMore,
                onRefresh = onRefresh,
                onLoadMore = onLoadMore,
                onItemClick = onItemClick
            )
        }
        else -> {
            val subTabs = listOf("All") + categories.map { it.name }
            val subPagerState = rememberPagerState(pageCount = { subTabs.size })

            LaunchedEffect(subPagerState.currentPage) {
                onSubTabSelected(subPagerState.currentPage)
            }
            LaunchedEffect(selectedSubIndex) {
                if (subPagerState.currentPage != selectedSubIndex) {
                    subPagerState.animateScrollToPage(selectedSubIndex)
                }
            }

            Column(modifier = Modifier.fillMaxSize()) {
                ScrollableTabRow(
                    selectedTabIndex = selectedSubIndex,
                    edgePadding = 16.dp,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    subTabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedSubIndex == index,
                            onClick = { onSubTabSelected(index) },
                            text = { Text(text = title, fontWeight = if (selectedSubIndex == index) FontWeight.Bold else FontWeight.Normal) }
                        )
                    }
                }

                HorizontalPager(
                    state = subPagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) { subPageIndex ->
                    if (subPageIndex == 0) {
                        WebsiteAllNewsList(
                            newsItems = websiteNews,
                            selectedItem = selectedItem,
                            isLoading = isLoading,
                            isLoadingMore = isLoadingMore,
                            onRefresh = onRefresh,
                            onLoadMore = onLoadMore,
                            onItemClick = onItemClick
                        )
                    } else {
                        val cat = categories.getOrNull(subPageIndex - 1)
                        val currentNews = if (cat != null) categoryNews[cat.id] ?: emptyList() else emptyList()
                        CategoryNewsList(
                            newsItems = currentNews,
                            selectedItem = selectedItem,
                            isLoading = isLoading,
                            isLoadingMore = isLoadingMore,
                            onRefresh = onRefresh,
                            onLoadMore = onLoadMore,
                            onItemClick = onItemClick
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebsiteAllNewsList(
    newsItems: List<NewsItem>,
    selectedItem: NewsItem?,
    isLoading: Boolean,
    isLoadingMore: Boolean,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    onItemClick: (NewsItem) -> Unit
) {
    AdaptiveNewsList(
        newsItems = newsItems,
        selectedItem = selectedItem,
        isLoading = isLoading,
        isLoadingMore = isLoadingMore,
        onRefresh = onRefresh,
        onLoadMore = onLoadMore,
        onItemClick = onItemClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryNewsList(
    newsItems: List<NewsItem>,
    selectedItem: NewsItem?,
    isLoading: Boolean = false,
    isLoadingMore: Boolean = false,
    onRefresh: () -> Unit = {},
    onLoadMore: () -> Unit = {},
    onItemClick: (NewsItem) -> Unit
) {
    AdaptiveNewsList(
        newsItems = newsItems,
        selectedItem = selectedItem,
        isLoading = isLoading,
        isLoadingMore = isLoadingMore,
        onRefresh = onRefresh,
        onLoadMore = onLoadMore,
        onItemClick = onItemClick
    )
}

@Composable
fun AdaptiveNewsList(
    newsItems: List<NewsItem>,
    selectedItem: NewsItem?,
    isLoading: Boolean = false,
    isLoadingMore: Boolean = false,
    onRefresh: () -> Unit = {},
    onLoadMore: () -> Unit = {},
    onItemClick: (NewsItem) -> Unit
) {
    val adaptiveInfo = currentWindowAdaptiveInfo()
    val isCompact = adaptiveInfo.windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.COMPACT
    val isSplitView = !isCompact && selectedItem != null

    PullToRefreshBox(
        isRefreshing = isLoading,
        onRefresh = onRefresh,
        modifier = Modifier.fillMaxSize()
    ) {
        if (newsItems.isEmpty() && !isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Không có bài viết trong danh sách này.")
            }
        } else {
            val listState = rememberLazyListState()
            val gridState = rememberLazyGridState()

            val shouldLoadMore = remember {
                derivedStateOf {
                    if (isCompact || isSplitView) {
                        val layoutInfo = listState.layoutInfo
                        val totalItemsNumber = layoutInfo.totalItemsCount
                        val lastVisibleItemIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                        totalItemsNumber > 0 && lastVisibleItemIndex >= totalItemsNumber - 3
                    } else {
                        val layoutInfo = gridState.layoutInfo
                        val totalItemsNumber = layoutInfo.totalItemsCount
                        val lastVisibleItemIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                        totalItemsNumber > 0 && lastVisibleItemIndex >= totalItemsNumber - 3
                    }
                }
            }

            LaunchedEffect(shouldLoadMore.value) {
                if (shouldLoadMore.value && !isLoadingMore) {
                    onLoadMore()
                }
            }

            if (isCompact || isSplitView) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(newsItems, key = { it.link }) { item ->
                        val isSelected = selectedItem?.link == item.link
                        NewsItemCard(item = item, isSelected = isSelected, onItemClick = onItemClick)
                    }

                    if (isLoadingMore) {
                        item(key = "loading_more") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(32.dp))
                            }
                        }
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    state = gridState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(newsItems, key = { it.link }) { item ->
                        val isSelected = selectedItem?.link == item.link
                        NewsItemCard(item = item, isSelected = isSelected, onItemClick = onItemClick)
                    }

                    if (isLoadingMore) {
                        item(span = { GridItemSpan(2) }, key = "loading_more_grid") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(32.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NewsItemCard(item: NewsItem, isSelected: Boolean = false, onItemClick: (NewsItem) -> Unit) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        label = "cardBackground"
    )
    val elevation by animateDpAsState(
        targetValue = if (isSelected) 6.dp else 2.dp,
        label = "cardElevation"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                if (item.link.isNotEmpty() && item.websiteId.isNotEmpty()) {
                    onItemClick(item)
                }
            },
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            if (item.image.isNotEmpty()) {
                AsyncImage(
                    model = item.image,
                    contentDescription = item.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(MaterialTheme.shapes.medium),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (item.subCat.isNotEmpty()) {
                Text(
                    text = item.subCat.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            if (item.shortDes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.shortDes,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (item.date.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = item.date,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}
