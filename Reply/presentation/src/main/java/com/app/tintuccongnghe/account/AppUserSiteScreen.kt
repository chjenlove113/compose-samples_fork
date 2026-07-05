package com.app.tintuccongnghe.account

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.tintuccongnghe.domain.models.AppUserSite
import com.app.tintuccongnghe.presentation.R
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AppUserSiteScreen(
    viewModel: AppUserSiteViewModel = hiltViewModel(),
    onNavigateToLogin: () -> Unit,
    onNavigateToRss: (Int, String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(false) }
    var showLoginRequiredDialog by remember { mutableStateOf(false) }
    var selectedSite by remember { mutableStateOf<AppUserSite?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.user_sites)) },
                actions = {
                    IconButton(onClick = {
                        if (uiState.isLoggedIn) {
                            selectedSite = null
                            showDialog = true
                        } else {
                            showLoginRequiredDialog = true
                        }
                    }) {
                        Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_site))
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.error != null) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = uiState.error!!,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { viewModel.retryLoad() }) {
                        Text(stringResource(R.string.retry))
                    }
                }
            } else if (uiState.sites.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(stringResource(R.string.no_sites_found))
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { viewModel.retryLoad() }) {
                        Text(stringResource(R.string.retry))
                    }
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    uiState.sites.forEach { (kind, sites) ->
                        stickyHeader {
                            HeaderItem(kind)
                        }
                        items(sites, key = { "${kind}_${it.GROUP}_${it.Id}" }) { site ->
                            SiteListItem(
                                site = site,
                                onToggleActive = {
                                    if (uiState.isLoggedIn) {
                                        viewModel.toggleActive(site)
                                    } else {
                                        showLoginRequiredDialog = true
                                    }
                                },
                                onEdit = {
                                    if (uiState.isLoggedIn) {
                                        selectedSite = site
                                        showDialog = true
                                    } else {
                                        showLoginRequiredDialog = true
                                    }
                                },
                                onSync = {
                                    if (uiState.isLoggedIn) {
                                        viewModel.syncSite(site)
                                    } else {
                                        showLoginRequiredDialog = true
                                    }
                                },
                                isSyncing = uiState.syncingSiteIds.contains(site.Id),
                                onNavigateToRss = onNavigateToRss
                            )
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        }
                    }
                }
            }
        }
    }

    if (showLoginRequiredDialog) {
        AlertDialog(
            onDismissRequest = { showLoginRequiredDialog = false },
            title = { Text(stringResource(R.string.login_required)) },
            text = { Text(stringResource(R.string.login_required_message)) },
            confirmButton = {
                Button(onClick = {
                    showLoginRequiredDialog = false
                    onNavigateToLogin()
                }) {
                    Text(stringResource(R.string.yes))
                }
            },
            dismissButton = {
                TextButton(onClick = { showLoginRequiredDialog = false }) {
                    Text(stringResource(R.string.no))
                }
            }
        )
    }

    if (showDialog) {
        SiteDialog(
            site = selectedSite,
            onDismiss = { showDialog = false },
            onConfirm = { id, name, url, icon, kind, isActive, otherCanSee ->
                viewModel.createOrUpdateSite(id, name, url, icon, kind, isActive, otherCanSee)
                showDialog = false
            },
            onDelete = { id, name, url, icon, kind, isActive, otherCanSee ->
                viewModel.deleteSite(id, name, url, icon, kind, isActive, otherCanSee)
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
            text = stringResource(R.string.kind_format, kind),
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
    onEdit: () -> Unit,
    onSync: () -> Unit,
    isSyncing: Boolean,
    onNavigateToRss: (Int, String) -> Unit
) {
    ListItem(
        modifier = Modifier.clickable { onNavigateToRss(site.Id, site.GROUP) },
        headlineContent = { Text(site.Name) },
        supportingContent = {
            Column {
                Text(site.Url)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.items_count, site.itemCount),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (site.lastRefreshTime != null) {
                        Text(
                            text = stringResource(R.string.last_refresh, formatTime(site.lastRefreshTime!!)),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                if (site.nextRefreshTime != null) {
                    Text(
                        text = stringResource(R.string.next_refresh, formatTime(site.nextRefreshTime!!)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                if (site.GROUP == "1" && site.Url.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = onSync,
                        enabled = !isSyncing,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        if (isSyncing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Syncing...", style = MaterialTheme.typography.labelMedium)
                        } else {
                            Text("Sync News", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        },
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
                    Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.edit))
                }
            }
        }
    )
}

private fun formatTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm dd/MM/yy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

@Composable
fun SiteDialog(
    site: AppUserSite? = null,
    onDismiss: () -> Unit,
    onConfirm: (id: Int, name: String, url: String, icon: String, kind: String, isActive: Boolean, otherCanSee: Boolean) -> Unit,
    onDelete: (id: Int, name: String, url: String, icon: String, kind: String, isActive: Boolean, otherCanSee: Boolean) -> Unit
) {
    val isEdit = site != null
    var name by remember { mutableStateOf(site?.Name ?: "") }
    var url by remember { mutableStateOf(site?.Url ?: "") }
    var icon by remember { mutableStateOf("") }
    var kind by remember { mutableStateOf(site?.Kind ?: "") }
    var isActive by remember { mutableStateOf(site?.IsActive ?: true) }
    var otherCanSee by remember { mutableStateOf(site?.OtherCanSee ?: false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEdit) stringResource(R.string.update_site) else stringResource(R.string.add_new_site)) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.name)) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text(stringResource(R.string.url)) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = icon,
                    onValueChange = { icon = it },
                    label = { Text(stringResource(R.string.icon)) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = kind,
                    onValueChange = { kind = it },
                    label = { Text(stringResource(R.string.kind)) },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.active_status),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = stringResource(R.string.enable_disable_site),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Switch(
                                checked = isActive,
                                onCheckedChange = { isActive = it }
                            )
                        }
                        
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.visibility),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = stringResource(R.string.allow_others_visibility),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Switch(
                                checked = otherCanSee,
                                onCheckedChange = { otherCanSee = it }
                            )
                        }
                    }
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
                        onClick = { onDelete(site.Id, name, url, icon, kind, isActive, otherCanSee) },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text(stringResource(R.string.delete))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Button(
                    onClick = { onConfirm(site?.Id ?: 0, name, url, icon, kind, isActive, otherCanSee) },
                    enabled = name.isNotBlank() && url.isNotBlank()
                ) {
                    Text(if (isEdit) stringResource(R.string.update) else stringResource(R.string.add))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
