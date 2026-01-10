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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.Text
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
import coil3.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlin.collections.map

@Composable
fun ShowHomeRoute(
    viewModel: ShowHomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ShowHomeScreen(uiState, viewModel, {})
}

@Composable
fun ShowHomeScreen(
    uiState: UiState<ShowHomeDataModel>,
    viewModel: ShowHomeViewModel?,
    onNewsClicked: (News) -> Unit
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
            //ShowHomeContent(uiState.data, onNewsClicked)
            ExploreContent(uiState.data, {})
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
fun ExploreContent(allEventCategories: ShowHomeDataModel, onEventClick: (String) -> Unit) {
    LazyColumn {
        item {
            AutoAdvancePager(allEventCategories.LstNewsHeader ?: emptyList())
        }
        allEventCategories.CategoryViewModel.AppSiteCateByGroup?.forEach { (catId, catName, zz, yy) ->
            EventItem(catId, catName, yy!!, onEventClick)
        }
    }
}

// LazyListScope Item
fun LazyListScope.EventItem(
    catId: Int,
    catName: String,
    eventList: List<News>,
    onEventClick: (String) -> Unit
) {
    stickyHeader {
        ExploreHeader(catName)
    }
    items(eventList.size) { index ->
        Card(onClick = { onEventClick(eventList[index].Title) }) {
            Column(modifier = Modifier.padding(horizontal = 9.dp)) {
                ExploreHeader(eventList[index].Title)
                Spacer(modifier = Modifier.height(8.dp))
            }

        }
    }
}

@Composable
fun ExploreHeader(title: String) {
    Text(text = title, modifier = Modifier.padding(9.dp).clickable(){
        println("ExploreHeader: $title")
    })
}

///
@Composable
fun AutoAdvancePager(pageItems: List<News>, modifier: Modifier = Modifier) {
    Box(modifier = Modifier.fillMaxSize()) {
        val pagerState = rememberPagerState(pageCount = { pageItems.size })
        val pagerIsDragged by pagerState.interactionSource.collectIsDraggedAsState()

        val pageInteractionSource = remember { MutableInteractionSource() }
        val pageIsPressed by pageInteractionSource.collectIsPressedAsState()

        // Stop auto-advancing when pager is dragged or one of the pages is pressed
        val autoAdvance = !pagerIsDragged && !pageIsPressed

        if (autoAdvance) {
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
            Box() {
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
                                    // Handle page click
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
                                // Handle page click
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