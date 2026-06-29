package com.app.tintuccongnghe.base

import androidx.compose.runtime.Composable


data class TabItem(
    val key: String,
    val title: String,
    val screen: @Composable () -> Unit // Use a Composable lambda for dynamic content
)
