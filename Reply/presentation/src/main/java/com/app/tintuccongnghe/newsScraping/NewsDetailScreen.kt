package com.app.tintuccongnghe.newsScraping

import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsDetailScreen(
    websiteId: String,
    link: String,
    isExpanded: Boolean = false,
    onToggleExpand: () -> Unit = {},
    onBack: () -> Unit,
    viewModel: NewsDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    val colorScheme = MaterialTheme.colorScheme
    val isDark = isSystemInDarkTheme()

    LaunchedEffect(websiteId, link) {
        viewModel.loadDetail(websiteId, link)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chi tiết bài viết") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(link))
                            Toast.makeText(context, "Đã sao chép liên kết vào bộ nhớ tạm", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy Link"
                        )
                    }

                    IconButton(onClick = onToggleExpand) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                            contentDescription = if (isExpanded) "Thu nhỏ" else "Phóng to"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is NewsDetailUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is NewsDetailUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(text = "Lỗi: ${state.message}", color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadDetail(websiteId, link) }) {
                            Text("Thử lại")
                        }
                    }
                }
                is NewsDetailUiState.Success -> {
                    val detail = state.detail
                    val processedHtml = remember(detail.contentHtml, colorScheme, isDark) {
                        try {
                            val baseFontSize = 18
                            context.assets.open("news_template_v0.html").bufferedReader().use { it.readText() }
                                .replace("#BG_COLOR", colorScheme.surfaceContainer.toHtmlHex().substring(1))
                                .replace("#TEXT_PRIMARY", colorScheme.onBackground.toHtmlHex().substring(1))
                                .replace("#TEXT_SECONDARY", colorScheme.onSurfaceVariant.toHtmlHex().substring(1))
                                .replace("#SURFACE", colorScheme.surfaceVariant.toHtmlHex().substring(1))
                                .replace("#PRIMARY", colorScheme.primary.toHtmlHex().substring(1))
                                .replace("#FONT_SIZE", baseFontSize.toString())
                                .replace("#PADDING_H", "0")
                                .replace("#PADDING_V", "0")
                                .replace("#CONTENT", detail.contentHtml)
                        } catch (e: Exception) {
                            Log.e("NewsDetailScreen", "Error loading template", e)
                            detail.contentHtml
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        Text(
                            text = detail.title,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            if (detail.author.isNotEmpty()) {
                                Text(
                                    text = "Tác giả: ${detail.author}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            if (detail.publishDate.isNotEmpty()) {
                                Text(
                                    text = detail.publishDate,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(16.dp))

                        if (processedHtml.isNotEmpty()) {
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
                                        webView.loadDataWithBaseURL(null, processedHtml, "text/html", "UTF-8", null)
                                        webView.tag = processedHtml
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 400.dp, max = 3000.dp)
                            )
                        } else {
                            Text(
                                text = "Không có nội dung chi tiết.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun Color.toHtmlHex(): String {
    return String.format("#%06X", (0xFFFFFF and this.toArgb()))
}
