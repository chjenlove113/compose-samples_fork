package com.news.presentation.showHomeChild

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
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
import com.news.domain.models.News
import com.news.presentation.R
import com.news.presentation.base.ShowError
import com.news.presentation.base.ShowLoading
import com.news.presentation.base.UiState
import com.news.presentation.showHome.ShowHomeChildPagingViewModel

@Composable
fun ShowHomeChildCateRoute(
    viewModel: ShowHomeChildPagingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ShowHomeChildCateScreen(uiState, viewModel, {}, "")
}

@Composable
fun ShowHomeChildCateScreen(
    uiState: UiState<PagingData<News>>,
    viewModel: ShowHomeChildPagingViewModel,
    x2: () -> Unit,
    site_Slug: String
) {
    when (uiState) {
        is UiState.Loading -> {
            ShowLoading()
        }

        is UiState.Error -> {
            ShowError(
                text = stringResource(R.string.something_went_wrong),
                retryEnabled = true
            ) {
                viewModel?.fetchShowHomeChildPaging(
                    1,
                    site_Slug,
                    viewModel?.navKey?.slug?.slug ?: ""
                )
            }
        }

        is UiState.Success -> {
//            Column() {
//                Text("site_Slug: $site_Slug, - KEY: ${viewModel?.navKey?.slug}")
//            }
            val lazyPagingItems = viewModel.items.collectAsLazyPagingItems()
            val listState = rememberLazyListState()

            LazyColumn() {
                pagingItems(lazyPagingItems, key = { "news_id_${it.Id}" }) { ite ->
                    if (ite != null) {
                        Text(ite.Title)
                    }else{
                        Text("null")
                    }
                }

                when (val appendState = lazyPagingItems.loadState.append) {
                    is androidx.paging.LoadState.Loading -> {
                        item {
                            ShowLoading()
                        }
                    }
                    is LoadState.Error ->{
                        item {
                            ErrorItem(
                                message = appendState.error.message ?: "Unknown error",
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

fun <T : Any> LazyListScope.pagingItems(
    lazyPagingItems: LazyPagingItems<T>,
    key: ((item: T) -> Any)? = null,
    itemContent: @Composable (T?) -> Unit
) {
    items(
        count = lazyPagingItems.itemCount,
        key = { index ->
            lazyPagingItems[index]?.let { key?.invoke(it) } ?: "placeholder_index_$index"
        }
    ) { index ->
        itemContent(lazyPagingItems[index])
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

