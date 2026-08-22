package com.app.tintuccongnghe.newsTag

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items as rowItems
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.PaneAdaptedValue
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldValue
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.tintuccongnghe.base.NewsItem
import com.app.tintuccongnghe.base.ShowError
import com.app.tintuccongnghe.base.ShowLoading
import com.app.tintuccongnghe.base.UiState
import com.app.tintuccongnghe.components.NewsDetailScreen
import com.app.tintuccongnghe.domain.models.News
import com.app.tintuccongnghe.domain.models.NewsTag
import com.app.tintuccongnghe.newsByTagId.NewsByTagIdScreen
import com.app.tintuccongnghe.newsByTagId.NewsByTagViewModel
import com.app.tintuccongnghe.presentation.R
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.launch

@Composable
fun NewsTagRoute(
    viewModel: NewsTagViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    NewsTagScreen(uiState = uiState, viewModel = viewModel)
}

@Composable
fun ExtraPaneScreen(
    news: News,
    onBack: () -> Unit,
    onTagClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    NewsDetailScreen(
        news = news,
        onBack = onBack,
        onExpand = {},
        showExpandButton = false,
        onTagClick = onTagClick,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun NewsTagScreen(
    uiState: UiState<List<NewsTag>>,
    viewModel: NewsTagViewModel?
) {
    when (uiState) {
        is UiState.Loading -> ShowLoading()

        is UiState.Error -> ShowError(
            text = uiState.message,
            retryEnabled = true,
            retryClicked = { viewModel?.fetchNewsTag() }
        )

        is UiState.Success -> {
            val tags = uiState.data
            var selectedTagSlug by rememberSaveable(tags.map(NewsTag::Slug)) {
                mutableStateOf(tags.firstOrNull()?.Slug)
            }
            val selectedTag = tags.firstOrNull {
                it.Slug == selectedTagSlug
            } ?: tags.firstOrNull()

            if (selectedTag == null) {
                EmptyTagFeed()
                return
            }

            val paneNavigator = rememberListDetailPaneScaffoldNavigator<Any>()
            val coroutineScope = rememberCoroutineScope()
            val currentDestination = paneNavigator.currentDestination
            val selectedNews = currentDestination?.contentKey
            var lastSelectedNews by remember { mutableStateOf<News?>(null) }
            var isDetailFullScreen by remember { mutableStateOf(false) }
            var lastSelectedTag by remember { mutableStateOf<String?>(selectedTag?.Slug ) }

            LaunchedEffect(currentDestination) {
                when (currentDestination?.pane) {
                    ListDetailPaneScaffoldRole.Detail -> lastSelectedNews = currentDestination.contentKey as? News
                    ListDetailPaneScaffoldRole.Extra -> lastSelectedTag = currentDestination.contentKey as? String
                    else -> {}
                }

//                if (currentDestination?.pane == ListDetailPaneScaffoldRole.Detail) {
//                    lastSelectedNews = currentDestination.contentKey as? News
//                }
//                if (currentDestination?.pane == ListDetailPaneScaffoldRole.Extra) {
//                    lastSelectedTag = currentDestination.contentKey as? String
//                }
            }

            val scaffoldValue = when {
                selectedNews == null -> ThreePaneScaffoldValue(
                    primary = PaneAdaptedValue.Hidden,
                    secondary = PaneAdaptedValue.Expanded,
                    tertiary = PaneAdaptedValue.Hidden
                )
                isDetailFullScreen -> ThreePaneScaffoldValue(
                    primary = PaneAdaptedValue.Expanded,
                    secondary = PaneAdaptedValue.Hidden,
                    tertiary = PaneAdaptedValue.Hidden
                )
                else -> paneNavigator.scaffoldValue
            }
            val newsViewModel =
                hiltViewModel<NewsByTagViewModel, NewsByTagViewModel.Factory>(
                    key = selectedTag.Slug,
                    creationCallback = { factory -> factory.create(selectedTag.Slug) }
                )
            val newsUiState by newsViewModel.uiState.collectAsStateWithLifecycle()
            val isSaved by newsViewModel.isSaved.collectAsStateWithLifecycle()

            BackHandler(
                enabled = paneNavigator.canNavigateBack() || isDetailFullScreen
            ) {
                if (isDetailFullScreen) {
                    isDetailFullScreen = false
                } else {
                    coroutineScope.launch { paneNavigator.navigateBack() }
                }
            }

            ListDetailPaneScaffold(
                directive = paneNavigator.scaffoldDirective,
                value = scaffoldValue,
                listPane = {
                    AnimatedPane {
                        NewsTagFeed(
                            tags = tags,
                            selectedTag = selectedTag,
                            newsUiState = newsUiState,
                            isSelectedTagSaved = isSaved,
                            onTagSelected = { tag ->
                                selectedTagSlug = tag.Slug
                                FirebaseCrashlytics.getInstance()
                                    .log("User selected tag: ${tag.Slug}")
                            },
                            onToggleSavedTag = newsViewModel::toggleSaveTag,
                            onRetry = newsViewModel::fetchNewsByTag,
                            selectedNewsId = (selectedNews as? News)?.Id,
                            onNewsClick = { news ->
                                coroutineScope.launch {
                                    paneNavigator.navigateTo(
                                        pane = ListDetailPaneScaffoldRole.Detail,
                                        contentKey = news
                                    )
                                }
                            }
                        )
                    }
                },
                detailPane = {
                    AnimatedPane {
                        val newsToDisplay = (selectedNews as? News) ?: lastSelectedNews
                        if (newsToDisplay != null) {
                            NewsDetailScreen(
                                news = newsToDisplay,
                                onBack = {
                                    if (isDetailFullScreen) {
                                        isDetailFullScreen = false
                                    }
                                    coroutineScope.launch { paneNavigator.navigateBack() }
                                },
                                onExpand = {
                                    isDetailFullScreen = !isDetailFullScreen
                                },
                                showExpandButton =
                                    paneNavigator.scaffoldDirective.maxHorizontalPartitions > 1,
                                onTagClick =
                                    { tagSlug ->
                                        coroutineScope.launch {
                                            paneNavigator.navigateTo(
                                                ListDetailPaneScaffoldRole.Extra,
                                                tagSlug
                                            )
                                        }
                                    }
//                                    { tagSlug ->
//                                    tags.firstOrNull { it.Slug == tagSlug }?.let { tag ->
//                                        selectedTagSlug = tag.Slug
//                                    }
//                                    isDetailFullScreen = false
//                                    coroutineScope.launch { paneNavigator.navigateBack() }
//                                }
                            )
                        }
//                        (selectedNews ?: lastSelectedNews)?.let { news ->
//                            NewsDetailScreen(
//                                news = news,
//                                onBack = {
//                                    if (isDetailFullScreen) {
//                                        isDetailFullScreen = false
//                                    }
//                                    coroutineScope.launch { paneNavigator.navigateBack() }
//                                },
//                                onExpand = {
//                                    isDetailFullScreen = !isDetailFullScreen
//                                },
//                                showExpandButton =
//                                    paneNavigator.scaffoldDirective.maxHorizontalPartitions > 1,
//                                onTagClick =
//                                    { tagSlug ->
//                                        coroutineScope.launch {
//                                            paneNavigator.navigateTo(
//                                                ListDetailPaneScaffoldRole.Extra,
//                                                tagSlug
//                                            )
//                                        }
//                                    }
////                                    { tagSlug ->
////                                    tags.firstOrNull { it.Slug == tagSlug }?.let { tag ->
////                                        selectedTagSlug = tag.Slug
////                                    }
////                                    isDetailFullScreen = false
////                                    coroutineScope.launch { paneNavigator.navigateBack() }
////                                }
//                            )
//                        }
                    }
                },
                extraPane = {
                    AnimatedPane {
                        if (selectedTagSlug != null) {
                            val tagViewModel = hiltViewModel<NewsByTagViewModel, NewsByTagViewModel.Factory>(
                                key = selectedTagSlug,
                                creationCallback = { factory -> factory.create(selectedTagSlug!!) }
                            )
                            NewsByTagIdScreen(
                                tagSlug = selectedTagSlug!!,
                                viewModel = tagViewModel,
                                onBack = {
                                    coroutineScope.launch {
                                        paneNavigator.navigateBack()
                                    }
                                },
                                onNewsClick = { news ->
                                    coroutineScope.launch {
                                        paneNavigator.navigateTo(ListDetailPaneScaffoldRole.Detail, news)
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

/**
 * Material 3 feed layout: tag filters stay in the top bar while the selected
 * tag's articles fill an adaptive, vertically scrolling pane.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsTagFeed(
    tags: List<NewsTag>,
    selectedTag: NewsTag,
    newsUiState: UiState<List<News>>,
    isSelectedTagSaved: Boolean,
    onTagSelected: (NewsTag) -> Unit,
    onToggleSavedTag: () -> Unit,
    onRetry: () -> Unit,
    selectedNewsId: Int?,
    onNewsClick: (News) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.news_tags_title)) },
                actions = {
                    IconButton(onClick = onToggleSavedTag) {
                        Icon(
                            imageVector = if (isSelectedTagSaved) {
                                Icons.Default.Favorite
                            } else {
                                Icons.Default.FavoriteBorder
                            },
                            contentDescription = stringResource(
                                if (isSelectedTagSaved) {
                                    R.string.unsave_tag_content_description
                                } else {
                                    R.string.save_tag_content_description
                                }
                            )
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            )
        }
    ) { scaffoldPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding)
        ) {
            NewsTagBar(
                tags = tags,
                selectedTagSlug = selectedTag.Slug,
                onTagSelected = onTagSelected
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                when (newsUiState) {
                    is UiState.Loading -> ShowLoading()
                    is UiState.Error -> ShowError(
                        text = newsUiState.message,
                        retryEnabled = true,
                        retryClicked = onRetry
                    )
                    is UiState.Success -> NewsFeedPane(
                        selectedTag = selectedTag,
                        news = newsUiState.data,
                        selectedNewsId = selectedNewsId,
                        onNewsClick = onNewsClick
                    )
                }
            }
        }
    }
}

@Composable
private fun NewsTagBar(
    tags: List<NewsTag>,
    selectedTagSlug: String,
    onTagSelected: (NewsTag) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        rowItems(
            items = tags,
            key = NewsTag::TagId
        ) { tag ->
            FilterChip(
                selected = tag.Slug == selectedTagSlug,
                onClick = { onTagSelected(tag) },
                label = {
                    Text(
                        text = tag.Title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            )
        }
    }
}

@Composable
private fun NewsFeedPane(
    selectedTag: NewsTag,
    news: List<News>,
    selectedNewsId: Int?,
    onNewsClick: (News) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 280.dp),
            modifier = Modifier
                .widthIn(max = 1280.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(
                start = 8.dp,
                top = 20.dp,
                end = 8.dp,
                bottom = 32.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                FeedPaneHeader(
                    selectedTag = selectedTag,
                    articleCount = news.size
                )
            }

            if (news.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    EmptyNewsCard()
                }
            } else {
                gridItems(
                    items = news,
                    key = { article -> article.Id }
                ) { article ->
                    NewsItem(
                        news = article,
                        onNewsClick = onNewsClick,
                        selected = article.Id == selectedNewsId, haveSelectedNews = true
                    )
                }
            }
        }
    }
}

@Composable
private fun FeedPaneHeader(
    selectedTag: NewsTag,
    articleCount: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = selectedTag.Title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = selectedTag.Description?.takeIf(String::isNotBlank)
                ?: stringResource(R.string.news_tag_article_count, articleCount),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun EmptyNewsCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.news_tag_empty_news_title),
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = stringResource(R.string.news_tag_empty_news_message),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EmptyTagFeed(modifier: Modifier = Modifier) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.news_tags_title)) })
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            EmptyNewsCard()
        }
    }
}
