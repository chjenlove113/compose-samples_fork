package com.example.reply.wear.rss

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TimeText
import androidx.wear.compose.material3.TitleCard

@Composable
fun RssScreenMaster(
    onAllClick: () -> Unit,
    onSiteClick: (Int, String, String, String) -> Unit,
    viewModel: RssMasterViewModel = hiltViewModel()
) {
    val sites by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScalingLazyListState()

    LaunchedEffect(Unit) {
        if (sites.isEmpty()) {
            viewModel.refresh()
        }
    }

    AppScaffold {
        ScreenScaffold(
            scrollState = scrollState,
            timeText = { TimeText() }
        ) {
            ScalingLazyColumn(
                state = scrollState,
                modifier = Modifier.fillMaxSize(),
                autoCentering = AutoCenteringParams(itemIndex = 0),
            ) {
                item {
                    Text(
                        text = "RSS Sites",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                item {
                    TitleCard(
                        onClick = onAllClick,
                        title = { Text("Tất cả") }
                    ) {
                        Text(
                            text = "Tất cả tin mới nhất",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }

                if (sites.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("No RSS sites")
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(onClick = { viewModel.refresh() }) {
                                    Text("Refresh")
                                }
                            }
                        }
                    }
                } else {
                    items(sites) { site ->
                        TitleCard(
                            onClick = { onSiteClick(site.Id, site.GROUP, site.Kind, site.Name) },
                            title = { Text(site.Name) }
                        ) {
                            Text(
                                text = "${site.itemCount} items",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                    item {
                        Button(onClick = { viewModel.refresh() }) {
                            Text("Refresh Sites")
                        }
                    }
                }
            }
        }
    }
}
