package com.app.tintuccongnghe.showHomeFavorite

import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldDefaults
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.calculateThreePaneScaffoldValue
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.tintuccongnghe.domain.models.News
import com.app.tintuccongnghe.main.MainViewModel
import com.app.tintuccongnghe.showHome.NewsListItem
import com.app.tintuccongnghe.showHomeRSS.RssItemRow
import com.app.tintuccongnghe.showHomeRSS.ShowHomeRSSFeedDetailScreen
import com.app.tintuccongnghe.components.NewsDetailScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ShowHomeFavoriteScreen(
    onBack: () -> Unit = {},
    viewModel: ShowHomeFavoriteViewModel = hiltViewModel(),
    mainViewModel: MainViewModel = hiltViewModel(viewModelStoreOwner = LocalActivity.current as ComponentActivity)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navigator = rememberListDetailPaneScaffoldNavigator<Any>()
    val scope = rememberCoroutineScope()
    var isFullScreen by rememberSaveable { mutableStateOf(false) }

    // Handle system back button
    BackHandler(navigator.canNavigateBack() || isFullScreen) {
        if (isFullScreen) {
            isFullScreen = false
        } else {
            scope.launch {
                navigator.navigateBack()
            }
        }
    }

    // Sync Navigator state back to ViewModel
    val currentDetail = navigator.currentDestination?.contentKey as? FavoriteItem
    LaunchedEffect(currentDetail) {
        viewModel.setSelectedItem(currentDetail)
    }

    // Determine if we should show List or Detail as the single pane when maxHorizontalPartitions = 1
    val currentScaffoldDirective = if (isFullScreen) {
        navigator.scaffoldDirective.copy(maxHorizontalPartitions = 1)
    } else {
        navigator.scaffoldDirective
    }

    val scaffoldValue = calculateThreePaneScaffoldValue(
        maxHorizontalPartitions = currentScaffoldDirective.maxHorizontalPartitions,
        adaptStrategies = ListDetailPaneScaffoldDefaults.adaptStrategies(),
        currentDestination = navigator.currentDestination
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Favorites") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            if (uiState.favorites.isEmpty() && !uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No favorites yet.",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                ListDetailPaneScaffold(
                    directive = currentScaffoldDirective,
                    value = scaffoldValue,
                    listPane = {
                        AnimatedPane {
                            FavoriteListPane(
                                favorites = uiState.favorites,
                                selectedRssItem = uiState.selectedRssItem,
                                selectedWebsiteNews = uiState.selectedWebsiteNews,
                                onItemClick = { item ->
                                    scope.launch {
                                        isFullScreen = false
                                        navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, item)
                                    }
                                }
                            )
                        }
                    },
                    detailPane = {
                        AnimatedPane {
                            when (currentDetail) {
                                is FavoriteItem.LocalRss -> {
                                    ShowHomeRSSFeedDetailScreen(
                                        item = currentDetail.item,
                                        onBack = {
                                            scope.launch {
                                                isFullScreen = false
                                                navigator.navigateBack()
                                            }
                                        },
                                        isFullScreen = isFullScreen,
                                        onToggleFullScreen = {
                                            isFullScreen = !isFullScreen
                                        }
                                    )
                                }
                                is FavoriteItem.Website -> {
                                    NewsDetailScreen(
                                        news = currentDetail.news,
                                        onBack = {
                                            scope.launch {
                                                isFullScreen = false
                                                navigator.navigateBack()
                                            }
                                        },
                                        onExpand = {
                                            isFullScreen = !isFullScreen
                                        },
                                        showExpandButton = true
                                    )
                                }
                                else -> {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Text("Select an article to view details")
                                    }
                                }
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun FavoriteListPane(
    favorites: List<FavoriteItem>,
    selectedRssItem: com.app.tintuccongnghe.data.local.entities.RssItemEntity? = null,
    selectedWebsiteNews: News? = null,
    onItemClick: (FavoriteItem) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(favorites) { item ->
            when (item) {
                is FavoriteItem.LocalRss -> {
                    val isSelected = selectedRssItem?.link == item.item.link
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TypeLabel("Local RSS")
                            item.item.siteName?.let {
                                Spacer(modifier = Modifier.width(8.dp))
                                SiteLabel(it)
                            }
                        }
                        RssItemRow(
                            item = item.item,
                            isSelected = isSelected,
                            onClick = { onItemClick(item) },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
                is FavoriteItem.Website -> {
                    val isSelected = selectedWebsiteNews?.Id == item.news.Id
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TypeLabel("From Website")
                            val siteName = item.news.App_Site_Name
                            if (!siteName.isNullOrEmpty()) {
                                Spacer(modifier = Modifier.width(8.dp))
                                SiteLabel(siteName)
                            }
                        }
                        NewsListItem(
                            news = item.news,
                            selected = isSelected,
                            onClick = { onItemClick(item) },
                            bHadDetail = selectedRssItem?.link != null,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SiteLabel(text: String) {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.padding(top = 8.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

@Composable
fun TypeLabel(text: String) {
    Surface(
        color = MaterialTheme.colorScheme.tertiaryContainer,
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.padding(start = 16.dp, top = 8.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            color = MaterialTheme.colorScheme.onTertiaryContainer
        )
    }
}
