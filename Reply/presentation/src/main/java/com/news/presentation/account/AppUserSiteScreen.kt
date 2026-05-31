package com.news.presentation.account

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.news.domain.models.AppUserSite

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AppUserSiteScreen(
    viewModel: AppUserSiteViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(false) }
    var selectedSite by remember { mutableStateOf<AppUserSite?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("User Sites") },
                actions = {
                    IconButton(onClick = { 
                        selectedSite = null
                        showDialog = true 
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Site")
                    }
                }
            )
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
                                onEdit = { 
                                    selectedSite = site
                                    showDialog = true 
                                }
                            )
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        SiteDialog(
            site = selectedSite,
            onDismiss = { showDialog = false },
            onConfirm = { id, name, url, icon, kind, isActive ->
                viewModel.createOrUpdateSite(id, name, url, icon, kind, isActive)
                showDialog = false
            },
            onDelete = { id, name, url, icon, kind, isActive ->
                viewModel.deleteSite(id, name, url, icon, kind, isActive)
                showDialog = false
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
fun SiteDialog(
    site: AppUserSite? = null,
    onDismiss: () -> Unit,
    onConfirm: (id: Int, name: String, url: String, icon: String, kind: String, isActive: Boolean) -> Unit,
    onDelete: (id: Int, name: String, url: String, icon: String, kind: String, isActive: Boolean) -> Unit
) {
    val isEdit = site != null
    var name by remember { mutableStateOf(site?.Name ?: "") }
    var url by remember { mutableStateOf(site?.Url ?: "") }
    var icon by remember { mutableStateOf("") }
    var kind by remember { mutableStateOf(site?.Kind ?: "") }
    var isActive by remember { mutableStateOf(site?.IsActive ?: true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEdit) "Update Site" else "Add New Site") },
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
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = icon,
                    onValueChange = { icon = it },
                    label = { Text("Icon") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = kind,
                    onValueChange = { kind = it },
                    label = { Text("Kind") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Active")
                    Switch(
                        checked = isActive,
                        onCheckedChange = { isActive = it }
                    )
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (site != null && site.GROUP == "1") {
                    TextButton(
                        onClick = { onDelete(site.Id, name, url, icon, kind, isActive) },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Delete")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Button(
                    onClick = { onConfirm(site?.Id ?: 0, name, url, icon, kind, isActive) },
                    enabled = name.isNotBlank() && url.isNotBlank()
                ) {
                    Text(if (isEdit) "Update" else "Add")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
