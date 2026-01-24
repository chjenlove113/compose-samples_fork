package com.news.presentation.base

import androidx.compose.runtime.Composable


data class TabItem(
    val title: String,
    val screen: @Composable () -> Unit // Use a Composable lambda for dynamic content
)
