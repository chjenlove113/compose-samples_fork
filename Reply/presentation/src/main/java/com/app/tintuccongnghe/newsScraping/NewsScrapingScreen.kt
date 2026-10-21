package com.app.tintuccongnghe.newsScraping

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
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

    // ViewModel-backed LazyGridStates to survive folding, unfolding, and configuration changes
    val gridStates = remember { mutableStateMapOf<String, LazyGridState>() }

    val getGridState: (String) -> LazyGridState = { key ->
        gridStates.getOrPut(key) {
            val saved = viewModel.getScrollState(key)
            LazyGridState(
                firstVisibleItemIndex = saved?.first ?: 0,
                firstVisibleItemScrollOffset = saved?.second ?: 0
            )
        }
    }

    val saveCurrentScrollState: () -> Unit = {
        val currentSuccess = uiState as? NewsScrapingUiState.Success
        if (currentSuccess != null) {
            val mainTab = currentSuccess.selectedMainTab
            if (mainTab > 0) {
                val website = currentSuccess.websites.getOrNull(mainTab - 1)
                val subIndex = website?.let { currentSuccess.selectedSubTabMap[it.id] } ?: 0
                val key = "website_${website?.id ?: ""}_sub_$subIndex"
                gridStates[key]?.let { grid ->
                    viewModel.saveScrollState(key, grid.firstVisibleItemIndex, grid.firstVisibleItemScrollOffset)
                }
            }
        }
    }

    val handleItemClick: (NewsItem) -> Unit = { item ->
        saveCurrentScrollState()
        viewModel.selectOrToggleNewsItem(item)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Key-Based Scroll Restoration") },
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
                        if (state.selectedMainTab != mainPagerState.currentPage) {
                            viewModel.selectMainTab(mainPagerState.currentPage)
                        }
                    }

                    val mainContent = @Composable {
                        val safeMainTab = if (mainTabs.isEmpty()) 0 else state.selectedMainTab.coerceIn(0, mainTabs.size - 1)
                        Column(modifier = Modifier.fillMaxSize()) {
                            ScrollableTabRow(
                                selectedTabIndex = safeMainTab,
                                edgePadding = 16.dp
                            ) {
                                mainTabs.forEachIndexed { index, title ->
                                    Tab(
                                        selected = safeMainTab == index,
                                        onClick = {
                                            viewModel.selectMainTab(index)
                                            scope.launch {
                                                mainPagerState.animateScrollToPage(index)
                                            }
                                        },
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
                                            gridStates = gridStates,
                                            getGridState = getGridState,
                                            onSubTabSelected = { subIndex ->
                                                viewModel.selectSubTab(website.id, subIndex)
                                            },
                                            isLoading = state.isLoading,
                                            isLoadingMore = state.isLoadingMore,
                                            hasMorePages = state.hasMorePages,
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
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                        ) {
                            if (!state.isDetailExpanded || state.selectedNewsItem == null) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                ) {
                                    mainContent()
                                }
                            }
                            if (state.selectedNewsItem != null) {
                                if (!state.isDetailExpanded) {
                                    VerticalDivider()
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(if (state.isDetailExpanded) 1f else 1.2f)
                                        .fillMaxHeight()
                                ) {
                                    NewsDetailScreen(
                                        websiteId = state.selectedNewsItem.websiteId,
                                        link = state.selectedNewsItem.link,
                                        isExpanded = state.isDetailExpanded,
                                        onToggleExpand = { viewModel.toggleDetailExpansion() },
                                        onBack = {
                                            saveCurrentScrollState()
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

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
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
    val columnsCount = if (isCompact || isSplitView) 1 else 2

    val listState = rememberLazyListState()

    LaunchedEffect(selectedItem) {
        if (selectedItem != null) {
            val allFlattened = groupedNews.values.flatten()
            val index = allFlattened.indexOfFirst { it.link == selectedItem.link }
            if (index != -1) {
                listState.animateScrollToItem(index)
            }
        }
    }

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

                    val chunkedItems = items.chunked(columnsCount)
                    items(chunkedItems.size, key = { rowIndex -> "row_${websiteId}_$rowIndex" }) { rowIndex ->
                        val rowItems = chunkedItems.getOrNull(rowIndex) ?: return@items
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            rowItems.forEach { newsItem ->
                                Box(modifier = Modifier.weight(1f)) {
                                    val isSelected = selectedItem?.link == newsItem.link
                                    NewsItemCard(
                                        item = newsItem,
                                        isSelected = isSelected,
                                        onItemClick = onItemClick
                                    )
                                }
                            }
                            if (rowItems.size < columnsCount) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
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
    gridStates: MutableMap<String, LazyGridState>,
    getGridState: (String) -> LazyGridState,
    onSubTabSelected: (Int) -> Unit,
    isLoading: Boolean,
    isLoadingMore: Boolean,
    hasMorePages: Boolean,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    onItemClick: (NewsItem) -> Unit
) {
    val scope = rememberCoroutineScope()
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
            val gridState = getGridState("website_${website.id}_single")
            AdaptiveNewsList(
                newsItems = currentNews,
                selectedItem = selectedItem,
                gridState = gridState,
                isLoading = isLoading,
                isLoadingMore = isLoadingMore,
                hasMorePages = hasMorePages,
                onRefresh = onRefresh,
                onLoadMore = onLoadMore,
                onItemClick = onItemClick
            )
        }
        else -> {
            val subTabs = listOf("All") + categories.map { it.name }
            val subPagerState = rememberPagerState(pageCount = { subTabs.size })

            LaunchedEffect(subPagerState.currentPage) {
                if (selectedSubIndex != subPagerState.currentPage) {
                    onSubTabSelected(subPagerState.currentPage)
                }
            }

            val safeSubTab = if (subTabs.isEmpty()) 0 else selectedSubIndex.coerceIn(0, subTabs.size - 1)
            Column(modifier = Modifier.fillMaxSize()) {
                ScrollableTabRow(
                    selectedTabIndex = safeSubTab,
                    edgePadding = 16.dp,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    subTabs.forEachIndexed { index, title ->
                        Tab(
                            selected = safeSubTab == index,
                            onClick = {
                                onSubTabSelected(index)
                                scope.launch {
                                    subPagerState.animateScrollToPage(index)
                                }
                            },
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
                    val gridState = getGridState("website_${website.id}_sub_$subPageIndex")
                    if (subPageIndex == 0) {
                        WebsiteAllNewsList(
                            newsItems = websiteNews,
                            selectedItem = selectedItem,
                            gridState = gridState,
                            isLoading = isLoading,
                            isLoadingMore = isLoadingMore,
                            hasMorePages = hasMorePages,
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
                            gridState = gridState,
                            isLoading = isLoading,
                            isLoadingMore = isLoadingMore,
                            hasMorePages = hasMorePages,
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
    gridState: LazyGridState,
    isLoading: Boolean,
    isLoadingMore: Boolean,
    hasMorePages: Boolean,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    onItemClick: (NewsItem) -> Unit
) {
    AdaptiveNewsList(
        newsItems = newsItems,
        selectedItem = selectedItem,
        gridState = gridState,
        isLoading = isLoading,
        isLoadingMore = isLoadingMore,
        hasMorePages = hasMorePages,
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
    gridState: LazyGridState,
    isLoading: Boolean,
    isLoadingMore: Boolean,
    hasMorePages: Boolean,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    onItemClick: (NewsItem) -> Unit
) {
    AdaptiveNewsList(
        newsItems = newsItems,
        selectedItem = selectedItem,
        gridState = gridState,
        isLoading = isLoading,
        isLoadingMore = isLoadingMore,
        hasMorePages = hasMorePages,
        onRefresh = onRefresh,
        onLoadMore = onLoadMore,
        onItemClick = onItemClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdaptiveNewsList(
    newsItems: List<NewsItem>,
    selectedItem: NewsItem?,
    gridState: LazyGridState,
    isLoading: Boolean,
    isLoadingMore: Boolean,
    hasMorePages: Boolean,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    onItemClick: (NewsItem) -> Unit
) {
    val adaptiveInfo = currentWindowAdaptiveInfo()
    val isCompact = adaptiveInfo.windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.COMPACT
    val isSplitView = !isCompact && selectedItem != null
    val columnsCount = if (isCompact || isSplitView) 1 else 2

    // Detect scroll near end to trigger loadMore
    LaunchedEffect(gridState) {
        snapshotFlow {
            val layoutInfo = gridState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val lastVisibleItemIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            totalItems > 0 && lastVisibleItemIndex >= totalItems - 4
        }
            .distinctUntilChanged()
            .filter { it }
            .collect {
                onLoadMore()
            }
    }

    LaunchedEffect(selectedItem) {
        if (selectedItem != null) {
            val index = newsItems.indexOfFirst { it.link == selectedItem.link }
            if (index != -1) {
                gridState.animateScrollToItem(index)
            }
        }
    }

    PullToRefreshBox(
        isRefreshing = isLoading,
        onRefresh = onRefresh,
        modifier = Modifier.fillMaxSize()
    ) {
        if (newsItems.isEmpty() && !isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Không có tin tức trong chuyên mục này.")
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(columnsCount),
                state = gridState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(newsItems, key = { it.link }) { item ->
                    val isSelected = selectedItem?.link == item.link
                    NewsItemCard(
                        item = item,
                        isSelected = isSelected,
                        onItemClick = { clicked ->
                            onItemClick(clicked)
                        }
                    )
                }

                if (isLoadingMore) {
                    item(span = { GridItemSpan(columnsCount) }) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Đang tải thêm tin tức...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else if (!hasMorePages && newsItems.isNotEmpty()) {
                    item(span = { GridItemSpan(columnsCount) }) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Đã xảy ra lỗi hoặc hết tin tức. Vui lòng quay lại sau",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NewsItemCard(item: NewsItem, isSelected: Boolean = false, onItemClick: (NewsItem) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick(item) },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (!item.image.isBlank()) {
                AsyncImage(
                    model = item.image,
                    contentDescription = item.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(MaterialTheme.shapes.medium),
                    contentScale = ContentScale.Crop
                )
            }
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.shortDes,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 3,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
