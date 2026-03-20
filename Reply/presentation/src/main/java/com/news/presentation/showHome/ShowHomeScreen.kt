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
import androidx.compose.runtime.remember
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
import com.news.presentation.components.NewsDetailScreen
import com.news.presentation.newsTag.ExtraPaneScreen
import com.news.presentation.showHomeChild.ShowHomeChildScreen
import com.news.presentation.showHomeChild.ShowHomeChildViewModel
import kotlinx.coroutines.delay
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
//                                detailPlaceholder = {
//                                    Column(
//                                        modifier = Modifier
//                                            .fillMaxSize()
//                                            .background(Color.Yellow.copy(alpha = 0.4f)),
//                                        verticalArrangement = Arrangement.Center,
//                                        horizontalAlignment = Alignment.CenterHorizontally
//                                    ) {
//                                        Text("Choose an Item from the List ZZ")
//                                    }
//                                }
                            )
                        ) {

                            ExploreContent(uiState.data, {backStack.add(ItemDetail(it))},{backStack.add(ItemDetailSite(it))},onNewsClicked)

                        }
                        entry<ItemDetail>(
                            // Metadata for the detail pane
                            metadata = ListDetailSceneStrategy.detailPane()
                        ) { product ->
                            val id = product.id
                            NewsDetailScreen(
                                modifier = Modifier.background(Color.Red.copy(alpha = 0.4f)),
                                name = id.Title, onItemClick = {}, goToEx = {
                                    backStack.add(com.news.presentation.newsTag.ExtraScreen) // Navigate to an extra pane
                                },onCloseDetail = {backStack.removeAll(backStack.filter { it != ItemsList }) }
                            )

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
    LazyColumn {
        item {
            AutoAdvancePager(allEventCategories.LstNewsHeader ?: emptyList(), onEventClickNewsItem = onEventClick)
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
    Box(modifier = Modifier.fillMaxSize()) {
        val pagerState = rememberPagerState(pageCount = { pageItems.size })
        val pagerIsDragged by pagerState.interactionSource.collectIsDraggedAsState()

        val pageInteractionSource = remember { MutableInteractionSource() }
        val pageIsPressed by pageInteractionSource.collectIsPressedAsState()

        // Stop auto-advancing when pager is dragged or one of the pages is pressed
        val autoAdvance = !pagerIsDragged && !pageIsPressed

        if (autoAdvance && pageItems.isNotEmpty()) {
            LaunchedEffect(pagerState, pageInteractionSource) {
                while (true) {
                    delay(2000)
                    val nextPage = (pagerState.currentPage + 1) % pageItems.size
                    pagerState.animateScrollToPage(nextPage)
                }
            }
        }

        HorizontalPager(
            state = pagerState
        ) { page ->
            Box(Modifier.clickable{
                onEventClickNewsItem(pageItems[page])
            }) {
                AsyncImage(
                    model = pageItems[page].Image,
                    contentDescription = pageItems[page].Title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .height(300.dp)
                        .padding(8.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .height(180.dp)
                        .padding(8.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    MaterialTheme.colorScheme.primary
                                )
                            )
                        )
                        .align(Alignment.BottomStart)
                ) {

                }

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {

                    Box(
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Text(
                            text = pageItems[page].Source,
                            fontSize = MaterialTheme.typography.titleSmall.fontSize,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = modifier
                                .clickable(
                                    interactionSource = pageInteractionSource,
                                    indication = LocalIndication.current
                                ) {
                                    println(pageItems[page].Source)
                                }
                        )
                    }

//                    Text(
//                        text = pageItems[page].ShortDes,
//                        fontSize = MaterialTheme.typography.titleSmall.fontSize,
//                        maxLines = 1,
//                        overflow = TextOverflow.Ellipsis,
//                    )
                    Text(
                        text = pageItems[page].Title,
                        fontSize = MaterialTheme.typography.titleLarge.fontSize,
                        color = MaterialTheme.colorScheme.onPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = modifier
                            .fillMaxSize()
                            .clickable(
                                interactionSource = pageInteractionSource,
                                indication = LocalIndication.current
                            ) {
                                onEventClickNewsItem(pageItems[page])
                            }
                            .wrapContentSize(align = Alignment.Center)
                    )



                }


            }

        }

        PagerIndicator(pageItems.size, pagerState.currentPage)
    }
}

@Composable
fun PagerIndicator(pageCount: Int, currentPageIndex: Int, modifier: Modifier = Modifier) {
    Box(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .wrapContentHeight()
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.End
        ) {
            repeat(pageCount) { iteration ->
                val color = if (currentPageIndex == iteration) Color.DarkGray else Color.LightGray
                Box(
                    modifier = modifier
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(color)
                        .size(8.dp)
                )
            }
        }
    }
}
