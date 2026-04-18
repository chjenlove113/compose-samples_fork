package com.news.presentation.showHomeChild

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.news.domain.models.News
import com.news.presentation.R
import com.news.presentation.base.NewsItem
import com.news.presentation.base.ShowError
import com.news.presentation.base.ShowLoading
import com.news.presentation.base.UiState
import com.news.presentation.showHome.ShowHomeChildPagingViewModel

@Composable
fun ShowHomeChildCateRoute(
    onNewsClick: (News) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ShowHomeChildPagingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lazyPagingItems = viewModel.items.collectAsLazyPagingItems()

    ShowHomeChildCateScreen(
        uiState = uiState,
        lazyPagingItems = lazyPagingItems,
        onRetry = {
            viewModel.fetchShowHomeChildPaging(
                site_Slug = "",
                cat_Slug = viewModel.navKey.slug.slug
            )
        },
        onNewsClick = onNewsClick,
        modifier = modifier
    )
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowHomeChildCateScreen(
    uiState: UiState<PagingData<News>>,
    lazyPagingItems: LazyPagingItems<News>,
    onRetry: () -> Unit,
    onNewsClick: (News) -> Unit,
    modifier: Modifier = Modifier,
    siteSlug: String = ""
) {
    val listState = rememberLazyListState()

    // Determine if we are currently refreshing.
    // We only show the pull-to-refresh indicator if there are already items visible.
    val isRefreshing = lazyPagingItems.loadState.refresh is LoadState.Loading && lazyPagingItems.itemCount > 0

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { lazyPagingItems.refresh() },
        modifier = modifier.fillMaxSize()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            when (uiState) {
                is UiState.Loading -> {
                    // Show full-screen loading for the very first load
                    ShowLoading()
                }

                is UiState.Error -> {
                    ShowError(
                        text = stringResource(R.string.something_went_wrong),
                        retryEnabled = true,
                        retryClicked = onRetry
                    )
                }

                is UiState.Success -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize() // Ensure LazyColumn fills the space to capture gestures
                    ) {
                        items(
                            count = lazyPagingItems.itemCount,
                            key = lazyPagingItems.itemKey { it.Id }
                        ) { index ->
                            val news = lazyPagingItems[index]
                            if (news != null) {
                                NewsItem(
                                    news = news,
                                    onNewsClick = onNewsClick
                                )
                            } else {
                                Text(
                                    text = stringResource(R.string.loading),
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }

                        // Handle appending states
                        when (val appendState = lazyPagingItems.loadState.append) {
                            is LoadState.Loading -> {
                                item { ShowLoading() }
                            }
                            is LoadState.Error -> {
                                item {
                                    ErrorItem(
                                        message = appendState.error.message
                                            ?: stringResource(R.string.something_went_wrong),
                                        onRetry = { lazyPagingItems.retry() }
                                    )
                                }
                            }
                            else -> {}
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ErrorItem(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = message, style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onRetry) {
            Text(stringResource(id = R.string.retry))
        }
    }
}
