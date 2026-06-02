package com.news.presentation.newsTag

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.news.domain.models.News
import com.news.domain.models.NewsTag
import com.news.presentation.R
import com.news.presentation.base.ShowError
import com.news.presentation.base.ShowLoading
import com.news.presentation.base.UiState
import com.news.presentation.base.calculateBottomNavigationBarHeight
import com.news.presentation.components.NewsDetailScreen
import com.news.presentation.newsByTagId.NewsByTagIdScreen
import com.news.presentation.newsByTagId.NewsByTagViewModel
import kotlinx.coroutines.launch
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.Map.entry

// Define our navigation keys for list, detail, and an extra pane
@Serializable
data object ItemsList : NavKey

@Serializable
data class ItemDetail(val tagSlug: String) : NavKey

@Serializable
data class NewsDetailKey(val news: News) : NavKey

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun NewsTagRoute(
    viewModel: NewsTagViewModel = hiltViewModel()
){

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    NewsTagScreen(uiState, viewModel)
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun NewsTagScreen(
    uiState: UiState<List<NewsTag>>,
    viewModel: NewsTagViewModel?
) {
    when (uiState) {
        is UiState.Loading -> {
            ShowLoading()
        }

        is UiState.Error -> {
            ShowError(
                text = stringResource(R.string.something_went_wrong),
                retryEnabled = true
            ) { viewModel?.fetchNewsTag() }
        }

        is UiState.Success -> {
            val backStack = rememberNavBackStack(ItemsList)
            val listDetailStrategy = rememberListDetailSceneStrategy<Any>()
            
            // Derive the currently selected tag slug from the backstack source of truth
            val selectedTagSlug by remember(backStack) {
                derivedStateOf {
                    var lastTag: String? = null
                    for (navKey in backStack) {
                        if (navKey is ItemDetail) {
                            lastTag = navKey.tagSlug
                        }
                    }
                    lastTag
                }
            }
            
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Explore Tags", style = MaterialTheme.typography.titleLarge) }
                    )
                }
            ) { paddingValues ->
                NavDisplay(
                    entryDecorators = listOf(
                        rememberSaveableStateHolderNavEntryDecorator(),
                        rememberViewModelStoreNavEntryDecorator()
                    ),
                    backStack = backStack,
                    modifier = Modifier
                        .padding(paddingValues)
                        .consumeWindowInsets(WindowInsets.statusBars),
                    onBack = { 
                        backStack.removeLastOrNull() 
                    },
                    sceneStrategies = listOf(listDetailStrategy),

                    entryProvider = entryProvider {
                        entry<ItemsList>(
                            metadata = ListDetailSceneStrategy.listPane(
                                detailPlaceholder = {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            "Select a tag to see related news",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            )
                        ) {
                            NewsTagList(
                                tags = uiState.data,
                                selectedTagSlug = selectedTagSlug
                            ) { tag ->
                                val slug = tag.Slug.toString()
                                // Guard against redundant navigation if already selected
                                if (selectedTagSlug != slug) {
                                    backStack.add(ItemDetail(slug))
                                }
                            }
                        }
                        entry<ItemDetail>(
                            metadata = ListDetailSceneStrategy.detailPane()
                        ) { detailKey ->
                            val tagViewModel = hiltViewModel<NewsByTagViewModel, NewsByTagViewModel.Factory>(
                                key = detailKey.tagSlug,
                                creationCallback = { factory -> factory.create(detailKey.tagSlug) }
                            )
                            NewsByTagIdScreen(
                                tagSlug = detailKey.tagSlug,
                                viewModel = tagViewModel,
                                onBack = { backStack.removeLastOrNull() },
                                onNewsClick = { news ->
                                    backStack.add(NewsDetailKey(news))
                                }
                            )
                        }
                        entry<NewsDetailKey>(
                            metadata = ListDetailSceneStrategy.extraPane()
                        ) { detailKey ->
                            ExtraPaneScreen(
                                news = detailKey.news,
                                onBack = { backStack.removeLastOrNull() },
                                onTagClick = { tagSlug ->
                                    // Guard against redundant navigation
                                    if (selectedTagSlug != tagSlug) {
                                        backStack.add(ItemDetail(tagSlug))
                                    }
                                }
                            )
                        }
                    }
                )
            }
        }
    }
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
        onExpand = { /* Handle expand if needed */ },
        showExpandButton = false,
        onTagClick = onTagClick,
        modifier = modifier
    )
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun NewsTagList(
    tags: List<NewsTag>,
    selectedTagSlug: String?,
    onTagClick: (NewsTag) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Popular Tags",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            tags.forEach { tag ->
                val isSelected = tag.Slug.toString() == selectedTagSlug
                
                FilterChip(
                    selected = isSelected,
                    onClick = { onTagClick(tag) },
                    label = { Text(tag.Title) },
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }
    }
}
