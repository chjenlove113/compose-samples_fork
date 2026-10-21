package com.app.tintuccongnghe.account

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppUserCategoryScreen(
    viewModel: WebsiteManagementViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val websites by viewModel.websites.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quản lý Website") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            if (websites.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Không có website nào trong Room Database.")
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(websites, key = { it.id }) { website ->
                        ListItem(
                            headlineContent = { Text(website.name.ifEmpty { website.id }) },
                            trailingContent = {
                                Switch(
                                    checked = website.isActive,
                                    onCheckedChange = { isChecked ->
                                        viewModel.updateWebsiteActive(website.id, isChecked)
                                    }
                                )
                            }
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    }
                }
            }
        }
    }
}
