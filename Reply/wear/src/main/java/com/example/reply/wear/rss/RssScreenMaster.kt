package com.example.reply.wear.rss

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.CardDefaults
import androidx.wear.compose.material3.EdgeButton
import androidx.wear.compose.material3.EdgeButtonSize
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TitleCard
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import com.example.reply.wear.R

@Composable
fun RssScreenMaster(
    onAllClick: () -> Unit,
    onSiteClick: (Int, String, String, String) -> Unit,
    viewModel: RssMasterViewModel = hiltViewModel(),
) {
    val sites by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberTransformingLazyColumnState()
    val transformationSpec = rememberTransformationSpec()

    LaunchedEffect(Unit) {
        if (sites.isEmpty()) {
            viewModel.refresh()
        }
    }

    ScreenScaffold(
        scrollState = scrollState,
        edgeButton = {
            EdgeButton(
                onClick = viewModel::refresh,
                buttonSize = EdgeButtonSize.Medium,
            ) {
                Text(stringResource(R.string.refresh))
            }
        },
    ) { contentPadding ->
        TransformingLazyColumn(
            state = scrollState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = contentPadding,
        ) {
            item {
                ListHeader(
                    modifier = Modifier
                        .fillMaxWidth()
                        .transformedHeight(this, transformationSpec),
                    transformation = SurfaceTransformation(transformationSpec),
                ) {
                    Text(stringResource(R.string.rss_sites))
                }
            }

            item {
                TitleCard(
                    onClick = onAllClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .transformedHeight(this, transformationSpec),
                    transformation = SurfaceTransformation(transformationSpec),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        titleColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        subtitleColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    ),
                    title = { Text(stringResource(R.string.all_news)) },
                    subtitle = { Text(stringResource(R.string.latest_news_from_all_sites)) },
                )
            }

            if (sites.isEmpty()) {
                item {
                    TitleCard(
                        onClick = viewModel::refresh,
                        modifier = Modifier
                            .fillMaxWidth()
                            .transformedHeight(this, transformationSpec),
                        transformation = SurfaceTransformation(transformationSpec),
                        title = { Text(stringResource(R.string.no_rss_sites)) },
                        subtitle = { Text(stringResource(R.string.tap_to_refresh)) },
                    )
                }
            } else {
                items(sites, key = { it.Id }) { site ->
                    TitleCard(
                        onClick = { onSiteClick(site.Id, site.GROUP, site.Kind, site.Name) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .transformedHeight(this, transformationSpec),
                        transformation = SurfaceTransformation(transformationSpec),
                        title = {
                            Text(
                                text = site.Name,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                        },
                        subtitle = {
                            Text(
                                text = stringResource(R.string.item_count, site.itemCount),
                                maxLines = 1,
                            )
                        },
                    )
                }
            }
        }
    }
}
