package com.example.reply.wear.rss

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TitleCard
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import androidx.wear.remote.interactions.RemoteActivityHelper
import com.example.reply.wear.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RssScreen(
    viewModel: RssViewModel = hiltViewModel(),
) {
    val items by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val remoteActivityHelper = remember { RemoteActivityHelper(context) }
    val scrollState = rememberTransformingLazyColumnState()
    val transformationSpec = rememberTransformationSpec()
    val openOnPhoneMessage = stringResource(R.string.opening_on_phone)

    LaunchedEffect(Unit) {
        if (items.isEmpty()) {
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
                    Text(
                        text = viewModel.siteName,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            if (items.isEmpty()) {
                item {
                    TitleCard(
                        onClick = viewModel::refresh,
                        modifier = Modifier
                            .fillMaxWidth()
                            .transformedHeight(this, transformationSpec),
                        transformation = SurfaceTransformation(transformationSpec),
                        title = { Text(stringResource(R.string.no_rss_items)) },
                        subtitle = { Text(stringResource(R.string.tap_to_refresh)) },
                    )
                }
            } else {
                items(items, key = { it.link }) { item ->
                    TitleCard(
                        onClick = {
                            val encodedLink = Uri.encode(item.link)
                            remoteActivityHelper.startRemoteActivity(
                                Intent(Intent.ACTION_VIEW)
                                    .addCategory(Intent.CATEGORY_BROWSABLE)
                                    .setData(Uri.parse("reply://rss_feed?link=$encodedLink"))
                            )
                            Toast.makeText(
                                context,
                                openOnPhoneMessage,
                                Toast.LENGTH_SHORT,
                            ).show()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .transformedHeight(this, transformationSpec),
                        transformation = SurfaceTransformation(transformationSpec),
                        colors = CardDefaults.cardColors(),
                        title = {
                            Text(
                                text = item.title,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                        },
                        subtitle = {
                            Text(
                                text = item.siteName ?: stringResource(R.string.rss_feed),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        },
                        time = item.pubDate?.let { timestamp ->
                            { Text(timestamp.toShortDateTime()) }
                        },
                    )
                }
            }
        }
    }
}

private fun Long.toShortDateTime(): String =
    SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(Date(this))
