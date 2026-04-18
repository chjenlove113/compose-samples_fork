package com.news.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import coil3.compose.AsyncImage
import com.news.domain.models.News
import com.news.presentation.R
import com.news.presentation.base.rememberNavigationState
import com.news.presentation.showHome.ItemDetail
import com.news.presentation.showHome.ItemsList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsDetailScreen(
    news: News,
    onBack: () -> Unit,
    onExpand: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(

        topBar = {
            val backStackBackup = rememberNavBackStack()

            TopAppBar(
                title = { Text(text = news.Source) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onExpand) {
                        Icon(imageVector = ImageVector.vectorResource(id = R.drawable.open_in_full_24px), contentDescription = "Expand",
                        )
                    }
                }
            )
        },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
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

                Spacer(modifier = Modifier.height(16.dp))

                news.ShortDes?.let { description ->
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // If there's more content in news.Html or news.NewsItemChilds, display it here
                news.NewsItemChilds.forEach { child ->
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
