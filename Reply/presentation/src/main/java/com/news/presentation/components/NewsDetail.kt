package com.news.presentation.components

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import coil3.compose.AsyncImage
import com.news.domain.models.News
import com.news.presentation.R
import com.news.presentation.base.UiState
import com.news.presentation.main.MainViewModel
import com.news.presentation.showHome.ItemDetail
import com.news.presentation.showHome.ItemsList
import com.news.utils.AppContants
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsDetailScreen(
    news: News,
    onBack: () -> Unit,
    onExpand: () -> Unit,
    showExpandButton: Boolean = true,
    onTagClick: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: NewsDetailViewModel = hiltViewModel(),
    mainViewModel: MainViewModel = hiltViewModel()
) {
    val htmlState by viewModel.htmlState.collectAsStateWithLifecycle()
    val tagsState by viewModel.tagsState.collectAsStateWithLifecycle()
    val isSaved by viewModel.isSaved.collectAsStateWithLifecycle()
    val fontScale by mainViewModel.fontScale.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Optimize M3 colors
    val colorScheme = MaterialTheme.colorScheme
    val isDark = isSystemInDarkTheme()

    // Single LaunchedEffect to handle initial data or ID changes
    LaunchedEffect(news.Id) {
        viewModel.fetchTags(news.Id)
        viewModel.fetchHtml(
            link = news.Link ?: "",
            cat = news.SubCat ?: "",
            id = news.Id,
            html = "",
            slug = news.Slug ?: ""
        )
        if (news.Html.isNullOrEmpty()) {

        }
    }

    // 1. Create and remember the scroll behavior
    var scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    //val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

    var expanded by remember { mutableStateOf(false) }
    var showFontScaleSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    val showFab by remember {
        derivedStateOf {
            scrollState.value > 0
        }
    }

    if(scrollState.value == 0){
        scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior (rememberTopAppBarState())
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = news.Source) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleSave(news) }) {
                        Icon(
                            imageVector = if (isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (isSaved) "Remove from favorites" else "Save to favorites"
                        )
                    }
                    IconButton(onClick = {
                        val shareIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, "${news.Title}\n\n${news.Link}")
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share news via"))
                    }) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = "Share")
                    }
                    IconButton(onClick = {
                        news.Link?.let { url ->
                            try {
                                val customTabsIntent = CustomTabsIntent.Builder().build()
                                customTabsIntent.launchUrl(context, Uri.parse(url))
                            } catch (e: Exception) {
                                Log.e("NewsDetail", "Error opening link", e)
                            }
                        }
                    }) {
                        Icon(imageVector = Icons.Default.Language, contentDescription = "Go to Website")
                    }
                    IconButton(onClick = { showFontScaleSheet = true }) {
                        Icon(imageVector = Icons.Default.FormatSize, contentDescription = "Change Font Scale")
                    }
                    if (showExpandButton) {
                        IconButton(onClick = onExpand) {
                            Icon(
                                imageVector = ImageVector.vectorResource(id = R.drawable.open_in_full_24px),
                                contentDescription = "Expand",
                            )
                        }
                    }
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More options")
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Option 1") },
                            onClick = { /* Do something... */ }
                        )
                        DropdownMenuItem(
                            text = { Text("Option 2") },
                            onClick = { /* Do something... */ }
                        )
                    }
                },
                // 3. Pass the behavior to the TopAppBar
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = showFab,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                FloatingActionButton(
                    onClick = {
                        coroutineScope.launch {
                            scrollState.animateScrollTo(0)
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = "Scroll to top"
                    )
                }
            }
        },
        // 2. Apply modifier to the Scaffold
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        if (showFontScaleSheet) {
            ModalBottomSheet(
                onDismissRequest = { showFontScaleSheet = false },
                sheetState = sheetState
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp, start = 24.dp, end = 24.dp, top = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Font Size",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = String.format(Locale.getDefault(), "%.1fx", fontScale),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Slider(
                        value = fontScale,
                        onValueChange = { 
                            // Snap to nearest 0.2 step
                            val snappedValue = Math.round(it * 5) / 5.0f
                            mainViewModel.setFontScale(snappedValue)
                        },
                        valueRange = 0.6f..1.4f,
                        steps = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("A", fontSize = 12.sp)
                        Text("A", fontSize = 20.sp)
                    }
                }
            }
        }
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .nestedScroll(scrollBehavior.nestedScrollConnection)
//                .verticalScroll(rememberScrollState())
                .verticalScroll(scrollState)
        ) {
            news.Image?.let { imageUrl ->
                AsyncImage(
                    model = imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp),
                    contentScale = ContentScale.Crop
                )
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = news.Title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = news.Date ?: "",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.outline
                )

                Spacer(modifier = Modifier.height(8.dp))

                when (val state = tagsState) {
                    is UiState.Success -> {
                        val data = state.data as List<*>?
                        if(data != null && data.size > 0){
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                data?.forEach { tagItem ->
                                    val tag = tagItem as? com.news.domain.models.NewsTag
                                    if (tag != null) {
                                        AssistChip(
                                            onClick = { onTagClick(tag.Slug) },
                                            label = { Text(tag.Title) }
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                    else -> {}
                }
                news.ShortDes?.let { description ->
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }


                // Handle htmlState more efficiently
                when (val state = htmlState) {
                    is UiState.Loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    is UiState.Success -> {
                        val processedHtml = remember(state.data, colorScheme, isDark, fontScale) {
                            try {
                                val baseFontSize = 18 // Base size in px
                                val scaledFontSize = (baseFontSize * fontScale).toInt()
                                
                                context.assets.open("news_template_v0.html").bufferedReader().use { it.readText() }
                                    .replace("#BG_COLOR", colorScheme.background.toHtmlHex().substring(1))
                                    .replace("#TEXT_PRIMARY", colorScheme.onBackground.toHtmlHex().substring(1))
                                    .replace("#TEXT_SECONDARY", colorScheme.onSurfaceVariant.toHtmlHex().substring(1))
                                    .replace("#SURFACE", colorScheme.surfaceVariant.toHtmlHex().substring(1))
                                    .replace("#PRIMARY", colorScheme.primary.toHtmlHex().substring(1))
                                    .replace("#FONT_SIZE", scaledFontSize.toString())
                                    .replace("#PADDING_H", "8")
                                    .replace("#PADDING_V", "8")
                                    .replace("#CONTENT", state.data)
                            } catch (e: Exception) {
                                Log.e("NewsDetail", "Error loading template", e)
                                state.data
                            }
                        }

                        AndroidView(
                            factory = { context ->
                                WebView(context).apply {
                                    webViewClient = WebViewClient()

                                    settings.apply {
                                        javaScriptEnabled = true
                                        domStorageEnabled = true
                                        loadWithOverviewMode = true
                                        useWideViewPort = true
                                        setLayerType(View.LAYER_TYPE_HARDWARE, null)
                                    }
                                    isVerticalScrollBarEnabled = false
                                    isHorizontalScrollBarEnabled = false
                                    overScrollMode = View.OVER_SCROLL_NEVER
                                    setBackgroundColor(0)

                                    isNestedScrollingEnabled = false
                                    isFocusable = false
                                    isFocusableInTouchMode = false

                                    layoutParams = ViewGroup.LayoutParams(
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                        ViewGroup.LayoutParams.WRAP_CONTENT
                                    )
                                }
                            },
                            update = { webView ->
                                if (webView.tag != processedHtml) {
                                    webView.loadDataWithBaseURL(AppContants.baseUrlUI, processedHtml, "text/html", "UTF-8", null)
                                    webView.tag = processedHtml
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    is UiState.Error -> {
                        Text(
                            text = "Error loading content: ${state.message}",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // If there's more content in news.Html or news.NewsItemChilds, display it here
                (news.NewsItemChilds as ArrayList<com.news.domain.models.NewsChildItem>?)?.forEach { child ->
                    if ((child.Title?.toString() ?: "").isNotEmpty()) {
                        Text(
                            text = child.Title?.toString() ?: "",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    if (child.MediaUrl.isNotEmpty()) {
                        AsyncImage(
                            model = child.MediaUrl,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .padding(vertical = 8.dp),
                            contentScale = ContentScale.Crop
                        )
                    }
                    if (child.Content.isNotEmpty()) {
                        Text(
                            text = child.Content,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun Color.toHtmlHex(): String {
    return String.format("#%06X", (0xFFFFFF and this.toArgb()))
}
