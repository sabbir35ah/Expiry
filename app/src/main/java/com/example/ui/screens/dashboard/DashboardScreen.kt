package com.example.ui.screens.dashboard

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.DocumentCategory
import com.example.data.model.DocumentItem
import com.example.data.model.ExpiryStatus
import com.example.ui.components.DocumentCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.ExpiryUiState
import com.example.ui.viewmodel.ExpiryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: ExpiryViewModel,
    uiState: ExpiryUiState,
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToEditor: (Long) -> Unit,
    onNavigateToScanner: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.testTag("dashboard_screen"),
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onNavigateToEditor(0L) },
                icon = { Icon(Icons.Filled.Add, contentDescription = "Add Document") },
                text = { Text("Add Document") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("fab_add_document")
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Top Bar
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(NavyDark),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_expiry_logo),
                                contentDescription = "Expiry App",
                                modifier = Modifier.size(32.dp),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Column {
                            Text(
                                text = "EXPIRY",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Shield,
                                    contentDescription = null,
                                    tint = BrandEmerald,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = "On-Device Privacy Vault",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = BrandEmerald,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // OCR Scan Shortcut
                        IconButton(
                            onClick = onNavigateToScanner,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .testTag("btn_quick_scan")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.DocumentScanner,
                                contentDescription = "Scan Document",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        // Privacy Mask Toggle
                        IconButton(
                            onClick = { viewModel.togglePrivacyRedact(!uiState.isPrivacyRedactActive) },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .testTag("btn_toggle_mask")
                        ) {
                            Icon(
                                imageVector = if (uiState.isPrivacyRedactActive) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = if (uiState.isPrivacyRedactActive) "Show numbers" else "Hide numbers",
                                tint = if (uiState.isPrivacyRedactActive) BrandAmber else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }

            // Stats Cards Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard(
                        count = uiState.stats.expired + uiState.stats.critical,
                        label = "Urgent / Due",
                        containerColor = if (uiState.stats.expired + uiState.stats.critical > 0) StatusCriticalContainer else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (uiState.stats.expired + uiState.stats.critical > 0) StatusCritical else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                if (uiState.selectedStatus == ExpiryStatus.CRITICAL) {
                                    viewModel.setSelectedStatus(null)
                                } else {
                                    viewModel.setSelectedStatus(ExpiryStatus.CRITICAL)
                                }
                            }
                    )

                    StatCard(
                        count = uiState.stats.upcoming,
                        label = "Within 90d",
                        containerColor = StatusUpcomingContainer,
                        contentColor = StatusUpcoming,
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                if (uiState.selectedStatus == ExpiryStatus.UPCOMING) {
                                    viewModel.setSelectedStatus(null)
                                } else {
                                    viewModel.setSelectedStatus(ExpiryStatus.UPCOMING)
                                }
                            }
                    )

                    StatCard(
                        count = uiState.stats.safe,
                        label = "Safe (>90d)",
                        containerColor = StatusActiveContainer,
                        contentColor = StatusActive,
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                if (uiState.selectedStatus == ExpiryStatus.ACTIVE) {
                                    viewModel.setSelectedStatus(null)
                                } else {
                                    viewModel.setSelectedStatus(ExpiryStatus.ACTIVE)
                                }
                            }
                    )
                }
            }

            // Urgent Banner Alert (if any documents due <= 30 days)
            item {
                val mostUrgent = uiState.documents
                    .filter { it.status == ExpiryStatus.CRITICAL || it.status == ExpiryStatus.EXPIRED }
                    .minByOrNull { it.daysRemaining }

                if (mostUrgent != null) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (mostUrgent.status == ExpiryStatus.EXPIRED) StatusExpiredContainer else StatusCriticalContainer
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToDetail(mostUrgent.id) }
                            .testTag("urgent_banner")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(if (mostUrgent.status == ExpiryStatus.EXPIRED) StatusExpired else StatusCritical),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Warning,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (mostUrgent.status == ExpiryStatus.EXPIRED) "Action Required: Expired" else "Upcoming Deadline Warning",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (mostUrgent.status == ExpiryStatus.EXPIRED) StatusExpired else StatusCritical
                                )
                                Text(
                                    text = "${mostUrgent.title} ${mostUrgent.relativeStatusText}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = mostUrgent.category.renewalLeadTimeNotice,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Icon(
                                imageVector = Icons.Filled.ChevronRight,
                                contentDescription = "View",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_bar"),
                    placeholder = { Text("Search by name, number, authority...") },
                    leadingIcon = {
                        Icon(Icons.Filled.Search, contentDescription = "Search")
                    },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Filled.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }

            // Category Filter Pills
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = uiState.selectedCategory == null && uiState.selectedStatus == null,
                            onClick = {
                                viewModel.setSelectedCategory(null)
                                viewModel.setSelectedStatus(null)
                            },
                            label = { Text("All (${uiState.documents.size})") },
                            modifier = Modifier.testTag("filter_all")
                        )
                    }

                    item {
                        FilterChip(
                            selected = uiState.selectedStatus == ExpiryStatus.CRITICAL,
                            onClick = {
                                viewModel.setSelectedStatus(
                                    if (uiState.selectedStatus == ExpiryStatus.CRITICAL) null else ExpiryStatus.CRITICAL
                                )
                            },
                            label = { Text("Urgent (${uiState.stats.critical})") },
                            leadingIcon = {
                                Icon(Icons.Filled.Warning, contentDescription = null, tint = StatusCritical, modifier = Modifier.size(16.dp))
                            }
                        )
                    }

                    items(DocumentCategory.entries.filter { cat ->
                        uiState.documents.any { it.category == cat }
                    }) { cat ->
                        val count = uiState.documents.count { it.category == cat }
                        FilterChip(
                            selected = uiState.selectedCategory == cat,
                            onClick = {
                                viewModel.setSelectedCategory(
                                    if (uiState.selectedCategory == cat) null else cat
                                )
                            },
                            label = { Text("${cat.displayName} ($count)") }
                        )
                    }
                }
            }

            // Documents List or Empty State
            if (uiState.filteredDocuments.isEmpty()) {
                item {
                    EmptyVaultState(
                        isFiltered = uiState.searchQuery.isNotBlank() || uiState.selectedCategory != null || uiState.selectedStatus != null,
                        onResetFilter = {
                            viewModel.setSearchQuery("")
                            viewModel.setSelectedCategory(null)
                            viewModel.setSelectedStatus(null)
                        },
                        onAddDocument = { onNavigateToEditor(0L) },
                        onLoadDemo = { viewModel.seedDemoData() }
                    )
                }
            } else {
                items(uiState.filteredDocuments, key = { it.id }) { doc ->
                    DocumentCard(
                        document = doc,
                        isRedactActive = uiState.isPrivacyRedactActive,
                        onClick = { onNavigateToDetail(doc.id) },
                        onTogglePin = { viewModel.togglePin(doc.id) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
private fun StatCard(
    count: Int,
    label: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = contentColor,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun EmptyVaultState(
    isFiltered: Boolean,
    onResetFilter: () -> Unit,
    onAddDocument: () -> Unit,
    onLoadDemo: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_secure_vault),
                contentDescription = "Zero Cloud Privacy Vault",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (isFiltered) "No Matching Documents" else "Your Vault is Empty",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = if (isFiltered)
                    "Try adjusting your search terms or filters to find what you're looking for."
                else
                    "Track your passports, licenses, visas, warranties, and insurance in one private on-device vault.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
            )

            if (isFiltered) {
                OutlinedButton(
                    onClick = onResetFilter,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Clear Filters")
                }
            } else {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onAddDocument,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add Document")
                    }

                    OutlinedButton(
                        onClick = onLoadDemo,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Load Demo")
                    }
                }
            }
        }
    }
}
