package com.app.tintuccongnghe.showHomeChild

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.paging.compose.collectAsLazyPagingItems
import com.app.tintuccongnghe.domain.models.AppSite
import com.app.tintuccongnghe.domain.models.AppSiteCateByGroup
import com.app.tintuccongnghe.domain.models.News
import com.app.tintuccongnghe.domain.models.ShowHomeDataModel
import com.app.tintuccongnghe.presentation.R
import com.app.tintuccongnghe.base.ShowError
import com.app.tintuccongnghe.base.ShowLoading
import com.app.tintuccongnghe.base.UiState
import com.app.tintuccongnghe.components.NewsDetailScreen
import com.app.tintuccongnghe.showHome.AutoAdvancePager
import com.app.tintuccongnghe.showHome.ItemDetail
import com.app.tintuccongnghe.showHome.ItemDetailSite
import com.app.tintuccongnghe.showHome.ItemsList
import com.app.tintuccongnghe.showHome.ShowHomeChildPagingViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun ShowHomeChildRoute(
    viewModel: ShowHomeChildViewModel = hiltViewModel(),
    onNewsClicked: ((News) -> Unit)? = null,
    onTabSelected: (String) -> Unit = {},
    selectedNews: News? = null
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (onNewsClicked != null) {
        ShowHomeChildScreen(uiState, viewModel, onNewsClicked, onTabSelected, selectedNews)
    } else {
        // Internal navigation for screen3 when called directly from MainActivity
        val backStack = rememberNavBackStack(ItemsList)
        val listDetailStrategy = rememberListDetailSceneStrategy<Any>()
        val adaptiveInfo = currentWindowAdaptiveInfo()
        val directive = calculatePaneScaffoldDirective(adaptiveInfo)
        //backup our back stack
        val backStackBackup = rememberNavBackStack()
        NavDisplay(
            backStack = backStack,
            sceneStrategies = listOf(listDetailStrategy),
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator()
            ),
            onBack = { backStack.removeLastOrNull() },
            entryProvider = entryProvider {
                entry<ItemsList>(metadata = ListDetailSceneStrategy.listPane()) {
                    ShowHomeChildScreen(
                        uiState = uiState,
                        viewModel = viewModel,
                        onNewsClicked = { news ->
                            backStack.add(ItemDetail(news))
                        }
                    )
                }
                entry<ItemDetail>(metadata = ListDetailSceneStrategy.detailPane()) { product ->
                    NewsDetailScreen(
                        news = product.id,
                        onBack = {
                            backStackBackup.filter { it is ItemsList }.forEach {
                                backStack.add(it)
                            }
                            backStackBackup.isEmpty()
                            backStack.removeAll { it is ItemDetail }
                        },
                        onExpand = {
                            backStack.filter { it is ItemsList }.forEach {
                                backStackBackup.add(it)
                            }
                            backStack.removeAll { it is ItemsList }
                        },
                        showExpandButton = directive.maxHorizontalPartitions > 1
                    )
                }
            }
        )
    }
}

@Composable
fun ShowHomeChildScreen(
    uiState: UiState<ShowHomeDataModel>,
    viewModel: ShowHomeChildViewModel?,
    onNewsClicked: (News) -> Unit,
    onTabSelected: (String) -> Unit = {},
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
            Column(modifier = Modifier.fillMaxSize()) {
                ExploreContentChild(
                    allEventCategories = uiState.data,
                    onNewsClick = onNewsClicked,
                    appSite = viewModel?.navKey?.slug?.slug ?: "",
                    onTabSelected = onTabSelected,
                    selectedNews = selectedNews
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ExploreContentChild(
    allEventCategories: ShowHomeDataModel,
    onNewsClick: (News) -> Unit,
    appSite: String,
    onTabSelected: (String) -> Unit = {},
    selectedNews: News? = null
) {
    val itemsListHeader = allEventCategories.CategoryViewModel.LstNewsHeader
    val appSiteCateByGroup = allEventCategories.CategoryViewModel.AppSiteCateByGroup

    LazyColumn(modifier = Modifier.fillMaxSize()) {
//        item {
//            AutoAdvancePager(
//                pageItems = itemsListHeader ?: emptyList(),
//                onEventClickNewsItem = onNewsClick,
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(250.dp)
//            )
//        }

        item {
            Box(modifier = Modifier.fillParentMaxSize()) {
                DynamicTabLayoutScreen(
                    appSiteCateByGroup = appSiteCateByGroup,
                    appSite = appSite,
                    onNewsClick = onNewsClick,
                    onTabSelected = onTabSelected,
                    itemsListHeader = itemsListHeader,
                    selectedNews = selectedNews
                )
            }
        }
    }
}

@Composable
fun DynamicTabLayoutScreen(
    appSiteCateByGroup: ArrayList<AppSiteCateByGroup>?,
    appSite: String,
    onNewsClick: (News) -> Unit,
    onTabSelected: (String) -> Unit = {},
    itemsListHeader: List<News>?,
    selectedNews: News? = null
) {
    val tabDefinitions = remember(appSiteCateByGroup) {
        val list = mutableListOf<Triple<String, String, String>>() // Name, Slug, Key
        if ((appSiteCateByGroup?.size ?: 0) > 1) {
            list.add(Triple("All_" + appSite, "", ""))
            //list.add(Triple("All", "", ""))
        }
        // neu appSite là All thì không cần load cate
        if(appSite != ""){
            appSiteCateByGroup?.forEach {
                list.add(Triple(it.Name, it.Slug, it.Key))
            }
        }

        list
    }

    if (tabDefinitions.isEmpty()) return

    val showTabs = tabDefinitions.size > 1

    if (showTabs) {

        var selectedTabKey by rememberSaveable { mutableStateOf("") }

        val initialPageIndex = remember(tabDefinitions) {
            val index = tabDefinitions.indexOfFirst { it.second == selectedTabKey }
            if (index >= 0) index else 0
        }

        val pagerState = rememberPagerState(pageCount = { tabDefinitions.size }, initialPage = initialPageIndex)
        val scope = rememberCoroutineScope()

        Column(modifier = Modifier.fillMaxSize()) {
            ScrollableTabRow(selectedTabIndex = pagerState.currentPage) {
                tabDefinitions.forEachIndexed { index, tab ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        text = { Text(text = tab.first.split("_")[0], maxLines = 1) }
                    )
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
                key = { index ->
                    val tabKey = tabDefinitions.getOrNull(index)?.second
                    if (tabKey.isNullOrEmpty()) "tab_$index" else tabKey
                }
            ) { page ->
                val tabDef = tabDefinitions[page]
                ShowHomeChildTabPage(
                    tabName = tabDef.first,
                    tabSlug = tabDef.second,
                    tabKey = tabDef.third,
                    parentAppSite = appSite,
                    onNewsClick = onNewsClick,
                    onTabSelected = onTabSelected,
                    itemsListHeader = itemsListHeader,
                    selectedNews = selectedNews, showAppCategory = true, onTabSelectedCategory = {
                        key -> selectedTabKey = key
                    }
                )
            }
        }
        // Synchronize external changes to selectedTabKey00 with pager
        LaunchedEffect(selectedTabKey) {
            val targetIndex = tabDefinitions.indexOfFirst { it.second == selectedTabKey }
            if (targetIndex >= 0 && pagerState.currentPage != targetIndex) {
                pagerState.animateScrollToPage(targetIndex)
            }
        }
        // Synchronize pager swipes back to selectedTabKey00
        LaunchedEffect(pagerState.currentPage) {
            val currentKey = tabDefinitions.getOrNull(pagerState.currentPage)?.second
            if (currentKey != null) {
                selectedTabKey = currentKey
            }
        }

    } else {
        val tabDef = tabDefinitions[0]
        ShowHomeChildTabPage(
            tabName = tabDef.first,
            tabSlug = tabDef.second,
            tabKey = tabDef.third,
            parentAppSite = appSite,
            onNewsClick = onNewsClick,
            onTabSelected = onTabSelected,
            itemsListHeader = itemsListHeader,
            selectedNews = selectedNews, false,{}
        )
    }
}

@Composable
fun ShowHomeChildTabPage(
    tabName: String,
    tabSlug: String,
    tabKey: String,
    parentAppSite: String,
    onNewsClick: (News) -> Unit,
    onTabSelected: (String) -> Unit = {},
    itemsListHeader: List<News>?,
    selectedNews: News? = null,
    showAppCategory: Boolean = false,
    onTabSelectedCategory: (String) -> Unit = {},
) {
    val appSiteModel = remember(tabSlug, tabKey, parentAppSite, tabName) {
        ItemDetailSite(
            AppSite(0, tabSlug, parentAppSite, tabName, "", "", "", "")
        )
    }

    val viewModel = hiltViewModel<ShowHomeChildPagingViewModel, ShowHomeChildPagingViewModel.Factory>(
        key = appSiteModel.slug.slug + "_" + appSiteModel.slug.Name,
        creationCallback = { factory -> factory.create(appSiteModel) }
    )

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lazyPagingItems = viewModel.items.collectAsLazyPagingItems()

    ShowHomeChildCateScreen(
        lazyPagingItems = lazyPagingItems,
        uiState = uiState,
        onRetry = { viewModel.fetchShowHomeChildPaging(1, parentAppSite, tabSlug) },
        onNewsClick = onNewsClick,
        onTabSelected = onTabSelected,
        siteSlug = tabSlug, 
        itemsListHeader = itemsListHeader,
        selectedNews = selectedNews, showAppCategory = showAppCategory, onTabSelectedCategory = onTabSelectedCategory
    )
}
