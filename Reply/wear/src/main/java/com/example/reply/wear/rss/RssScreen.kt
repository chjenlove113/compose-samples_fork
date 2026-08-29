package com.example.reply.wear.rss

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.foundation.lazy.AutoCenteringParams
import androidx.compose.foundation.layout.PaddingValues
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TimeText
import androidx.wear.compose.material3.TitleCard
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.wear.remote.interactions.RemoteActivityHelper
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow

@Composable
fun RssScreen(
    viewModel: RssViewModel = hiltViewModel()
) {
    val items by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val remoteActivityHelper = remember { RemoteActivityHelper(context) }
    val scrollState = rememberScalingLazyListState()

    LaunchedEffect(Unit) {
        if (items.isEmpty()) {
            viewModel.refresh()
        }
    }

    AppScaffold {
        ScreenScaffold(
            scrollState = scrollState,
            timeText = { TimeText() }
        ) {
            if (items.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("No RSS items")
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.refresh() }) {
                            Text("Refresh")
                        }
                    }
                }
            } else {
                ScalingLazyColumn(
                    state = scrollState,
                    modifier = Modifier.fillMaxSize(),
                    autoCentering = AutoCenteringParams(itemIndex = 0)
                ) {
                    item {
                        Text(
                            text = viewModel.siteName,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    items(items) { item ->
                        TitleCard(
                            onClick = { 
                                val encodedLink = Uri.encode(item.link)
                                remoteActivityHelper.startRemoteActivity(
                                    Intent(Intent.ACTION_VIEW)
                                        .addCategory(Intent.CATEGORY_BROWSABLE)
                                        .setData(Uri.parse("reply://rss_feed?link=$encodedLink"))
                                )
                                Toast.makeText(context, "Mở trên điện thoại...", Toast.LENGTH_SHORT).show()
                            },
                            title = { Text(item.title, maxLines = 2) }
                        ) {
                            Row() {

                                Text(
                                    text = item.siteName ?: "RSS Feed",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.weight(1F),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )

                                val dateStr = item.pubDate?.let {
                                    java.text.SimpleDateFormat("dd/MM/yy HH:mm", java.util.Locale.getDefault()).format(java.util.Date(it))
                                } ?: ""
                                Text(
                                    text = dateStr,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                    item {
                        Button(onClick = { viewModel.refresh() }) {
                            Text("Refresh")
                        }
                    }
                }
            }
        }
    }
}
