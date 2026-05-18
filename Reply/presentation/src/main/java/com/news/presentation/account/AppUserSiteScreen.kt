package com.news.presentation.account

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.news.domain.models.AppUserSite

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AppUserSiteScreen(
    viewModel: AppUserSiteViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var editingSite by remember { mutableStateOf<AppUserSite?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("User Sites") })
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.error != null) {
                Text(
                    text = uiState.error!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    uiState.sites.forEach { (kind, sites) ->
                        stickyHeader {
                            HeaderItem(kind)
                        }
                        items(sites, key = { it.Id }) { site ->
                            SiteListItem(
                                site = site,
                                onToggleActive = { viewModel.toggleActive(site) },
                                onEdit = { editingSite = site }
                            )
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        }
                    }
                }
            }
        }
    }

    if (editingSite != null) {
        EditSiteDialog(
            site = editingSite!!,
            onDismiss = { editingSite = null },
            onConfirm = { updatedSite ->
                viewModel.updateSite(updatedSite)
                editingSite = null
            }
        )
    }
}

@Composable
fun HeaderItem(kind: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = "Kind: $kind",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun SiteListItem(
    site: AppUserSite,
    onToggleActive: () -> Unit,
    onEdit: () -> Unit
) {
    ListItem(
        headlineContent = { Text(site.Name) },
        supportingContent = { Text(site.Url) },
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(
                    checked = site.IsActive,
                    onCheckedChange = { onToggleActive() }
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = onEdit,
                    enabled = site.AllowEdit
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }
            }
        }
    )
}

@Composable
fun EditSiteDialog(
    site: AppUserSite,
    onDismiss: () -> Unit,
    onConfirm: (AppUserSite) -> Unit
) {
    var name by remember { mutableStateOf(site.Name) }
    var url by remember { mutableStateOf(site.Url) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Site") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("URL") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(site.copy(Name = name, Url = url)) }) {
                Text("Update")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
