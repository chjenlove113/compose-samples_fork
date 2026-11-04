package com.app.tintuccongnghe.showHomeRSS

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloseFullscreen
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.window.core.layout.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.tintuccongnghe.data.local.entities.RssItemEntity
import com.app.tintuccongnghe.main.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun ShowHomeRSSFeedDetailScreen(
    item: RssItemEntity,
    onBack: () -> Unit,
    isFullScreen: Boolean = false,
    onToggleFullScreen: () -> Unit = {},
    mainViewModel: MainViewModel = hiltViewModel(viewModelStoreOwner = LocalContext.current as ComponentActivity),
    rssViewModel: ShowHomeRssViewModel = hiltViewModel(viewModelStoreOwner = LocalContext.current as ComponentActivity)
) {
    val context = LocalContext.current
    val adaptiveInfo = currentWindowAdaptiveInfo()
    val isCompact = adaptiveInfo.windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.COMPACT
    val contentPadding = if (isCompact) 8.dp else 16.dp
    
    val fontScale by mainViewModel.fontScale.collectAsStateWithLifecycle()
    val rssUiState by rssViewModel.uiState.collectAsStateWithLifecycle()
    val colorScheme = MaterialTheme.colorScheme
    
    // Use the item from the ViewModel if it matches, to get live updates (like isFavorite)
    val currentItem = remember(rssUiState.selectedItem, item) {
        if (rssUiState.selectedItem?.link == item.link) {
            rssUiState.selectedItem!!
        } else {
            item
        }
    }

    val dateStr = remember(currentItem.pubDate) {
        currentItem.pubDate?.let {
            SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault()).format(Date(it))
        } ?: ""
    }

    var showFontScaleSheet by remember { mutableStateOf(false) }
    var clickedLinkUrl by remember { mutableStateOf<String?>(null) }
    val sheetState = rememberModalBottomSheetState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Article Detail", style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { rssViewModel.toggleFavorite(currentItem) }) {
                        Icon(
                            imageVector = if (currentItem.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (currentItem.isFavorite) "Remove from favorites" else "Add to favorites",
                            tint = if (currentItem.isFavorite) Color.Red else LocalContentColor.current
                        )
                    }
                    IconButton(onClick = {
                        val shareIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, "${currentItem.title}\n\n${currentItem.link}")
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share news via"))
                    }) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = "Share")
                    }
                    IconButton(onClick = { showFontScaleSheet = true }) {
                        Icon(imageVector = Icons.Default.FormatSize, contentDescription = "Change Font Scale")
                    }
                    if (!isCompact) {
                        IconButton(onClick = onToggleFullScreen) {
                            Icon(
                                if (isFullScreen) Icons.Default.CloseFullscreen else Icons.Default.OpenInFull,
                                contentDescription = if (isFullScreen) "Exit Full Screen" else "Expand to Full Screen"
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        if (showFontScaleSheet) {
            ModalBottomSheet(
                onDismissRequest = { showFontScaleSheet = false },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
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

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize().background(MaterialTheme.colorScheme.surfaceContainer)
                .verticalScroll(rememberScrollState())
                .padding(contentPadding)
        ) {
            Text(
                text = currentItem.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            if (dateStr.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            val content = currentItem.content
            val description = currentItem.description
            if (content != null) {
                HtmlContent(
                    html = content,
                    colorScheme = colorScheme,
                    fontScale = fontScale,
                    onLinkClick = { url ->
                        clickedLinkUrl = url
                    }
                )
            } else if (description != null) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("Article Link", currentItem.link)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, "Copied link to clipboard", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Copy Link Article")
            }
        }
    }
}

class WebAppInterface(private val onLinkClick: (String) -> Unit) {
    @JavascriptInterface
    fun onLinkClicked(url: String) {
        onLinkClick(url)
    }
}

@Composable
fun HtmlContent(
    html: String,
    colorScheme: ColorScheme,
    fontScale: Float,
    onLinkClick: (String) -> Unit
) {
    AndroidView<WebView>(
        modifier = Modifier.fillMaxWidth().heightIn(min = 300.dp),
        factory = { context ->
            WebView(context).apply {
                addJavascriptInterface(WebAppInterface(onLinkClick), "AndroidBridge")
                webChromeClient = WebChromeClient()
                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(view: WebView?, request: android.webkit.WebResourceRequest?): Boolean {
                        val url = request?.url?.toString()
                        if (url != null) {
                            if (url.contains("youtube.com") || url.contains("youtu.be")) {
                                return false // Allow YouTube loading
                            }
                            onLinkClick(url)
                        }
                        return true
                    }
                }
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    useWideViewPort = true
                    loadWithOverviewMode = true
                    mediaPlaybackRequiresUserGesture = false
                    mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                    userAgentString = "Mozilla/5.0 (Linux; Android 10; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
                }
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
            }
        },
        update = { webView ->
            val surface = colorScheme.surfaceContainer.toHtmlHex()
            val onSurface = colorScheme.onSurface.toHtmlHex()
            val primary = colorScheme.primary.toHtmlHex()
            val outline = colorScheme.outline.toHtmlHex()
            val baseFontSize = 16 * fontScale

            val processedHtml = html.replace("<iframe ", "<iframe allow=\"accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture\" ")

            val styledHtml = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <style>
                        :root {
                            --md-surface: $surface;
                            --md-on-surface: $onSurface;
                            --md-primary: $primary;
                            --md-outline: $outline;
                            --md-font-size: ${baseFontSize}px;
                        }
                        body {
                            background-color: var(--md-surface);
                            color: var(--md-on-surface);
                            font-family: 'Roboto', -apple-system, sans-serif;
                            font-size: var(--md-font-size);
                            line-height: 1.6;
                            margin: 0;
                            padding: 0;
                            word-wrap: break-word;
                        }
                        h1, h2, h3, h4, h5, h6 {
                            color: var(--md-primary);
                            margin-top: 1.5em;
                            margin-bottom: 0.5em;
                            font-weight: 600;
                        }
                        p { margin-bottom: 1em; }
                        a {
                            color: var(--md-primary);
                            text-decoration: none;
                            border-bottom: 1px solid var(--md-outline);
                        }
                        video, .wp-video {
                            max-width: 100% !important;
                            width: 100% !important;
                            height: auto !important;
                            border-radius: 12px;
                            margin: 16px 0;
                        }
                        iframe {
                            max-width: 100% !important;
                            width: 100% !important;
                            aspect-ratio: 16 / 9;
                            border-radius: 12px;
                            margin: 16px 0;
                        }
                        img {
                            max-width: 100%;
                            height: auto;
                            border-radius: 16px;
                            margin: 16px 0;
                            display: block;
                        }
                        blockquote {
                            margin: 16px 0;
                            padding-left: 16px;
                            border-left: 4px solid var(--md-primary);
                            font-style: italic;
                            color: var(--md-outline);
                        }
                        code {
                            background-color: var(--md-outline);
                            padding: 2px 4px;
                            border-radius: 4px;
                            font-size: 0.9em;
                        }
                        pre {
                            video, .wp-video {
                                max-width: 100% !important;
                                width: 100% !important;
                                height: auto !important;
                            }
                            background-color: var(--md-outline);
                            padding: 12px;
                            border-radius: 8px;
                            overflow-x: auto;
                        }
                        ul, ol { padding-left: 24px; margin-bottom: 16px; }
                        li { margin-bottom: 8px; }
                    </style>
                </head>
                <body>
                    $processedHtml
                    <script>
                        document.addEventListener('click', function(e) {
                            var target = e.target.closest('a');
                            if (target && target.href) {
                                if (target.href.includes('youtube.com') || target.href.includes('youtu.be')) {
                                    return; // allow youtube links
                                }
                                e.preventDefault();
                                AndroidBridge.onLinkClicked(target.href);
                            }
                        }, false);

                        document.addEventListener('DOMContentLoaded', () => {
                            const images = document.querySelectorAll('img');
                            images.forEach(img => {
                                img.style.opacity = '0';
                                img.style.transition = 'opacity 0.5s ease-in-out';
                                img.onload = () => img.style.opacity = '1';
                            });
                        });
                    </script>
                </body>
                </html>
            """.trimIndent()
            webView.loadDataWithBaseURL("https://www.youtube.com", styledHtml, "text/html", "utf-8", null)
        }
    )
}

fun Color.toHtmlHex(): String {
    return String.format("#%06X", (0xFFFFFF and this.toArgb()))
}
