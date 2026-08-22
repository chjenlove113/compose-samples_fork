package com.app.tintuccongnghe.base

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.collectFoldingFeaturesAsState
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.window.core.layout.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import androidx.window.layout.FoldingFeature
import coil3.compose.AsyncImage
import com.google.zxing.common.StringUtils
import com.app.tintuccongnghe.domain.models.News
import com.app.tintuccongnghe.domain.util.isValidUrl
import com.app.tintuccongnghe.presentation.R
import com.app.tintuccongnghe.utils.AppContants

@Composable
fun calculateBottomNavigationBarHeight(): Dp {
    val context = LocalContext.current
    val resourceId = context.resources.getIdentifier("navigation_bar_height", "dimen", "android")
    return if (resourceId > 0) {
        context.resources.getDimensionPixelSize(resourceId).dp + 20.dp
    } else {
        0.dp // Return a default height or handle the case when navigation bar height is not available
    }
}

@Composable
fun ShowLoading() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
    ) {
        val contentDesc = stringResource(R.string.loading)
        CircularProgressIndicator(modifier = Modifier
            .align(Alignment.Center)
            .semantics {
                contentDescription = contentDesc
            })
    }
}

@Composable
fun ShowError(
    modifier: Modifier = Modifier,
    text: String,
    retryEnabled: Boolean = false,
    retryClicked: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_error),
            contentDescription = null,
            modifier = modifier
                .width(120.dp)
                .height(120.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            modifier = modifier.padding(15.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (retryEnabled) {
            Button(onClick = { retryClicked() }, shape = RoundedCornerShape(10.dp)) {
                Text(text = stringResource(R.string.retry))
            }
        }
    }
}

/**
 * A modern, expressive News Item card following Material 3 design principles.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun NewsItem(
    news: News,
    onNewsClick: (News) -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    haveSelectedNews: Boolean = false
) {
    val adaptiveInfo = currentWindowAdaptiveInfoV2()
    val pagePadding = if (adaptiveInfo.windowSizeClass.minWidthDp <= WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND || haveSelectedNews) 8.dp else 16.dp

    androidx.compose.material3.Card(
        onClick = { onNewsClick(news) },
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = pagePadding, vertical = 8.dp),
        shape = if (pagePadding == 8.dp) MaterialTheme.shapes.large else MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
            contentColor = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
        ),
        border = if (selected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Column {
            news.Image?.let { imageUrl ->
                if(imageUrl != ""){
                    Box(
                        modifier = Modifier
                            .fillMaxWidth().padding( (if (pagePadding == 8.dp) 0.dp else pagePadding))
                            .height(200.dp)
                    ) {
                        if (!isValidUrl(imageUrl)){
                            AsyncImage(
                                model = AppContants.baseUrlUI + imageUrl,
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(MaterialTheme.shapes.medium),
                                contentScale = ContentScale.Crop
                            )
                        }else{
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(MaterialTheme.shapes.medium),
                                contentScale = ContentScale.Crop
                            )
                        }

                        // Icon overlay in bottom-right
                        if (news.Is_Video || news.Is_Album) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(12.dp)
                                    .background(
                                        Color.Black.copy(alpha = 0.3f),
                                        shape = CircleShape
                                    )
                                    .padding(4.dp)
                            ) {
                                Icon(
                                    imageVector = if (news.Is_Video) Icons.Default.PlayCircle else Icons.Default.Collections,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }
            
            Column(
                modifier = Modifier
                    .padding(pagePadding)
            ) {
                Text(
                    text = news.Title,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(8.dp))

                news.ShortDes?.let { description ->
                    if(description != ""){
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Optional: Add a small source icon/avatar if available
                        Text(
                            text = news.Source,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    
                    Text(
                        text = news.Date ?: "",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}

/**
 * A modern, expressive News Item card following Material 3 design principles.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun NewsItemAdv(
    news: News,
    onNewsClick: (News) -> Unit,
    modifier: Modifier = Modifier,
    onTabSelected: (String) -> Unit = {},
    selected: Boolean = false,
    showAppCategory: Boolean = false,
    onTabSelectedCategory: (String) -> Unit = {},
    haveSelectedNews: Boolean = false
) {
    val adaptiveInfo = currentWindowAdaptiveInfo()
    val pagePadding = if (adaptiveInfo.windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.COMPACT || haveSelectedNews) 8.dp else 16.dp

    androidx.compose.material3.Card(
        onClick = { onNewsClick(news) },
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = pagePadding, vertical = 8.dp),
        shape = if (pagePadding == 8.dp) MaterialTheme.shapes.large else MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
            contentColor = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
        ),
        border = if (selected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Column {
            news.Image?.let { imageUrl ->

                if(imageUrl != ""){
                    Box(
                        modifier = Modifier
                            .fillMaxWidth().padding((if (pagePadding == 8.dp) 0.dp else pagePadding))
                            .height(200.dp)
                    ) {
                        if (!isValidUrl(imageUrl)){
                            AsyncImage(
                                model = AppContants.baseUrlUI + imageUrl,
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(MaterialTheme.shapes.medium),
                                contentScale = ContentScale.Crop
                            )
                        }else{
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(MaterialTheme.shapes.medium),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Box(modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(20.dp,0.dp,0.dp,12.dp)
                        ) {
                            Text(
                                text = (if (showAppCategory) news.App_Category_Name else news.App_Site_Name) ?: "",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSecondary,
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.secondary.copy(0.8f), shape = CircleShape)
                                    .clip(CircleShape)
                                    .clickable {
                                        if(showAppCategory){
                                            news.App_Category_Slug?.let {
                                                onTabSelectedCategory(it)
                                                Log.d("news.App_Category_Slug?.let { onTabSelected(it)",it.toString())
                                            }
                                        }else{
                                            news.App_Site_Slug?.let {
                                                onTabSelected(it)
                                                Log.d("news.App_Site_Slug?.let { onTabSelected(it)",it.toString())
                                            }
                                         }
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        // Icon overlay in bottom-right
                        if (news.Is_Video || news.Is_Album) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(12.dp)
                                    .background(
                                        Color.Black.copy(alpha = 0.3f),
                                        shape = CircleShape
                                    )
                                    .padding(4.dp)
                            ) {
                                Icon(
                                    imageVector = if (news.Is_Video) Icons.Default.PlayCircle else Icons.Default.Collections,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .padding(pagePadding)
            ) {
                Text(
                    text = news.Title,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                news.ShortDes?.let { description ->
                    if(description != ""){
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {

                        if(news.Source != ""){
                            Text(
                                text = news.Source,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                    }

                    Text(
                        text = news.Date ?: "",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}
enum class WindowMode {
    COMPACT,
    MEDIUM,
    EXPANDED,
    LARGE,
    EXTRA_LARGE
}
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun currentWindowMode(): WindowMode {
    val sizeClass =
        currentWindowAdaptiveInfoV2().windowSizeClass

    return when {
        sizeClass.isWidthAtLeastBreakpoint(
            WindowSizeClass.WIDTH_DP_EXTRA_LARGE_LOWER_BOUND
        ) -> WindowMode.EXTRA_LARGE

        sizeClass.isWidthAtLeastBreakpoint(
            WindowSizeClass.WIDTH_DP_LARGE_LOWER_BOUND
        ) -> WindowMode.LARGE

        sizeClass.isWidthAtLeastBreakpoint(
            WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND
        ) -> WindowMode.EXPANDED

        sizeClass.isWidthAtLeastBreakpoint(
            WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND
        ) -> WindowMode.MEDIUM

        else -> WindowMode.COMPACT
    }
}
enum class AdaptiveMode {
    COMPACT,           // Phone or closed foldable cover screen
    MEDIUM,
    EXPANDED,
    LARGE,
    EXTRA_LARGE,
    FOLDABLE_FLAT,
    FOLDABLE_BOOK,
    FOLDABLE_TABLETOP
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun currentAdaptiveMode(): AdaptiveMode {
    val foldingFeatures by collectFoldingFeaturesAsState()
    val fold = foldingFeatures.firstOrNull()

    if (fold != null) {
        when (fold.state) {
            FoldingFeature.State.HALF_OPENED -> {
                return when (fold.orientation) {
                    FoldingFeature.Orientation.HORIZONTAL ->
                        AdaptiveMode.FOLDABLE_TABLETOP

                    FoldingFeature.Orientation.VERTICAL ->
                        AdaptiveMode.FOLDABLE_BOOK

                    else -> AdaptiveMode.FOLDABLE_FLAT
                }
            }

            FoldingFeature.State.FLAT -> {
                return AdaptiveMode.FOLDABLE_FLAT
            }
        }
    }

    return when (currentWindowMode()) {
        WindowMode.COMPACT -> AdaptiveMode.COMPACT
        WindowMode.MEDIUM -> AdaptiveMode.MEDIUM
        WindowMode.EXPANDED -> AdaptiveMode.EXPANDED
        WindowMode.LARGE -> AdaptiveMode.LARGE
        WindowMode.EXTRA_LARGE -> AdaptiveMode.EXTRA_LARGE
    }
}
