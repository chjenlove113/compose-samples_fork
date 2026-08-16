package com.app.tintuccongnghe.newsByTagId

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.app.tintuccongnghe.domain.models.News
import com.app.tintuccongnghe.presentation.R
import com.app.tintuccongnghe.base.NewsItem
import com.app.tintuccongnghe.base.ShowError
import com.app.tintuccongnghe.base.ShowLoading
import com.app.tintuccongnghe.base.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsByTagIdScreen(
    tagSlug: String,
    viewModel: NewsByTagViewModel,
    onBack: () -> Unit,
    onNewsClick: (News) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isSaved by viewModel.isSaved.collectAsStateWithLifecycle()
    // 1. Create and track the scroll behavior state
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            // 2. Attach the nested scroll connection to the Scaffold modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                // 3. Pass the scroll behavior directly to your TopAppBar
                scrollBehavior = scrollBehavior,
                title = { Text(text = stringResource(R.string.tag_title_format, tagSlug), maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back_button_content_description)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleSaveTag() }) {
                        Icon(
                            imageVector = if (isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (isSaved) {
                                stringResource(R.string.unsave_tag_content_description)
                            } else {
                                stringResource(R.string.save_tag_content_description)
                            }
                        )
                    }
                }

            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.surfaceContainer)
        ) {
            when (uiState) {
                is UiState.Loading -> ShowLoading()
                is UiState.Error -> ShowError(
                    text = (uiState as UiState.Error).message,
                    retryEnabled = true,
                    retryClicked = { viewModel.fetchNewsByTag() }
                )
                is UiState.Success -> {
                    val newsList = (uiState as UiState.Success<List<News>>).data
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(newsList) { news ->
                            NewsItem(news = news, onNewsClick = onNewsClick)
                        }
                    }
                }
            }
        }
    }
}
