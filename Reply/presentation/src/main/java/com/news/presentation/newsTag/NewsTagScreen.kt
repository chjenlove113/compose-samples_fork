package com.news.presentation.newsTag

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Scaffold
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
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
                    sceneStrategies = listOf(listDetailStrategy),

                    entryProvider = entryProvider {
                        entry<ItemsList>(
                            // Metadata for the list pane, including a placeholder for the detail pane
                            metadata = ListDetailSceneStrategy.listPane(
                                detailPlaceholder = {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color.Yellow.copy(alpha = 0.4f)),
                                        verticalArrangement = Arrangement.Center,
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("Choose an Item from the List")
                                    }
                                }
                            )
                        ) {

                            NewsTagList(uiState.data, {backStack.add(ItemDetail(it))})

                        }
                        entry<ItemDetail>(
                            // Metadata for the detail pane
                            metadata = ListDetailSceneStrategy.detailPane()
                        ) { product ->
                            val id = product.id
                            ItemDetailScreen(
                                x0 = id,
                                modifier = Modifier.background(Color.Red.copy(alpha = 0.4f))
                            ) {
                                backStack.add(ExtraScreen) // Navigate to an extra pane
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
fun NewsTagList(x0: List<NewsTag>, x1: (NewsTag) -> Unit) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { innerPadding ->
        LazyColumn(contentPadding = PaddingValues(bottom = calculateBottomNavigationBarHeight())) {
            items(count = x0.size, itemContent = { NewsTagItem(x0[it], x1) })
        }
    }

}

@Composable
fun NewsTagItem(x0: NewsTag, x1: (NewsTag) -> Unit) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        modifier = Modifier
            .size(width = 240.dp, height = 100.dp)
            .clickable{
                x1.invoke(x0)
            }
    ) {
        Text(
            text = x0.Title,
            modifier = Modifier
                .padding(16.dp),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun ItemDetailScreen(
    modifier: Modifier = Modifier,
    title: String = "Detail",
    x0: NewsTag,
    goToEx: () -> Unit = {}
){
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
            Text("Item Detail Id: ${x0.Title}")
            Button(onClick = goToEx) {
                Text("Go To Extra Screen")
            }
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