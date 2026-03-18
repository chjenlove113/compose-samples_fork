package com.news.presentation.showHomeChild

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.news.presentation.base.ShowError
import com.news.presentation.base.ShowLoading
import com.news.presentation.base.UiState
import com.news.presentation.showHome.ShowHomeChildPagingViewModel

@Composable
fun ShowHomeChildCateRoute(
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
        modifier = modifier
    )
}

@Composable
fun ShowHomeChildCateScreen(
    uiState: UiState<PagingData<News>>,
    lazyPagingItems: LazyPagingItems<News>,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    siteSlug: String = ""
) {
    Column(modifier = modifier) {
        when (uiState) {
            is UiState.Loading -> {
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
                val listState = rememberLazyListState()

                LazyColumn(state = listState) {
                    items(
                        count = lazyPagingItems.itemCount,
                        key = lazyPagingItems.itemKey { it.Id }
                    ) { index ->
                        val news = lazyPagingItems[index]
                        if (news != null) {
                            Text(news.Title)
                        } else {
                            Text(stringResource(R.string.loading))
                        }
                    }

                    when (val appendState = lazyPagingItems.loadState.append) {
                        is LoadState.Loading -> {
                            item {
                                ShowLoading()
                            }
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
            Text("Retry")
        }
    }
}
