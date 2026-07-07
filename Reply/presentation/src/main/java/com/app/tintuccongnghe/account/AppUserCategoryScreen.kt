package com.app.tintuccongnghe.account

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
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
fun AppUserCategoryScreen(
    viewModel: AppUserSiteViewModel = hiltViewModel(),
    onNavigateToLogin: () -> Unit,
    onNavigateToCategoryItems: (Int, String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showLoginRequiredDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Filter sites to only include those with GROUP == "0"
    val group0Sites = remember(uiState.sites) {
        uiState.sites.mapValues { (_, sites) ->
            sites.filter { it.GROUP == "0" }
        }.filter { it.value.isNotEmpty() }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            if (uiState.sites.isNotEmpty() && !it.contains("limit", ignoreCase = true)) {
                snackbarHostState.showSnackbar(it)
                viewModel.clearError()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.user_categories)) }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                if (uiState.isLoading && group0Sites.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (uiState.error != null && group0Sites.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = uiState.error!!,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { viewModel.retryLoad() }) {
                                Text(stringResource(R.string.retry))
                            }
                        }
                    }
                } else if (group0Sites.isEmpty() && !uiState.isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("No categories found.")
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { viewModel.retryLoad() }) {
                                Text(stringResource(R.string.retry))
                            }
                        }
                    }
                } else {
                    val allCategories = remember(group0Sites) { group0Sites.values.flatten() }
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(allCategories, key = { "${it.Kind}_${it.GROUP}_${it.Id}" }) { site ->
                            CategoryListItem(
                                site = site,
                                onToggleActive = {
                                    if (uiState.isLoggedIn) {
                                        viewModel.toggleActive(site)
                                    } else {
                                        showLoginRequiredDialog = true
                                    }
                                },
                                onNavigateToCategoryItems = onNavigateToCategoryItems
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
}

@Composable
fun CategoryListItem(
    site: AppUserSite,
    onToggleActive: () -> Unit,
    onNavigateToCategoryItems: (Int, String) -> Unit
) {
    ListItem(
        modifier = Modifier.clickable { onNavigateToCategoryItems(site.Id, site.GROUP) },
        headlineContent = { Text(site.Name) },
        supportingContent = {
            Column {
                Text(site.Url)
            }
        },
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(
                    checked = site.IsActive,
                    onCheckedChange = { onToggleActive() }
                )
            }
        }
    )
}

