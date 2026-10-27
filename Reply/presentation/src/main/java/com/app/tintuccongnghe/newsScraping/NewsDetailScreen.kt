package com.app.tintuccongnghe.newsScraping

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.core.layout.WindowWidthSizeClass
import com.app.tintuccongnghe.components.AdMobBannerAd

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
    val hasActiveSub by viewModel.hasActiveSubscription.collectAsStateWithLifecycle()
    val hasCloudSub by viewModel.hasCloudStorageSubscription.collectAsStateWithLifecycle()
    val isAdFree = hasActiveSub || hasCloudSub

    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    val colorScheme = MaterialTheme.colorScheme
    val isDark = isSystemInDarkTheme()

    var clickedLinkUrl by remember { mutableStateOf<String?>(null) }

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

                    val adaptiveInfo = currentWindowAdaptiveInfo()
                    val isExpandedScreen = adaptiveInfo.windowSizeClass.windowWidthSizeClass != WindowWidthSizeClass.COMPACT
                    if (isExpandedScreen) {
                        IconButton(onClick = onToggleExpand) {
                            Icon(
                                imageVector = if (isExpanded) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                                contentDescription = if (isExpanded) "Thu nhỏ" else "Phóng to"
                            )
                        }
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
            if (clickedLinkUrl != null) {
                AlertDialog(
                    onDismissRequest = { clickedLinkUrl = null },
                    title = { Text("Liên kết bài viết") },
                    text = {
                        Column {
                            Text("Bạn muốn sao chép liên kết này?", style = MaterialTheme.typography.bodyMedium)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = clickedLinkUrl!!,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 2
                            )
                        }
                    },
                    confirmButton = {
                        Button(onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Copied URL", clickedLinkUrl)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Copied link to clipboard", Toast.LENGTH_SHORT).show()
                            clickedLinkUrl = null
                        }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copy")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { clickedLinkUrl = null }) {
                            Text("Hủy")
                        }
                    }
                )
            }

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
                    val description = state.description

                    val processedHtml = remember(detail, description, colorScheme, isDark) {
                        try {
                            val baseFontSize = 18
                            context.assets.open("news_template_v0.html").bufferedReader().use { it.readText() }
                                .replace("#BG_COLOR", colorScheme.surfaceContainer.toHtmlHex().substring(1))
                                .replace("#TEXT_PRIMARY", colorScheme.onBackground.toHtmlHex().substring(1))
                                .replace("#TEXT_SECONDARY", colorScheme.onSurfaceVariant.toHtmlHex().substring(1))
                                .replace("#SURFACE", colorScheme.surfaceVariant.toHtmlHex().substring(1))
                                .replace("#PRIMARY", colorScheme.primary.toHtmlHex().substring(1))
                                .replace("#FONT_SIZE", baseFontSize.toString())
                                .replace("#TITLE", detail.title)
                                .replace("#AUTHOR", if (detail.author.isNotEmpty()) "Tác giả: ${detail.author}" else "")
                                .replace("#DATE", detail.publishDate)
                                .replace("#DESCRIPTION", description)
                                .replace("#CONTENT", detail.contentHtml)
                        } catch (e: Exception) {
                            Log.e("NewsDetailScreen", "Error loading template", e)
                            detail.contentHtml
                        }
                    }

                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        if (processedHtml.isNotEmpty()) {
                            AndroidView(
                                factory = { context ->
                                    WebView(context).apply {
                                        addJavascriptInterface(NewsWebAppInterface { url ->
                                            clickedLinkUrl = url
                                        }, "AndroidBridge")

                                        webViewClient = object : WebViewClient() {
                                            override fun shouldOverrideUrlLoading(view: WebView?, request: android.webkit.WebResourceRequest?): Boolean {
                                                val url = request?.url?.toString()
                                                if (url != null) {
                                                    if (url.contains("youtube.com") || url.contains("youtu.be")) {
                                                        return false // Allow YouTube loading
                                                    }
                                                    clickedLinkUrl = url
                                                }
                                                return true
                                            }
                                        }
                                        settings.apply {
                                            javaScriptEnabled = true
                                            domStorageEnabled = true
                                            loadWithOverviewMode = true
                                            useWideViewPort = true
                                            setLayerType(View.LAYER_TYPE_HARDWARE, null)
                                        }
                                        setBackgroundColor(0)
                                        layoutParams = ViewGroup.LayoutParams(
                                            ViewGroup.LayoutParams.MATCH_PARENT,
                                            ViewGroup.LayoutParams.MATCH_PARENT
                                        )
                                    }
                                },
                                update = { webView ->
                                    if (webView.tag != processedHtml) {
                                        webView.loadDataWithBaseURL("https://www.youtube.com", processedHtml, "text/html", "UTF-8", null)
                                        webView.tag = processedHtml
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Không có nội dung chi tiết.",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // AdMob Banner Ad (hidden if user purchased subscription)
                        if (!isAdFree) {
                            AdMobBannerAd(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

class NewsWebAppInterface(private val onLinkClick: (String) -> Unit) {
    @JavascriptInterface
    fun onLinkClicked(url: String) {
        onLinkClick(url)
    }
}

private fun Color.toHtmlHex(): String {
    return String.format("#%06X", (0xFFFFFF and this.toArgb()))
}
