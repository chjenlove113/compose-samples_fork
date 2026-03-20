package com.news.presentation.components

import androidx.compose.material.Text
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun NewsDetailScreen(modifier: Modifier = Modifier,name: String, onItemClick: (String) -> Unit,goToEx: () -> Unit = {}, onCloseDetail:() -> Unit = {} ) {
    Button(onClick = onCloseDetail) {
        Text(text = "Close")
    }
    Text(text = "Hello $name!")
}