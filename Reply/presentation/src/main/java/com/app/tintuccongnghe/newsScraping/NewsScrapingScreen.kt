package com.app.tintuccongnghe.newsScraping

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
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
import coil3.compose.AsyncImage
import com.app.tintuccongnghe.domain.models.NewsCategory
import com.app.tintuccongnghe.domain.models.NewsItem
import com.app.tintuccongnghe.domain.models.NewsWebsite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsScrapingScreen(
    onItemClick: (String, String) -> Unit = { _, _ -> },
    viewModel: NewsScrapingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Firestore Nested TabPage (Home & Websites)") },
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
                                    newsItems = state.homeNews,
                                    isLoading = state.isLoading,
                                    onRefresh = { viewModel.refresh() },
                                    onItemClick = onItemClick
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
                                        onSubTabSelected = { subIndex ->
                                            viewModel.selectSubTab(website.id, subIndex)
                                        },
                                        isLoading = state.isLoading,
                                        isLoadingMore = state.isLoadingMore,
                                        onRefresh = { viewModel.refresh() },
                                        onLoadMore = { viewModel.loadMore() },
                                        onItemClick = onItemClick
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeNewsContent(
    newsItems: List<NewsItem>,
    isLoading: Boolean,
    onRefresh: () -> Unit,
    onItemClick: (String, String) -> Unit
) {
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
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(newsItems) { item ->
                    NewsItemCard(item = item, onItemClick = onItemClick)
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
    onSubTabSelected: (Int) -> Unit,
    isLoading: Boolean,
    isLoadingMore: Boolean,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    onItemClick: (String, String) -> Unit
) {
    when {
        categories.isEmpty() -> {
            // Case 1: categories.isEmpty()
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
            // Case 2: categories.size == 1 (Hide Sub-TabRow and Pager entirely)
            val singleCategory = categories.first()
            val currentNews = categoryNews[singleCategory.id] ?: emptyList()
            CategoryNewsList(
                newsItems = currentNews,
                isLoading = isLoading,
                isLoadingMore = isLoadingMore,
                onRefresh = onRefresh,
                onLoadMore = onLoadMore,
                onItemClick = onItemClick
            )
        }
        else -> {
            // Case 3: categories.size > 1 (Insert virtual "All" tab + ScrollableTabRow + HorizontalPager)
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
                            isLoading = isLoading,
                            onRefresh = onRefresh,
                            onItemClick = onItemClick
                        )
                    } else {
                        val cat = categories.getOrNull(subPageIndex - 1)
                        val currentNews = if (cat != null) categoryNews[cat.id] ?: emptyList() else emptyList()
                        CategoryNewsList(
                            newsItems = currentNews,
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
    isLoading: Boolean,
    onRefresh: () -> Unit,
    onItemClick: (String, String) -> Unit
) {
    PullToRefreshBox(
        isRefreshing = isLoading,
        onRefresh = onRefresh,
        modifier = Modifier.fillMaxSize()
    ) {
        if (newsItems.isEmpty() && !isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Không có bài viết tổng hợp nào cho website này.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(newsItems) { item ->
                    NewsItemCard(item = item, onItemClick = onItemClick)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryNewsList(
    newsItems: List<NewsItem>,
    isLoading: Boolean = false,
    isLoadingMore: Boolean = false,
    onRefresh: () -> Unit = {},
    onLoadMore: () -> Unit = {},
    onItemClick: (String, String) -> Unit
) {
    PullToRefreshBox(
        isRefreshing = isLoading,
        onRefresh = onRefresh,
        modifier = Modifier.fillMaxSize()
    ) {
        if (newsItems.isEmpty() && !isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Không có bài viết trong chuyên mục này.")
            }
        } else {
            val listState = rememberLazyListState()
            val shouldLoadMore = remember {
                derivedStateOf {
                    val layoutInfo = listState.layoutInfo
                    val totalItemsNumber = layoutInfo.totalItemsCount
                    val lastVisibleItemIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                    totalItemsNumber > 0 && lastVisibleItemIndex >= totalItemsNumber - 3
                }
            }

            LaunchedEffect(shouldLoadMore.value) {
                if (shouldLoadMore.value && !isLoadingMore) {
                    onLoadMore()
                }
            }

            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(newsItems) { item ->
                    NewsItemCard(item = item, onItemClick = onItemClick)
                }

                if (isLoadingMore) {
                    item {
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

@Composable
fun NewsItemCard(item: NewsItem, onItemClick: (String, String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                if (item.link.isNotEmpty() && item.websiteId.isNotEmpty()) {
                    onItemClick(item.websiteId, item.link)
                }
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
