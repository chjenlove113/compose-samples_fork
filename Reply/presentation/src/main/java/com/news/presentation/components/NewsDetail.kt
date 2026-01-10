package com.news.presentation.components

import androidx.compose.material.Text
import androidx.compose.runtime.Composable

@Composable
fun NewsDetailScreen(name: String, onItemClick: (String) -> Unit) {
    Text(text = "Hello $name!")
}