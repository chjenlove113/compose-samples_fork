package com.news.presentation.components

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun NewsDetailScreen(modifier: Modifier = Modifier,name: String, onItemClick: (String) -> Unit,goToEx: () -> Unit = {}) {
    Text(text = "Hello $name!")
}