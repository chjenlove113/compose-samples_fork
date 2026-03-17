package com.news.presentation.showHome

import android.util.Log
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
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
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
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import coil3.compose.AsyncImage
import com.news.domain.models.AppSite
import com.news.presentation.base.TabItem
import com.news.presentation.components.NewsDetailScreen
import com.news.presentation.newsTag.ExtraPaneScreen
import com.news.presentation.showHomeChild.ShowHomeChildScreen
import com.news.presentation.showHomeChild.ShowHomeChildViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlin.collections.forEach
import kotlin.collections.map


// Define our navigation keys for list, detail, and an extra pane
@Serializable
data object ItemsList2 : NavKey

@Serializable
data class ItemDetail2(val id: News) : NavKey

@Serializable
data class ItemDetailSite2(val slug: AppSite) : NavKey

@Serializable
data object ExtraScreen2 : NavKey

@Composable
fun ShowHomeRoute2(
    viewModel: ShowHomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ShowHomeScreen2(uiState, viewModel, {0})
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun ShowHomeScreen2(
    uiState: UiState<ShowHomeDataModel>,
    viewModel: ShowHomeViewModel?,
    onSiteNameClicked: () -> Int
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
            ////ShowHomeContent(uiState.data, onNewsClicked)
            //ExploreContent(uiState.data, {})

            // Manage our back stack
            val backStack = rememberNavBackStack(ItemsList)
            // Create the ListDetailSceneStrategy
            val listDetailStrategy = rememberListDetailSceneStrategy<Any>()
            Scaffold { paddingValues ->
                NavDisplay(
                    entryDecorators = listOf(
                        // Add the default decorators for managing scenes and saving state
                        rememberSaveableStateHolderNavEntryDecorator(),
                        // Then add the view model store decorator
                        rememberViewModelStoreNavEntryDecorator()
                    ),
                    backStack = backStack,
                    modifier = Modifier
                        .padding(paddingValues)
                        .consumeWindowInsets(WindowInsets.statusBars),
                    // onBack now takes 'count' because the strategy might pop multiple keys
                    onBack = { backStack.removeLastOrNull()  },
                    sceneStrategy = listDetailStrategy,

                    entryProvider = entryProvider {
                        entry<ItemsList>(
                            // Metadata for the list pane, including a placeholder for the detail pane
                            metadata = ListDetailSceneStrategy.listPane(
                            )
                        ) {
                            var selectedTabIndex00 by remember { mutableIntStateOf(2) }
// Callback function to update the state
                            val updateResult: (Int) -> Unit = { newValue ->
                                selectedTabIndex00 = newValue
                            }
                            Log.d("selectedTabIndex00",selectedTabIndex00.toString())
                            val tabsItem = arrayListOf<TabItem>()

                            tabsItem.add(TabItem(title = "Home", screen = { ShowHomeRoute(onSiteNameClicked = updateResult) }))
                            Log.d("selectedTabIndex00",selectedTabIndex00.toString())
                            uiState.data.CategoryViewModel.AppSiteCateByGroup?.forEach {
                                val appSite = ItemDetailSite(
                                    AppSite(
                                        it.Id,
                                        it.Slug,
                                        it.Key,
                                        it.Name,
                                        "",
                                        "",
                                        "",
                                        ""
                                    )
                                )

                                val viewModel = hiltViewModel<ShowHomeChildViewModel, ShowHomeChildViewModel.Factory>(key = it.Key,
                                    creationCallback = { factory ->
                                        factory.create(
                                            appSite
                                        )
                                    }
                                )
                                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                                tabsItem.add(
                                    TabItem(title = it.Name, screen = {
                                        ShowHomeChildScreen(
                                            viewModel = viewModel,
                                            uiState = uiState
                                        ) {
                                            //backStack.add(com.news.presentation.newsTag.ExtraScreen) // Navigate to an extra pane
                                        }
                                    })

                                )


                            }
                            val tabs by remember {mutableStateOf(
                                tabsItem)
                            }

                            val scope = rememberCoroutineScope()
                            // 2. Manage the selected tab state
                            //var selectedTabIndex00 by remember { mutableIntStateOf(selectedTabIndex0) }
                            val pagerState = rememberPagerState(pageCount = { tabs.size }, initialPage = selectedTabIndex00)

                            Column {
                                // Tab Row implementation
                                ScrollableTabRow(selectedTabIndex = selectedTabIndex00) {
                                    tabs.forEachIndexed { index, tab ->
                                        Tab(
                                            selected = selectedTabIndex00 == index,

                                            onClick = {
                                                selectedTabIndex00 = index
                                                Log.d("onClick", "Page changed to $index")
                                                scope.launch {
                                                    pagerState.animateScrollToPage(selectedTabIndex00)
                                                }
                                            },
                                            text = { Text(text = tab.title, maxLines = 1) }
                                        )
                                    }
                                }

                                // Horizontal Pager implementation (The ViewPager equivalent)
                                HorizontalPager(
                                    state = pagerState,
                                    modifier = Modifier.fillMaxSize()
                                ) { page ->
                                    // Display the content (screen composable) for the current page/tab
                                    tabs[page].screen()
                                }
                            }

                            // Synchronize pager swipes with the TabRow indicator
                            LaunchedEffect(selectedTabIndex00) {
                                // No explicit synchronization is needed here because `selectedTabIndex`
                                // in `TabRow` is already observing `pagerState.currentPage`
                                pagerState.animateScrollToPage(selectedTabIndex00)

                                // Collect from the a snapshotFlow reading the currentPage
                                snapshotFlow { pagerState.currentPage }.collect { page ->
                                    // Do something with each page change, for example:
                                    // viewModel.sendPageSelectedEvent(page)
                                    selectedTabIndex00 = page
                                    pagerState.animateScrollToPage(selectedTabIndex00)
                                    Log.d("snapshotFlow", "Page changed to $page")
                                }
                            }

                            //ExploreContent2(tabs, selectedTabIndex00, {backStack.add(ItemDetail(it))},{backStack.add(ItemDetailSite(it))})

                        }
                        entry<ItemDetail>(
                            // Metadata for the detail pane
                            metadata = ListDetailSceneStrategy.detailPane()
                        ) { product ->
                            val id = product.id
                            NewsDetailScreen(
                                modifier = Modifier.background(Color.Red.copy(alpha = 0.4f)),
                                name = id.Title, onItemClick = {}
                            ) {
                                backStack.add(com.news.presentation.newsTag.ExtraScreen) // Navigate to an extra pane
                            }
                        }
                        entry<ItemDetailSite>(
                            // Metadata for the detail pane
                            metadata = ListDetailSceneStrategy.listPane(detailPlaceholder = {})
                        ) { product ->
                            val id = product.slug
                            val viewModel = hiltViewModel<ShowHomeChildViewModel, ShowHomeChildViewModel.Factory>(
                                // Note: We need a new ViewModel for every new RouteB instance. Usually
                                // we would need to supply a `key` String that is unique to the
                                // instance, however, the ViewModelStoreNavEntryDecorator (supplied
                                // above) does this for us, using `NavEntry.contentKey` to uniquely
                                // identify the viewModel.
                                //
                                // tl;dr: Make sure you use rememberViewModelStoreNavEntryDecorator()
                                // if you want a new ViewModel for each new navigation key instance.
                                creationCallback = { factory ->
                                    factory.create(product)
                                }
                            )

                            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                            ShowHomeChildScreen(
                                viewModel = viewModel,
                                uiState = uiState
                            ) {
                                backStack.add(com.news.presentation.newsTag.ExtraScreen) // Navigate to an extra pane
                            }
                        }
                        entry<ExtraScreen>(
                            // Metadata for an optional extra pane
                            metadata = ListDetailSceneStrategy.extraPane()
                        ) {
                            ExtraPaneScreen(
                                modifier = Modifier.background(Color.LightGray)
                            )
                        }
                    }
                )
            }


        }
    }
}

@Composable
fun ExploreContent2(tabs: ArrayList<TabItem>, selectedTabIndex0: Int = 0, onEventClick: (News) -> Unit, onEventClickSiteName: (AppSite) -> Unit) {

        val scope = rememberCoroutineScope()
        // 2. Manage the selected tab state
        var selectedTabIndex00 by remember { mutableIntStateOf(selectedTabIndex0) }
        val pagerState = rememberPagerState(pageCount = { tabs.size }, initialPage = selectedTabIndex00)

        Column {
            // Tab Row implementation
            ScrollableTabRow(selectedTabIndex = selectedTabIndex00) {
                tabs.forEachIndexed { index, tab ->
                    Tab(
                        selected = selectedTabIndex00 == index,
                        onClick = {
                            selectedTabIndex00 = index
                            scope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        text = { Text(text = tab.title, maxLines = 1) }
                    )
                }
            }

            // Horizontal Pager implementation (The ViewPager equivalent)
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->

                // Display the content (screen composable) for the current page/tab
                tabs[page].screen()
            }
        }

        // Synchronize pager swipes with the TabRow indicator
        LaunchedEffect(pagerState.currentPage) {
            // No explicit synchronization is needed here because `selectedTabIndex`
            // in `TabRow` is already observing `pagerState.currentPage`

            // Collect from the a snapshotFlow reading the currentPage
            snapshotFlow { pagerState.currentPage }.collect { page ->
                // Do something with each page change, for example:
                // viewModel.sendPageSelectedEvent(page)
                selectedTabIndex00 = page
                Log.d("selectedTabIndex00", "Page changed to $page")
            }
        }
}

@Composable
fun ExploreContent3(tabs: ArrayList<TabItem>, selectedTabIndex0: Int = 0, onEventClick: (News) -> Unit, onEventClickSiteName: (AppSite) -> Unit) {

    val scope = rememberCoroutineScope()
    // 2. Manage the selected tab state
    var selectedTabIndex00 by remember { mutableIntStateOf(selectedTabIndex0) }
    val pagerState = rememberPagerState(pageCount = { tabs.size }, initialPage = selectedTabIndex00)

    Column {
        // Tab Row implementation
        ScrollableTabRow(selectedTabIndex = pagerState.currentPage) {
            tabs.forEachIndexed { index, tab ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                    text = { Text(text = tab.title, maxLines = 1) }
                )
            }
        }

        // Horizontal Pager implementation (The ViewPager equivalent)
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->

            // Display the content (screen composable) for the current page/tab
            tabs[page].screen()
        }
    }

//    // Synchronize pager swipes with the TabRow indicator
//    LaunchedEffect(pagerState.currentPage) {
//        // No explicit synchronization is needed here because `selectedTabIndex`
//        // in `TabRow` is already observing `pagerState.currentPage`
//
//        // Collect from the a snapshotFlow reading the currentPage
//        snapshotFlow { pagerState.currentPage }.collect { page ->
//            // Do something with each page change, for example:
//            // viewModel.sendPageSelectedEvent(page)
//            Log.d("selectedTabIndex00", "Page changed to $page")
//        }
//    }


}
