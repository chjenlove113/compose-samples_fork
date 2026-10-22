@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.app.tintuccongnghe.account

import android.app.Activity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.tintuccongnghe.domain.models.AppUserSite
import com.app.tintuccongnghe.presentation.R
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AppUserSiteScreen(
    viewModel: AppUserSiteViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToRss: (Int, String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(false) }
    var showLoginRequiredDialog by remember { mutableStateOf(false) }
    var showCloudDialog by remember { mutableStateOf(false) }
    var showLimitDialog by remember { mutableStateOf(false) }
    var selectedSite by remember { mutableStateOf<AppUserSite?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val sitesMap = remember(uiState.sites) {
        uiState.sites.filter { it.value.isNotEmpty() }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            if (uiState.sites.isNotEmpty()) {
                snackbarHostState.showSnackbar(it)
                viewModel.clearError()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.user_sites), maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    val rssCount = uiState.sites.values.flatten().count { it.GROUP == "1" }
                    val limit = uiState.rssLimit

                    if (rssCount >= limit) {
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = MaterialTheme.shapes.small,
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            Text(
                                text = "Limit $rssCount/$limit",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    // Sync Data Button with login check
                    IconButton(onClick = {
                        if (uiState.isLoggedIn) {
                            viewModel.syncData()
                        } else {
                            showLoginRequiredDialog = true
                        }
                    }) {
                        Icon(Icons.Default.Sync, contentDescription = "Sync Data")
                    }

                    IconButton(onClick = {
                        selectedSite = null
                        showDialog = true
                    }) {
                        Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_site))
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                if (uiState.isLoading && sitesMap.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        LoadingIndicator()
                    }
                } else if (uiState.error != null && sitesMap.isEmpty()) {
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
                } else if (sitesMap.isEmpty() && !uiState.isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(stringResource(R.string.no_sites_found))
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { viewModel.retryLoad() }) {
                                Text(stringResource(R.string.retry))
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        item {
                            val rssCount = uiState.sites.values.flatten().count { it.GROUP == "1" }
                            SubscriptionStatusCard(
                                hasCloud = uiState.hasCloudStorageSubscription,
                                hasLimitSub = uiState.hasActiveSubscription,
                                rssCount = rssCount,
                                limit = uiState.rssLimit,
                                onCloudClick = { showCloudDialog = true },
                                onLimitClick = { showLimitDialog = true }
                            )
                        }
                        sitesMap.forEach { (kind, sites) ->
                            stickyHeader {
                                HeaderItem(kind)
                            }
                            items(sites, key = { "${kind}_${it.GROUP}_${it.Id}" }) { site ->
                                SiteListItem(
                                    site = site,
                                    onToggleActive = { isChecked ->
                                        viewModel.toggleActive(site, isChecked)
                                    },
                                    onEdit = {
                                        selectedSite = site
                                        showDialog = true
                                    },
                                    onSync = {
                                        viewModel.syncSite(site)
                                    },
                                    onCopy = {
                                        viewModel.copySite(site.Id)
                                    },
                                    isSyncing = uiState.syncingSiteIds.contains(site.Id),
                                    onNavigateToRss = onNavigateToRss
                                )
                            }
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

    if (showCloudDialog) {
        CloudStorageSubscriptionDialog(
            onDismiss = { showCloudDialog = false },
            onSubscribe = { productId ->
                showCloudDialog = false
                viewModel.buySubscription(context as Activity, productId) { success ->
                    scope.launch {
                        snackbarHostState.showSnackbar(if (success) "Subscription flow started" else "Failed to launch billing flow")
                    }
                }
            }
        )
    }

    if (showLimitDialog) {
        WebsiteLimitSubscriptionDialog(
            onDismiss = { showLimitDialog = false },
            onSubscribe = { productId ->
                showLimitDialog = false
                viewModel.buySubscription(context as Activity, productId) { success ->
                    scope.launch {
                        snackbarHostState.showSnackbar(if (success) "Subscription flow started" else "Failed to launch billing flow")
                    }
                }
            }
        )
    }

    if (showDialog) {
        val rssCount = uiState.sites.values.flatten().count { it.GROUP == "1" }
        val limit = uiState.rssLimit
        SiteDialog(
            site = selectedSite,
            rssCount = rssCount,
            limit = limit,
            onUpgradeClick = {
                showDialog = false
                showLimitDialog = true
            },
            onDismiss = { showDialog = false },
            onConfirm = { id, name, url, icon, kind, group, isActive ->
                viewModel.createOrUpdateSite(id, name, url, icon, kind, group, isActive, false)
                showDialog = false
            },
            onDelete = { id, name, url, icon, kind, group, isActive ->
                viewModel.deleteSite(id, name, url, icon, kind, group, isActive, false)
                showDialog = false
            }
        )
    }
}

@Composable
fun SubscriptionStatusCard(
    hasCloud: Boolean,
    hasLimitSub: Boolean,
    rssCount: Int,
    limit: Int,
    onCloudClick: () -> Unit,
    onLimitClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Quản lý Gói cước & Đám mây",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Website Limit: $rssCount / $limit",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = if (hasLimitSub) "Đã nâng cấp (+30 sites)" else "Miễn phí (Tối đa 5 sites)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (!hasLimitSub) {
                    Button(onClick = onLimitClick, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)) {
                        Text("Nâng cấp", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Cloud Storage Sync",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = if (hasCloud) "Đang hoạt động" else "Chưa kích hoạt",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (hasCloud) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                }
                if (!hasCloud) {
                    Button(onClick = onCloudClick, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)) {
                        Text("Mua Cloud", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}

@Composable
fun HeaderItem(kind: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Category: $kind".uppercase(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
fun SiteListItem(
    site: AppUserSite,
    onToggleActive: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onSync: () -> Unit,
    onCopy: () -> Unit,
    isSyncing: Boolean,
    onNavigateToRss: (Int, String) -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = site.Name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        modifier = Modifier.weight(1f, false)
                    )
                    if (!site.isSynced) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = MaterialTheme.shapes.extraSmall
                        ) {
                            Text(
                                text = "Unsynced",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = site.IsActive,
                        onCheckedChange = { isChecked -> onToggleActive(isChecked) }
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(
                        onClick = onEdit,
                        enabled = site.AllowEdit,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = stringResource(R.string.edit),
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = site.Url,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.items_count, site.itemCount),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
                if (site.lastRefreshTime != null) {
                    Text(
                        text = "• Refresh: ${formatTime(site.lastRefreshTime!!)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (site.AllowEdit) {
                    FilledTonalButton(
                        onClick = { onNavigateToRss(site.Id, site.GROUP) },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text("Go to News", style = MaterialTheme.typography.labelMedium)
                    }
                }

                if (site.GROUP == "1" && site.Url.isNotBlank()) {
                    Button(
                        onClick = onSync,
                        enabled = !isSyncing,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        if (isSyncing) {
                            LoadingIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Syncing...", style = MaterialTheme.typography.labelMedium)
                        } else {
                            Text("Sync News", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }

                if (site.GROUP == "2") {
                    OutlinedButton(
                        onClick = onCopy,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text("Copy site", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}

private fun formatTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm dd/MM/yy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

@Composable
fun CloudStorageSubscriptionDialog(
    onDismiss: () -> Unit,
    onSubscribe: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Subscription Cloud Storage",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Đồng bộ lưu trữ đám mây Google Firestore cho dữ liệu cá nhân của bạn.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSubscribe("cloud_storage_monthly_sub") },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Monthly Plan",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "1 Month • Cloud Storage",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "$0.99",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSubscribe("cloud_storage_annual_sub") },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Annual Plan",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = MaterialTheme.shapes.extraSmall
                                ) {
                                    Text(
                                        text = "BEST VALUE",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "1 Year • Cloud Storage",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Text(
                            text = "$5.99",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Đóng")
            }
        }
    )
}

@Composable
fun WebsiteLimitSubscriptionDialog(
    onDismiss: () -> Unit,
    onSubscribe: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Nâng cấp giới hạn Website",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Mở rộng thêm 30 website (Nâng tổng giới hạn lên 35 website RSS).",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Weekly
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSubscribe("rss_weekly_sub") },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Weekly Plan", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("1 Week • +30 Websites", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text("$0.49", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                    }
                }

                // Monthly
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSubscribe("rss_monthly_sub") },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Monthly Plan", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("1 Month • +30 Websites", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text("$0.99", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                    }
                }

                // Annual
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSubscribe("rss_annual_sub") },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Annual Plan", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    color = MaterialTheme.colorScheme.secondary,
                                    shape = MaterialTheme.shapes.extraSmall
                                ) {
                                    Text(
                                        text = "BEST VALUE",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSecondary,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text("1 Year • +30 Websites", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
                        }
                        Text("$5.59", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Đóng")
            }
        }
    )
}

@Composable
fun SiteDialog(
    site: AppUserSite? = null,
    rssCount: Int = 0,
    limit: Int = 5,
    onUpgradeClick: () -> Unit,
    onDismiss: () -> Unit,
    onConfirm: (id: Int, name: String, url: String, icon: String, kind: String, group: String, isActive: Boolean) -> Unit,
    onDelete: (id: Int, name: String, url: String, icon: String, kind: String, group: String, isActive: Boolean) -> Unit
) {
    val isEdit = site != null
    var name by remember { mutableStateOf(site?.Name ?: "") }
    var url by remember { mutableStateOf(site?.Url ?: "") }
    var icon by remember { mutableStateOf("") }
    var kind by remember { mutableStateOf(site?.Kind ?: "") }
    var isActive by remember { mutableStateOf(site?.IsActive ?: true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEdit) stringResource(R.string.update_site) else stringResource(R.string.add_new_site)) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Spacer(modifier = Modifier.height(4.dp))
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
                    }
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!isEdit && rssCount >= limit) {
                    TextButton(onClick = onUpgradeClick) {
                        Text("Limit $rssCount/$limit. Tap to Upgrade", color = MaterialTheme.colorScheme.error)
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                Row {
                    if (site != null && site.GROUP == "1") {
                        val group = site.GROUP
                        TextButton(
                            onClick = { onDelete(site.Id, name, url, icon, kind, group, isActive) },
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text(stringResource(R.string.delete))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    Button(
                        onClick = {
                            val group = site?.GROUP ?: "1" // Default to 1 for new RSS sites
                            onConfirm(site?.Id ?: 0, name, url, icon, kind, group, isActive)
                        },
                        enabled = name.isNotBlank() && url.isNotBlank() && (isEdit || rssCount < limit)
                    ) {
                        Text(if (isEdit) stringResource(R.string.update) else stringResource(R.string.add))
                    }
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
