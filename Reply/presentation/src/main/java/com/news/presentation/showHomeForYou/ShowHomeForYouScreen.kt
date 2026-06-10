package com.news.presentation.showHomeForYou

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
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.PaneAdaptedValue
import androidx.compose.material3.adaptive.layout.PaneScaffoldDirective
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldValue
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.news.presentation.newsByTagId.NewsByTagIdScreen
import com.news.presentation.newsByTagId.NewsByTagViewModel
import com.news.presentation.newsTag.ExtraPaneScreen
import com.news.presentation.showHomeChild.ShowHomeChildScreen
import com.news.presentation.showHomeChild.ShowHomeChildViewModel
import com.news.presentation.showHome.ShowHomeRoute
import com.news.presentation.showHome.ItemDetailSite
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlin.collections.forEach
import kotlin.collections.listOf
import kotlin.collections.map


// Define our navigation keys for list, detail, and an extra pane
@Serializable
data object ItemsListForYou : NavKey

@Serializable
data class ItemDetailForYou(val id: News) : NavKey

@Serializable
data class ItemDetailSiteForYou(val slug: AppSite) : NavKey

@Serializable
data object ExtraScreenForYou : NavKey

@Composable
fun ShowHomeForYouRoute(
    viewModel: ShowHomeForYouViewModel = hiltViewModel(),
    onNavigateToLogin: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ShowHomeForYouScreen(uiState, viewModel, onNavigateToLogin = onNavigateToLogin)
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun ShowHomeForYouScreen(
    uiState: ShowHomeForYouUiState,
    viewModel: ShowHomeForYouViewModel?,
    onTabSelected: (String) -> Unit = {},
    onNavigateToLogin: () -> Unit = {}
) {
    if (!uiState.isLoggedIn) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "You must be logged in to see personalized content.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
                Button(onClick = onNavigateToLogin) {
                    Text("Go to Login")
                }
            }
        }
        return
    }

    when (val data = uiState.data) {
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
            // 1. Set up the navigator
            val navigator = rememberListDetailPaneScaffoldNavigator<Any>()

            // 2. Manual toggle state for the detail full-screen mode
            var isDetailFullScreen by remember { mutableStateOf(false) }

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


            ListDetailPaneScaffold(
                directive = navigator.scaffoldDirective,
                value = manualValue,
                listPane = {
                    AnimatedPane{
                        var selectedTabKey00 by rememberSaveable { mutableStateOf("home") }

                        val tabs = remember(data.data) {
                            val tabsItem = arrayListOf<TabItem>()
                            tabsItem.add(TabItem(key = "home", title = "Home", screen = {
                                ShowHomeRoute(
                                    onNewsClicked = { news ->
                                        scope.launch {
                                            navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, news)
                                        }
                                    },
                                    onTabSelected = { key ->
                                        selectedTabKey00 = key
                                    }
                                )
                            }))

                            tabsItem.add(
                                TabItem(key = "all", title = "All", screen = {
                                    val appSiteAll = ItemDetailSite(
                                        AppSite(0, "", "", "All", "", "", "", "")
                                    )
                                    val childViewModel = hiltViewModel<ShowHomeChildViewModel, ShowHomeChildViewModel.Factory>(
                                        key = "all",
                                        creationCallback = { factory -> factory.create(appSiteAll) }
                                    )
                                    val childUiState by childViewModel.uiState.collectAsStateWithLifecycle()
                                    ShowHomeChildScreen(
                                        viewModel = childViewModel,
                                        uiState = childUiState,
                                        onNewsClicked = { news ->
                                            scope.launch {
                                                navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, news)

                                            }
                                        },
                                        onTabSelected = { key ->
                                            selectedTabKey00 = key
                                        }
                                    )
                                })
                            )

                            data.data.CategoryViewModel.AppSiteCateByGroup?.forEach {
                                val appSite = ItemDetailSite(
                                    AppSite(it.Id, it.Slug, it.Key, it.Name, "", "", "", "")
                                )

                                tabsItem.add(
                                    TabItem(key = it.Slug, title = it.Name, screen = {
                                        val childViewModel = hiltViewModel<ShowHomeChildViewModel, ShowHomeChildViewModel.Factory>(
                                            key = it.Slug,
                                            creationCallback = { factory -> factory.create(appSite) }
                                        )
                                        val childUiState by childViewModel.uiState.collectAsStateWithLifecycle()
                                        ShowHomeChildScreen(
                                            viewModel = childViewModel,
                                            uiState = childUiState,
                                            onNewsClicked = { news ->
                                                scope.launch {
                                                    navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, news)

                                                }
                                            },
                                            onTabSelected = { key ->
                                                selectedTabKey00 = key
                                            }
                                        )
                                    })
                                )
                            }
                            tabsItem
                        }

                        val scope = rememberCoroutineScope()
                        val initialPageIndex = remember(tabs) {
                            val index = tabs.indexOfFirst { it.key == selectedTabKey00 }
                            if (index >= 0) index else 0
                        }
                        val pagerState = rememberPagerState(pageCount = { tabs.size }, initialPage = initialPageIndex)

                        Column {
                            // Tab Row implementation - Use pagerState.currentPage directly to avoid feedback loops
                            ScrollableTabRow(selectedTabIndex = pagerState.currentPage) {
                                tabs.forEachIndexed { index, tab ->
                                    Tab(
                                        selected = pagerState.currentPage == index,
                                        onClick = {
                                            scope.launch { pagerState.animateScrollToPage(index) }
                                        },
                                        text = { Text(text = tab.title, maxLines = 1) }
                                    )
                                }
                            }

                            // Horizontal Pager implementation
                            HorizontalPager(
                                state = pagerState,
                                modifier = Modifier.fillMaxSize()
                            ) { page ->
                                tabs[page].screen()
                            }
                        }

                        // Synchronize external changes to selectedTabKey00 with pager
                        LaunchedEffect(selectedTabKey00) {
                            val targetIndex = tabs.indexOfFirst { it.key == selectedTabKey00 }
                            if (targetIndex >= 0 && pagerState.currentPage != targetIndex) {
                                pagerState.animateScrollToPage(targetIndex)
                            }
                        }

                        // Synchronize pager swipes back to selectedTabKey00
                        LaunchedEffect(pagerState.currentPage) {
                            val currentKey = tabs.getOrNull(pagerState.currentPage)?.key
                            if (currentKey != null) {
                                selectedTabKey00 = currentKey
                            }
                        }
                    }

                }, detailPane = {
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
