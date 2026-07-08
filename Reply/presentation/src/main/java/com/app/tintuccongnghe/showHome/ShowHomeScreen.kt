package com.app.tintuccongnghe.showHome

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Scaffold
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.PaneAdaptedValue
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldValue
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation.NavigableListDetailPaneScaffold
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.tintuccongnghe.domain.models.AppSiteCateByGroup
import com.app.tintuccongnghe.domain.models.CategoryViewModel
import com.app.tintuccongnghe.domain.models.News
import com.app.tintuccongnghe.domain.models.ShowHomeDataModel
import com.app.tintuccongnghe.presentation.R
import com.app.tintuccongnghe.base.ShowError
import com.app.tintuccongnghe.base.ShowLoading
import com.app.tintuccongnghe.base.UiState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.retain.RetainedValuesStoreRegistry
import androidx.compose.runtime.retain.retainRetainedValuesStoreRegistry
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.heightIn
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.window.core.layout.WindowWidthSizeClass
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import coil3.compose.AsyncImage
import com.app.tintuccongnghe.domain.models.AppSite
import com.app.tintuccongnghe.components.NewsDetailScreen
import com.app.tintuccongnghe.newsByTagId.NewsByTagIdScreen
import com.app.tintuccongnghe.newsByTagId.NewsByTagViewModel
import com.app.tintuccongnghe.newsTag.ExtraPaneScreen
import com.app.tintuccongnghe.showHomeChild.ShowHomeChildScreen
import com.app.tintuccongnghe.showHomeChild.ShowHomeChildViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlin.collections.map


// Define our navigation keys for list, detail, and an extra pane
@Serializable
data object ItemsList : NavKey

@Serializable
data class ItemDetail(val id: News) : NavKey

@Serializable
data class ItemDetailSite(val slug: AppSite) : NavKey

@Serializable
data object ExtraScreen : NavKey

@Composable
fun ShowHomeRoute(
    viewModel: ShowHomeViewModel = hiltViewModel(),
    onNewsClicked: ((News) -> Unit)? = null,
    onTabSelected: (String) -> Unit,
    selectedNews: News? = null
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val viewMode by viewModel.viewMode.collectAsStateWithLifecycle()
    ShowHomeScreen(uiState, viewMode, viewModel, onNewsClicked, onTabSelected, selectedNews)
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun ShowHomeScreen(
    uiState: UiState<ShowHomeDataModel>,
    viewMode: String,
    viewModel: ShowHomeViewModel?,
    onNewsClicked: ((News) -> Unit)? = null,
    onTabSelected: (String) -> Unit,
    selectedNews: News? = null
) {
    when (uiState) {
        is UiState.Loading -> {
            ShowLoading()
        }

        is UiState.Error -> {
            ShowError(
                text = stringResource(R.string.something_went_wrong),
                retryEnabled = true
            ) { viewModel?.fetchShowHome() }
        }

        is UiState.Success -> {
            if (onNewsClicked != null) {
                PullToRefreshBox(
                    isRefreshing = false,
                    onRefresh = { viewModel?.fetchShowHome() },
                    modifier = Modifier.fillMaxSize()
                ) {
                    ExploreContent(
                        allEventCategories = uiState.data,
                        viewMode = viewMode,
                        selectedNews = selectedNews,
                        onViewModeChange = { viewModel?.setViewMode(it) },
                        onEventClick = onNewsClicked,
                        onEventClickSiteName = {},
                        onTabSelected = onTabSelected
                    )
                }
            } else {
                // 1. Setup the navigator
                val navigator = rememberListDetailPaneScaffoldNavigator<Any>()

                // 2. Manual toggle state for the detail full-screen mode
                var isDetailFullScreen by remember { mutableStateOf(false) }

                // 2.5 Pull to refresh state
                val isRefreshing = uiState is UiState.Loading && navigator.scaffoldValue.secondary != PaneAdaptedValue.Hidden

                val currentDestination = navigator.currentDestination
                val currentSelectedItem = currentDestination?.contentKey

                // Keep track of the last selected items for each pane
                var lastSelectedNews by remember { mutableStateOf<News?>(null) }
                var lastSelectedTag by remember { mutableStateOf<String?>(null) }

                LaunchedEffect(currentDestination) {
                    when (currentDestination?.pane) {
                        ListDetailPaneScaffoldRole.Detail -> lastSelectedNews = currentDestination.contentKey as? News
                        ListDetailPaneScaffoldRole.Extra -> lastSelectedTag = currentDestination.contentKey as? String
                        else -> {}
                    }
                }

                // 3. We manually define the Scaffold Value to force Full Screen behaviors
                val manualValue = when {
                    // Case A: Nothing selected -> Force List (Secondary) to fill screen
                    currentSelectedItem == null -> {
                        ThreePaneScaffoldValue(
                            primary = PaneAdaptedValue.Hidden,     // Detail
                            secondary = PaneAdaptedValue.Expanded, // List
                            tertiary = PaneAdaptedValue.Hidden
                        )
                    }
                    // Case B: Item selected + Full Screen Toggled -> Force Detail (Primary) to fill screen
                    isDetailFullScreen -> {
                        ThreePaneScaffoldValue(
                            primary = PaneAdaptedValue.Expanded, // Detail
                            secondary = PaneAdaptedValue.Hidden, // List
                            tertiary = PaneAdaptedValue.Hidden
                        )
                    }
                    // Case C: Normal selection -> Use the navigator's adaptive logic (Split-screen on tablets)
                    else -> navigator.scaffoldValue
                }
                val scope = rememberCoroutineScope()
                // 2. The Scaffold handles the actual layout
                ListDetailPaneScaffold(
                    directive = navigator.scaffoldDirective,
                    value = manualValue,
                    listPane = {
                        AnimatedPane {
                            PullToRefreshBox(
                                isRefreshing = uiState is UiState.Loading,
                                onRefresh = { viewModel?.fetchShowHome() },
                                modifier = Modifier.fillMaxSize()
                            ) {
                                ExploreContent(
                                    uiState.data,
                                    viewMode = viewMode,
                                    selectedNews = currentSelectedItem as? News,
                                    onViewModeChange = { viewModel?.setViewMode(it) },
                                    onEventClick = {
                                        scope.launch {
                                            navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, it)
                                        }
                                    },
                                    onEventClickSiteName = {},
                                    onTabSelected = onTabSelected
                                )
                            }
                        }
                    },
                    detailPane = {
                        AnimatedPane {
                            if (lastSelectedNews != null) {
                                NewsDetailScreen(
                                    news = lastSelectedNews!!,
                                    onBack = {
                                        if (isDetailFullScreen) isDetailFullScreen = false
                                        scope.launch {
                                            navigator.navigateBack()
                                        }
                                    },
                                    onExpand = {
                                        isDetailFullScreen = !isDetailFullScreen
                                    },
                                    showExpandButton = navigator.scaffoldDirective.maxHorizontalPartitions > 1,
                                    onTagClick = { tagSlug ->
                                        scope.launch {
                                            navigator.navigateTo(ListDetailPaneScaffoldRole.Extra, tagSlug)
                                        }
                                    }
                                )
                            }
                        }
                    },
                    extraPane = {
                        AnimatedPane {
                            if (lastSelectedTag != null) {
                                val tagViewModel = hiltViewModel<NewsByTagViewModel, NewsByTagViewModel.Factory>(
                                    key = lastSelectedTag,
                                    creationCallback = { factory -> factory.create(lastSelectedTag!!) }
                                )
                                NewsByTagIdScreen(
                                    tagSlug = lastSelectedTag!!,
                                    viewModel = tagViewModel,
                                    onBack = {
                                        scope.launch {
                                            navigator.navigateBack()
                                        }
                                    },
                                    onNewsClick = { news ->
                                        scope.launch {
                                            navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, news)
                                        }
                                    }
                                )
                            }
                        }
                    }
                )
            }
        }
    }
}


//        }
//    }
//}

@Composable
fun ShowHomeContent(x0: ShowHomeDataModel, x1: (News) -> Unit) {
    LazyColumn {
        items(
            count = x0.LstNewsHeader?.size ?: 0,
            itemContent = { NewsHeaderItem(x0.LstNewsHeader!![it], x1) })
    }
}

@Composable
fun NewsHeaderItem(x0: News, x1: (News) -> Unit) {
    Text(x0.Title)
}

@Composable
fun CategoryHeader(x0: ArrayList<AppSiteCateByGroup>?) {
    TODO("Not yet implemented")
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ExploreContent(
    allEventCategories: ShowHomeDataModel,
    viewMode: String,
    selectedNews: News? = null,
    onViewModeChange: (String) -> Unit,
    onEventClick: (News) -> Unit,
    onEventClickSiteName: (AppSite) -> Unit,
    onTabSelected: (String) -> Unit
) {
    val listState = rememberLazyListState()

    LaunchedEffect(selectedNews) {
        if (selectedNews != null) {
            var targetIndex = -1
            var currentIndex = 0

            // Featured posts
            currentIndex++ // for item { ... } at index 0

            // Latest posts header
            currentIndex++ // for stickyHeader { ... } at index 1

            allEventCategories.CategoryViewModel.AppSiteCateByGroup?.forEach { category ->
                val categoryNews = category.LstNews ?: emptyList()
                if (categoryNews.isNotEmpty()) {
                    // ExploreHeader
                    if (categoryNews.any { it.Id == selectedNews.Id }) {
                        // Found in this category
                        currentIndex++ // Skip the category header itself to get to items
                        
                        if (viewMode == "grid") {
                            val chunks = categoryNews.chunked(2)
                            val chunkIndex = chunks.indexOfFirst { rowItems -> 
                                rowItems.any { it.Id == selectedNews.Id } 
                            }
                            if (chunkIndex != -1) {
                                targetIndex = currentIndex + chunkIndex
                            }
                        } else {
                            val itemIndex = categoryNews.indexOfFirst { it.Id == selectedNews.Id }
                            if (itemIndex != -1) {
                                targetIndex = currentIndex + itemIndex
                            }
                        }
                        return@forEach
                    } else {
                        // Not in this category, skip it all
                        currentIndex++ // The header
                        if (viewMode == "grid") {
                            currentIndex += categoryNews.chunked(2).size
                        } else {
                            currentIndex += categoryNews.size
                        }
                    }
                }
            }

            if (targetIndex != -1) {
                listState.animateScrollToItem(targetIndex)
            }
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        item {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Featured posts",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(
                    modifier = Modifier.padding(bottom = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                
                AutoAdvancePager(
                    allEventCategories.LstNewsHeader ?: emptyList(),
                    onEventClickNewsItem = onEventClick,
                    onTabSelected = onTabSelected,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                )
            }
        }

        stickyHeader {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Latest posts",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { onViewModeChange("grid") },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = if (viewMode == "grid") MaterialTheme.colorScheme.primary else Color.Transparent,
                            contentColor = if (viewMode == "grid") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.outline
                        ),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.GridView, contentDescription = "Grid View", modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(
                        onClick = { onViewModeChange("list") },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = if (viewMode == "list") MaterialTheme.colorScheme.primary else Color.Transparent,
                            contentColor = if (viewMode == "list") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.outline
                        ),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.List, contentDescription = "List View", modifier = Modifier.size(20.dp))
                    }
                }
            }
        }

        allEventCategories.CategoryViewModel.AppSiteCateByGroup?.forEachIndexed { index, category ->
            val categoryNews = category.LstNews ?: emptyList()
            if (categoryNews.isNotEmpty()) {
                stickyHeader {
                    Box(modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(horizontal = 16.dp)) {
                        ExploreHeader(category.Key, category.Name, category.Slug, onEventClickSiteName, onTabSelected)
                    }
                }

                if (viewMode == "grid") {
                    val chunks = categoryNews.chunked(2)
                    items(chunks.size) { index ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            val rowItems = chunks[index]
                            rowItems.forEach { news ->
                                NewsGridItem(
                                    news = news,
                                    selected = news.Id == selectedNews?.Id,
                                    onClick = { onEventClick(news) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (rowItems.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                } else {
                    items(categoryNews.size) { index ->
                        val news = categoryNews[index]
                        NewsListItem(
                            news = news,
                            selected = news.Id == selectedNews?.Id,
                            onClick = { onEventClick(news) },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NewsListItem(
    news: News,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    onClick: () -> Unit
) {
    val adaptiveInfo = currentWindowAdaptiveInfo()
    val isExpanded = adaptiveInfo.windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.EXPANDED
    
    val titleStyle = if (isExpanded) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleMedium
    val bodyStyle = if (isExpanded) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodySmall
    val labelStyle = if (isExpanded) MaterialTheme.typography.labelMedium else MaterialTheme.typography.labelSmall
    val padding = if (isExpanded) 16.dp else 8.dp

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
            contentColor = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
        ),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = if (selected) 4.dp else 1.dp),
        border = if (selected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Row(
            modifier = Modifier
                .padding(padding)
                .fillMaxWidth()
                .height(IntrinsicSize.Max),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(0.35f)
                    .fillMaxHeight()
                    .heightIn(min = if (isExpanded) 140.dp else 100.dp)
                    .clip(MaterialTheme.shapes.small)
            ) {
                AsyncImage(
                    model = news.Image,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.width(if (isExpanded) 20.dp else 12.dp))

            Column(
                modifier = Modifier
                    .weight(0.65f)
                    .fillMaxHeight()
                    .padding(vertical = 2.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = news.Title,
                        style = titleStyle,
                        fontWeight = FontWeight.Bold,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = titleStyle.lineHeight
                    )

                    val shortDes = news.ShortDes
                    if (!shortDes.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.height(if (isExpanded) 16.dp else 8.dp))
                        Text(
                            text = shortDes,
                            style = bodyStyle,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Spacer(modifier = Modifier.height(if (isExpanded) 16.dp else 8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        text = news.Source,
                        style = labelStyle,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = news.Date ?: "",
                        style = labelStyle,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun NewsGridItem(
    news: News,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    onClick: () -> Unit
) {
    val adaptiveInfo = currentWindowAdaptiveInfo()
    val isExpanded = adaptiveInfo.windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.EXPANDED

    val titleStyle = if (isExpanded) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleSmall
    val bodyStyle = if (isExpanded) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodySmall
    val labelStyle = MaterialTheme.typography.labelSmall
    val padding = if (isExpanded) 16.dp else 12.dp

    Card(
        onClick = onClick,
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
            contentColor = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
        ),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = if (selected) 4.dp else 1.dp),
        border = if (selected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Column {
            AsyncImage(
                model = news.Image,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.5f)
            )
            Column(
                modifier = Modifier
                    .padding(padding)
                    .height(IntrinsicSize.Min)
            ) {
                Text(
                    text = news.Title,
                    style = titleStyle,
                    fontWeight = FontWeight.Bold,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    minLines = 3
                )

                val shortDes = news.ShortDes
                if (!shortDes.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(if (isExpanded) 8.dp else 4.dp))
                    Text(
                        text = shortDes,
                        style = bodyStyle,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        minLines = 2
                    )
                }

                Spacer(modifier = Modifier.height(if (isExpanded) 16.dp else 12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = news.Source,
                        style = labelStyle,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = news.Date ?: "",
                        style = labelStyle,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun ExploreHeader(key: String, title: String, slug: String = "", onEventClickSiteName: (AppSite) -> Unit, onTabSelected: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .clickable { onTabSelected(key) }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                textDecoration = TextDecoration.Underline
            ),
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = "See more",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
    }
}
@Composable
fun ExploreHeaderItem(title: String) {
    Text(text = title, modifier = Modifier.padding(9.dp))
}
///
@Composable
fun AutoAdvancePager(
    pageItems: List<News>,
    modifier: Modifier = Modifier,
    onEventClickNewsItem: (News) -> Unit,
    onTabSelected: (String) -> Unit
) {
    if (pageItems.isEmpty()) return

    Box(modifier = modifier) {
        val pagerState = rememberPagerState(pageCount = { pageItems.size })
        val pagerIsDragged by pagerState.interactionSource.collectIsDraggedAsState()

        val pageInteractionSource = remember { MutableInteractionSource() }
        val pageIsPressed by pageInteractionSource.collectIsPressedAsState()

        // Stop auto-advancing when pager is dragged or one of the pages is pressed
        val autoAdvance = !pagerIsDragged && !pageIsPressed

        LaunchedEffect(pagerState, pageInteractionSource, autoAdvance, pageItems.size) {
            if (autoAdvance && pageItems.isNotEmpty()) {
                while (true) {
                    delay(3000)
                    if (pagerState.pageCount > 0) {
                        val nextPage = (pagerState.currentPage + 1) % pagerState.pageCount
                        pagerState.animateScrollToPage(nextPage)
                    }
                }
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val newsItem = pageItems[page]
            Box(Modifier.fillMaxSize().clickable {
                onEventClickNewsItem(newsItem)
            }) {
                AsyncImage(
                    model = newsItem.Image,
                    contentDescription = newsItem.Title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.7f)
                                )
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    newsItem.App_Site_Name?.let { siteName ->
                        Text(
                            text = siteName,
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White,
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.primary, shape = CircleShape)
                                .clip(CircleShape)
                                .clickable {
                                    newsItem.App_Site_Slug?.let { slug ->
                                        onTabSelected(slug)
                                    }
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = newsItem.Title,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        PagerIndicator(
            pageCount = pageItems.size, 
            currentPageIndex = pagerState.currentPage,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 8.dp)
        )
    }
}

@Composable
fun PagerIndicator(pageCount: Int, currentPageIndex: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .wrapContentHeight()
            .wrapContentSize(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pageCount) { iteration ->
            val color = if (currentPageIndex == iteration) Color.White else Color.White.copy(alpha = 0.5f)
            Box(
                modifier = Modifier
                    .padding(4.dp)
                    .clip(CircleShape)
                    .background(color)
                    .size(8.dp)
            )
        }
    }
}

/**
 * Returns a [RetainedValuesStoreNavEntryDecorator] that is remembered across recompositions backed
 * by [registry].
 *
 * The underlying storage is controlled by the provided [registry]. By default, a new
 * [RetainedValuesStoreRegistry] is retained at this point in the composition hierarchy and will be
 * destroyed when the composition is permanently discarded or when the returned decorator is removed
 * from the composition hierarchy. If you need the backing storage of this decorator to have a
 * different lifespan, you can manually manage and provide a [RetainedValuesStoreRegistry] with the
 * intended lifespan.
 *
 * @param registry The underlying [RetainedValuesStoreRegistry] used to provide
 *   [RetainedValuesStore] instances to [NavEntries][NavEntry]. This instance should be retained to
 *   properly survive destruction and recreation scenarios.
 */
@Composable
fun <T : Any> rememberRetainedValuesStoreNavEntryDecorator(
    registry: RetainedValuesStoreRegistry = retainRetainedValuesStoreRegistry()
): RetainedValuesStoreNavEntryDecorator<T> {
    return remember(registry) {
        RetainedValuesStoreNavEntryDecorator(registry)
    }
}

/**
 * Provides the content of each [NavEntry] with a dedicated [RetainedValuesStore] so that each nav
 * entry may retain its own values.
 *
 * @param registry The underlying [RetainedValuesStoreRegistry] used to provide
 *   [RetainedValuesStore] instances to [NavEntries][NavEntry]. This instance should be retained to
 *   properly survive destruction and recreation scenarios.
 */
class RetainedValuesStoreNavEntryDecorator<T : Any>(
    registry: RetainedValuesStoreRegistry,
) : NavEntryDecorator<T>(
    onPop = { key ->
        registry.clearChild(key)
    },
    decorate = { entry ->
        registry.LocalRetainedValuesStoreProvider(entry.contentKey) { entry.Content() }
    },
)
