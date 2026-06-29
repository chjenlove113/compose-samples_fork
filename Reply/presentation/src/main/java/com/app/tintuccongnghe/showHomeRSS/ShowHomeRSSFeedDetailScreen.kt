package com.app.tintuccongnghe.showHomeRSS

import android.content.Intent
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloseFullscreen
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.tintuccongnghe.data.local.entities.RssItemEntity
import com.app.tintuccongnghe.main.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowHomeRSSFeedDetailScreen(
    item: RssItemEntity,
    onBack: () -> Unit,
    isFullScreen: Boolean = false,
    onToggleFullScreen: () -> Unit = {},
    mainViewModel: MainViewModel = hiltViewModel(viewModelStoreOwner = LocalContext.current as ComponentActivity)
) {
    val context = LocalContext.current
    val fontScale by mainViewModel.fontScale.collectAsStateWithLifecycle()
    val colorScheme = MaterialTheme.colorScheme
    
    val dateStr = remember(item.pubDate) {
        item.pubDate?.let {
            SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault()).format(Date(it))
        } ?: ""
    }

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
                    IconButton(onClick = onToggleFullScreen) {
                        Icon(
                            if (isFullScreen) Icons.Default.CloseFullscreen else Icons.Default.OpenInFull,
                            contentDescription = if (isFullScreen) "Exit Full Screen" else "Expand to Full Screen"
                        )
                    }
                    IconButton(onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, item.link.toUri())
                        context.startActivity(intent)
                    }) {
                        Icon(Icons.Default.OpenInBrowser, contentDescription = "Open in Browser")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = item.title,
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

            val content = item.content
            val description = item.description
            if (content != null) {
                HtmlContent(
                    html = content,
                    colorScheme = colorScheme,
                    fontScale = fontScale
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
                    val intent = Intent(Intent.ACTION_VIEW, item.link.toUri())
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Read Full Article")
            }
        }
    }
}

@Composable
fun HtmlContent(
    html: String,
    colorScheme: ColorScheme,
    fontScale: Float
) {
    AndroidView<WebView>(
        modifier = Modifier.fillMaxWidth(),
        factory = { context ->
            WebView(context).apply {
                webViewClient = WebViewClient()
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    useWideViewPort = true
                    loadWithOverviewMode = true
                }
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
            }
        },
        update = { webView ->
            val surface = colorScheme.surface.toHtmlHex()
            val onSurface = colorScheme.onSurface.toHtmlHex()
            val primary = colorScheme.primary.toHtmlHex()
            val outline = colorScheme.outline.toHtmlHex()
            val baseFontSize = 16 * fontScale

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
                    $html
                    <script>
                        // Add some expressive Material 3 animations or interactions if needed
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
            webView.loadDataWithBaseURL(null, styledHtml, "text/html", "utf-8", null)
        }
    )
}

fun Color.toHtmlHex(): String {
    return String.format("#%06X", (0xFFFFFF and this.toArgb()))
}
