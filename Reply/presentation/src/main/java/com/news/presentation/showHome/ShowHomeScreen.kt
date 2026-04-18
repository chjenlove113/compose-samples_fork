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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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
    ShowHomeScreen(uiState, viewModel, onSiteNameClicked)
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun ShowHomeScreen(
    uiState: UiState<ShowHomeDataModel>,
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
            val navigator = rememberListDetailPaneScaffoldNavigator<News>()

            // 2. Manual toggle state for the detail full-screen mode
            var isDetailFullScreen by remember { mutableStateOf(false) }

            val currentSelectedItem = navigator.currentDestination?.contentKey

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
                        ExploreContent(uiState.data
                            , {
                                scope.launch {
                                    navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, it)

                                }
                            }
                            ,{}
                            ,onNewsClicked)
//                        ListContent(onItemClick = { id ->
//                            navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, id)
//
//                        })
                    }
                },
                detailPane = {
                    AnimatedPane {
                        val selectedId = navigator.currentDestination?.contentKey
                        if (selectedId != null) {
                                                        NewsDetailScreen(
                                news = selectedId,
                                onBack = {
                                    if (isDetailFullScreen) isDetailFullScreen = false
                                    scope.launch {
                                        navigator.navigateBack()

                                    }
                                },
                                onExpand = {
                                    isDetailFullScreen = !isDetailFullScreen
                                    }
                            )
//                            DetailContent(
//                                id = selectedId,
//                                isFullScreen = isDetailFullScreen,
//                                onBack = {
//                                    if (isDetailFullScreen) isDetailFullScreen = false
//                                    navigator.navigateBack()
//                                },
//                                onToggleFullScreen = { isDetailFullScreen = !isDetailFullScreen }
//                            )
                        } else {
                            //EmptyDetailView()
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

@Composable
fun ExploreContent(allEventCategories: ShowHomeDataModel, onEventClick: (News) -> Unit, onEventClickSiteName: (AppSite) -> Unit,onNewsClicked: (Int) -> Unit) {
    LazyColumn(Modifier.fillMaxSize()) {
        item {
            AutoAdvancePager(
                allEventCategories.LstNewsHeader ?: emptyList(), 
                onEventClickNewsItem = onEventClick,
                modifier = Modifier.fillMaxWidth().height(300.dp)
            )
        }
        allEventCategories.CategoryViewModel.AppSiteCateByGroup?.forEach { (catId, catName, zz, yy,catSlug,catKey) ->
            EventItem(catId, catName, catSlug,yy ?: emptyList(), onEventClick,onEventClickSiteName, onNewsClicked)
        }
    }
}

// LazyListScope Item
fun LazyListScope.EventItem(
    catId: Int,
    catName: String,
    catSlug: String?,
    eventList: List<News>,
    onEventClick: (News) -> Unit,
    onEventClickSite: (AppSite) -> Unit,
    onNewsClicked: (Int) -> Unit
) {
    stickyHeader {
//        ExploreHeader(catName, slug = catSlug ?: "",onEventClickSite)
        ExploreHeader(catId,catName, slug = catSlug ?: "",onEventClickSite, onNewsClicked)
    }
    items(eventList.size) { index ->
        Card(onClick = { onEventClick(eventList[index]) }) {
            Column(modifier = Modifier.padding(horizontal = 9.dp)) {
                ExploreHeaderItem(eventList[index].Title)
                Spacer(modifier = Modifier.height(8.dp))
            }

        }
    }
}

@Composable
fun ExploreHeader(id:Int,title: String, slug: String = "", onEventClickSiteName: (AppSite) -> Unit,onNewsClicked: (Int) -> Unit) {
    Text(text = title, modifier = Modifier.padding(9.dp).clickable(){
        onNewsClicked(id-7)
        //onEventClickSiteName(AppSite(0,slug,slug,title,"","","",""))
    })
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
