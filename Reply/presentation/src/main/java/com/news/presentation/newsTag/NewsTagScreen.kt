package com.news.presentation.newsTag

import androidx.compose.foundation.background
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
import androidx.compose.material.Scaffold
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.news.domain.models.NewsTag
import com.news.presentation.R
import com.news.presentation.base.ShowError
import com.news.presentation.base.ShowLoading
import com.news.presentation.base.UiState
import com.news.presentation.base.calculateBottomNavigationBarHeight
import kotlinx.coroutines.launch
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.Map.entry

// Define our navigation keys for list, detail, and an extra pane
@Serializable
data object ItemsList : NavKey

@Serializable
data class ItemDetail(val id: NewsTag) : NavKey

@Serializable
data object ExtraScreen : NavKey

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun NewsTagRoute(
    viewModel: NewsTagViewModel = hiltViewModel()
){

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    NewsTagScreen(uiState, viewModel, {
    })
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun NewsTagScreen(
    uiState: UiState<List<NewsTag>>,
    viewModel: NewsTagViewModel?,
    onNewsTagClicked: (NewsTag) -> Unit,
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
            
            androidx.compose.material3.Scaffold(
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
                    onBack = { backStack.removeLastOrNull() },
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
                            NewsTagList(uiState.data) { tag ->
                                // For now, we'll just show the tag info, 
                                // but we could navigate to a NewsByTag screen here
                                backStack.add(ItemDetail(tag))
                            }
                        }
                        entry<ItemDetail>(
                            metadata = ListDetailSceneStrategy.detailPane()
                        ) { product ->
                            ItemDetailScreen(
                                x0 = product.id,
                                onBack = { backStack.removeLastOrNull() }
                            )
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NewsTagList(tags: List<NewsTag>, onTagClick: (NewsTag) -> Unit) {
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
                SuggestionChip(
                    onClick = { onTagClick(tag) },
                    label = { Text(tag.Title) },
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }
    }
}

@Composable
fun ItemDetailScreen(
    x0: NewsTag,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = x0.Title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Explore more news with this tag.",
            style = MaterialTheme.typography.bodyLarge
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back to Tags")
        }
    }
}

//Extra pane
@Composable
fun ExtraPaneScreen(
    modifier: Modifier = Modifier,
    title: String = "Extra Pane Screen",
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .clip(RoundedCornerShape(48.dp))
    ) {
        Text(title)
        Column(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Extra Pane Content")
        }
    }
}