package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DocumentCategory
import com.example.data.model.DocumentItem
import com.example.data.model.ExpiryStatus
import com.example.data.util.ExpiryDateUtils

@Composable
fun DocumentCard(
    document: DocumentItem,
    isRedactActive: Boolean,
    onClick: () -> Unit,
    onTogglePin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val category = document.category
    val status = document.status

    val borderColor = when (status) {
        ExpiryStatus.EXPIRED -> MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
        ExpiryStatus.CRITICAL -> Color(0xFFEA580C).copy(alpha = 0.5f)
        else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("document_card_${document.id}")
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(borderColor)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row: Category Badge & Pin & Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(category.color.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (category) {
                                DocumentCategory.PASSPORT -> Icons.Filled.Public
                                DocumentCategory.VISA -> Icons.Filled.FlightTakeoff
                                DocumentCategory.DRIVING_LICENSE -> Icons.Filled.DirectionsCar
                                DocumentCategory.NATIONAL_ID -> Icons.Filled.Badge
                                DocumentCategory.INSURANCE -> Icons.Filled.HealthAndSafety
                                DocumentCategory.CERTIFICATION -> Icons.Filled.School
                                DocumentCategory.WARRANTY -> Icons.Filled.Verified
                                DocumentCategory.VEHICLE -> Icons.Filled.Commute
                                DocumentCategory.CONTRACT -> Icons.Filled.Description
                                DocumentCategory.MEMBERSHIP -> Icons.Filled.Loyalty
                                DocumentCategory.OTHER -> Icons.Filled.Folder
                            },
                            contentDescription = category.displayName,
                            tint = category.color,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = category.displayName,
                            style = MaterialTheme.typography.labelSmall,
                            color = category.color,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = document.personTag,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ExpiryBadge(status = status, daysRemaining = document.daysRemaining)

                    IconButton(
                        onClick = onTogglePin,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("pin_button_${document.id}")
                    ) {
                        Icon(
                            imageVector = if (document.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                            contentDescription = if (document.isPinned) "Unpin" else "Pin",
                            tint = if (document.isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Document Title
            Text(
                text = document.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Document Number / ID (Masked or Unmasked)
            if (document.documentNumber.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = document.getMaskedDocumentNumber(isRedactActive),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Validity progress bar
            val progressColor = when (status) {
                ExpiryStatus.EXPIRED -> MaterialTheme.colorScheme.error
                ExpiryStatus.CRITICAL -> Color(0xFFEA580C)
                ExpiryStatus.UPCOMING -> Color(0xFFD97706)
                ExpiryStatus.ACTIVE -> MaterialTheme.colorScheme.primary
            }

            LinearProgressIndicator(
                progress = { document.percentElapsed },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = progressColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Expiry Date footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Expires: ${ExpiryDateUtils.formatDisplayDate(document.expiryDateMillis)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = document.relativeStatusText,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = when (status) {
                        ExpiryStatus.EXPIRED -> MaterialTheme.colorScheme.error
                        ExpiryStatus.CRITICAL -> Color(0xFFEA580C)
                        ExpiryStatus.UPCOMING -> Color(0xFFD97706)
                        ExpiryStatus.ACTIVE -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}
