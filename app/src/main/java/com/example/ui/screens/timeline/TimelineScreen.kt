package com.example.ui.screens.timeline

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DocumentCategory
import com.example.data.model.DocumentItem
import com.example.data.model.ExpiryStatus
import com.example.data.util.ExpiryDateUtils
import com.example.ui.components.ExpiryBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.ExpiryUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineScreen(
    uiState: ExpiryUiState,
    onNavigateToDetail: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val documents = uiState.documents.sortedBy { it.expiryDateMillis }
    val expiredDocs = documents.filter { it.status == ExpiryStatus.EXPIRED }
    val futureDocs = documents.filter { it.status != ExpiryStatus.EXPIRED }

    // Group future docs by "Month Year" (e.g. "November 2026")
    val groupedByMonth = futureDocs.groupBy { ExpiryDateUtils.formatMonthYear(it.expiryDateMillis) }

    Scaffold(
        modifier = modifier.testTag("timeline_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Expiration Timeline",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Chronological roadmap of all upcoming renewal milestones",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        if (documents.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No documents found to display on timeline.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Expired Section (if any)
                if (expiredDocs.isNotEmpty()) {
                    item {
                        TimelineSectionHeader(
                            title = "Past / Expired Deadlines (${expiredDocs.size})",
                            color = MaterialTheme.colorScheme.error,
                            icon = Icons.Filled.History
                        )
                    }

                    items(expiredDocs) { doc ->
                        TimelineItemCard(
                            document = doc,
                            onClick = { onNavigateToDetail(doc.id) }
                        )
                    }
                }

                // Future Sections grouped by Month
                groupedByMonth.forEach { (monthLabel, docsInMonth) ->
                    item {
                        TimelineSectionHeader(
                            title = monthLabel,
                            color = MaterialTheme.colorScheme.primary,
                            icon = Icons.Filled.CalendarMonth
                        )
                    }

                    items(docsInMonth) { doc ->
                        TimelineItemCard(
                            document = doc,
                            onClick = { onNavigateToDetail(doc.id) }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

@Composable
private fun TimelineSectionHeader(
    title: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(top = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(16.dp)
            )
        }

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
private fun TimelineItemCard(
    document: DocumentItem,
    onClick: () -> Unit
) {
    val category = document.category
    val status = document.status

    val statusDotColor = when (status) {
        ExpiryStatus.EXPIRED -> MaterialTheme.colorScheme.error
        ExpiryStatus.CRITICAL -> Color(0xFFEA580C)
        ExpiryStatus.UPCOMING -> Color(0xFFD97706)
        ExpiryStatus.ACTIVE -> BrandEmerald
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Timeline Dot & Line
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(20.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(statusDotColor)
            )
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(64.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            )
        }

        // Card Content
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier
                .weight(1f)
                .testTag("timeline_item_${document.id}")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = category.displayName,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = category.color
                    )

                    ExpiryBadge(status = status, daysRemaining = document.daysRemaining)
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = document.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Due: ${ExpiryDateUtils.formatDisplayDate(document.expiryDateMillis)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = document.relativeStatusText,
                        style = MaterialTheme.typography.labelSmall,
                        color = statusDotColor,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
