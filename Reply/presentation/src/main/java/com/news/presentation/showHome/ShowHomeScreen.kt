package com.news.presentation.showHome

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
import com.news.domain.models.AppSiteCateByGroup
import com.news.domain.models.CategoryViewModel
import com.news.domain.models.News
import com.news.domain.models.ShowHomeDataModel
import com.news.presentation.R
import com.news.presentation.base.ShowError
import com.news.presentation.base.ShowLoading
import com.news.presentation.base.UiState
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
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import coil3.compose.AsyncImage
import com.news.domain.models.AppSite
import com.news.presentation.components.NewsDetailScreen
import com.news.presentation.newsByTagId.NewsByTagIdScreen
import com.news.presentation.newsByTagId.NewsByTagViewModel
import com.news.presentation.newsTag.ExtraPaneScreen
import com.news.presentation.showHomeChild.ShowHomeChildScreen
import com.news.presentation.showHomeChild.ShowHomeChildViewModel
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
    onSiteNameClicked: (Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val viewMode by viewModel.viewMode.collectAsStateWithLifecycle()
    ShowHomeScreen(uiState, viewMode, viewModel, onSiteNameClicked)
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun ShowHomeScreen(
    uiState: UiState<ShowHomeDataModel>,
    viewMode: String,
    viewModel: ShowHomeViewModel?,
    onNewsClicked: (Int) -> Unit
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
                                onNewsClicked = onNewsClicked
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


//            // Manage our back stack
//            //val backStack = rememberNavBackStack(ItemsList)
//            val backStack = remember { mutableStateListOf<NavKey>(ItemsList) }
//
//            // 1. Track if the user requested "Full Screen" for the detail
//            var isDetailFullScreen by remember { mutableStateOf(false) }
//
//            val standardDirective = calculatePaneScaffoldDirective(currentWindowAdaptiveInfo())
//
//            // 2. The strategy automatically hides the detail area when backStack.size == 1
//            // because we force maxHorizontalPartitions = 1
//            val listDetailStrategy = rememberListDetailSceneStrategy<Any>(
//                directive = if (backStack.size == 1 || isDetailFullScreen) {
//                    standardDirective.copy(maxHorizontalPartitions = 1)
//                } else {
//                    standardDirective
//                }
//            )

//            // 1. Wrap your List content in movableContentOf to preserve scroll state
//            val movableList = remember(uiState.data) {
//                movableContentOf {
//                    ExploreContent(uiState.data, {backStack.add(ItemDetail(it))},{backStack.add(ItemDetailSite(it))},onNewsClicked)
//
//                }
//            }
//            Scaffold { paddingValues ->
//                NavDisplay(
//
//                    entryDecorators = listOf(
//                        // Add the default decorators for managing scenes and saving state
//                        rememberSaveableStateHolderNavEntryDecorator(),
//                        // Then add the view model store decorator
//                        rememberViewModelStoreNavEntryDecorator(),
//                    ),
//                    backStack = backStack,
//                    modifier = Modifier
//                        .padding(paddingValues)
//                        .consumeWindowInsets(WindowInsets.statusBars),
//                    // onBack now takes 'count' because the strategy might pop multiple keys
//                    onBack = { backStack.removeLastOrNull()  },
//                    sceneStrategies = listOf(listDetailStrategy),
//
//                    entryProvider = entryProvider {
//                        entry<ItemsList>(
//                            // Metadata for the list pane, including a placeholder for the detail pane
//                            metadata = ListDetailSceneStrategy.listPane()
//                        ) {
//                            //movableList()
//                            ExploreContent(uiState.data, {backStack.add(ItemDetail(it))},{backStack.add(ItemDetailSite(it))},onNewsClicked)
//                        }
//                        entry<ItemDetail>(
//                            // Metadata for the detail pane
//                            metadata = ListDetailSceneStrategy.detailPane()
//                        ) { product ->
//                            NewsDetailScreen(
//                                news = product.id,
//                                onBack = {
//                                    //isDetailFullScreen = false
//                                    //backStack.removeLastOrNull()
//                                    backStack.removeAll { it is ItemDetail }
//                                },
//                                onExpand = {
//                                    isDetailFullScreen = !isDetailFullScreen
//                                    }
//                            )
//
//                        }
//                        entry<ItemDetailSite>(
//                            // Metadata for the detail pane
//                            metadata = ListDetailSceneStrategy.listPane(detailPlaceholder = {})
//                        ) { product ->
//                            val viewModel = hiltViewModel<ShowHomeChildViewModel, ShowHomeChildViewModel.Factory>(
//                                creationCallback = { factory ->
//                                    factory.create(product)
//                                }
//                            )
//
//                            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
//
//                            ShowHomeChildScreen(
//                                viewModel = viewModel,
//                                uiState = uiState
//                            ) { news ->
//                                backStack.add(ItemDetail(news))
//                            }
//                        }
//                        entry<ExtraScreen>(
//                            // Metadata for an optional extra pane
//                            metadata = ListDetailSceneStrategy.extraPane()
//                        ) {
//                            ExtraPaneScreen(
//                                modifier = Modifier.background(Color.LightGray)
//                            )
//                        }
//                    }
//                )
//            }


        }
    }
}

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
    onNewsClicked: (Int) -> Unit
) {
    LazyColumn(
        Modifier
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

        allEventCategories.CategoryViewModel.AppSiteCateByGroup?.forEach { category ->
            val categoryNews = category.LstNews ?: emptyList()
            if (categoryNews.isNotEmpty()) {
                stickyHeader {
                    Box(modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(horizontal = 16.dp)) {
                        ExploreHeader(category.Id, category.Name, category.Slug, onEventClickSiteName, onNewsClicked)
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
    selected: Boolean = false,
    onClick: () -> Unit, 
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
            contentColor = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (selected) 4.dp else 2.dp),
        border = if (selected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth()
        ) {
            AsyncImage(
                model = news.Image,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(width = 140.dp, height = 90.dp)
                    .clip(MaterialTheme.shapes.small)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = news.Date ?: "",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
//                    Spacer(modifier = Modifier.weight(1f))
//                    Icon(
//                        Icons.Outlined.ChatBubbleOutline,
//                        null,
//                        Modifier.size(12.dp),
//                        tint = MaterialTheme.colorScheme.onSurfaceVariant
//                    )
//                    Text(
//                        " 10",
//                        style = MaterialTheme.typography.labelSmall,
//                        color = MaterialTheme.colorScheme.onSurfaceVariant
//                    )
//                    Spacer(modifier = Modifier.width(8.dp))
//                    Icon(
//                        Icons.Outlined.FavoriteBorder,
//                        null,
//                        Modifier.size(12.dp),
//                        tint = MaterialTheme.colorScheme.onSurfaceVariant
//                    )
//                    Text(
//                        " 25",
//                        style = MaterialTheme.typography.labelSmall,
//                        color = MaterialTheme.colorScheme.onSurfaceVariant
//                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = news.Title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = news.Icon ?: news.Image,
                        contentDescription = null,
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = news.Source,
                        style = MaterialTheme.typography.labelSmall,
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
    selected: Boolean = false,
    onClick: () -> Unit, 
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
            contentColor = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (selected) 4.dp else 2.dp),
        border = if (selected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Column {
            AsyncImage(
                model = news.Image,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            )
            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = news.Date ?: "",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
//                    Spacer(modifier = Modifier.weight(1f))
//                    Icon(
//                        Icons.Outlined.ChatBubbleOutline,
//                        null,
//                        Modifier.size(10.dp),
//                        tint = MaterialTheme.colorScheme.onSurfaceVariant
//                    )
//                    Text(
//                        " 5",
//                        style = MaterialTheme.typography.labelSmall,
//                        color = MaterialTheme.colorScheme.onSurfaceVariant
//                    )
//                    Spacer(modifier = Modifier.width(4.dp))
//                    Icon(
//                        Icons.Outlined.FavoriteBorder,
//                        null,
//                        Modifier.size(10.dp),
//                        tint = MaterialTheme.colorScheme.onSurfaceVariant
//                    )
//                    Text(
//                        " 12",
//                        style = MaterialTheme.typography.labelSmall,
//                        color = MaterialTheme.colorScheme.onSurfaceVariant
//                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = news.Title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    minLines = 3
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
//                    AsyncImage(
//                        model = news.Icon ?: news.Image,
//                        contentDescription = null,
//                        modifier = Modifier
//                            .size(16.dp)
//                            .clip(CircleShape)
//                    )
//                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = news.Source,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun ExploreHeader(id:Int,title: String, slug: String = "", onEventClickSiteName: (AppSite) -> Unit,onNewsClicked: (Int) -> Unit) {
    Text(
        text = title, 
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 12.dp).clickable(){
            onNewsClicked(id-7)
        }
    )
}
@Composable
fun ExploreHeaderItem(title: String) {
    Text(text = title, modifier = Modifier.padding(9.dp))
}
///
@Composable
fun AutoAdvancePager(pageItems: List<News>, modifier: Modifier = Modifier, onEventClickNewsItem: (News) -> Unit) {
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
                    Text(
                        text = newsItem.Source,
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White,
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primary, shape = CircleShape)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )

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
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp)
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
