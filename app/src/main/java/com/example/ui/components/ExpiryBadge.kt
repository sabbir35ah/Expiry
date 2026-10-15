package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ExpiryStatus
import com.example.ui.theme.*

@Composable
fun ExpiryBadge(
    status: ExpiryStatus,
    daysRemaining: Long,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, icon) = when (status) {
        ExpiryStatus.EXPIRED -> Triple(StatusExpiredContainer, StatusExpired, Icons.Filled.Error)
        ExpiryStatus.CRITICAL -> Triple(StatusCriticalContainer, StatusCritical, Icons.Filled.Warning)
        ExpiryStatus.UPCOMING -> Triple(StatusUpcomingContainer, StatusUpcoming, Icons.Filled.Warning)
        ExpiryStatus.ACTIVE -> Triple(StatusActiveContainer, StatusActive, Icons.Filled.CheckCircle)
    }

    val label = when (status) {
        ExpiryStatus.EXPIRED -> if (daysRemaining < -1) "Expired ${-daysRemaining}d ago" else "Expired"
        ExpiryStatus.CRITICAL -> if (daysRemaining == 0L) "Expires TODAY!" else "Due in ${daysRemaining}d"
        ExpiryStatus.UPCOMING -> "Due in ${daysRemaining}d"
        ExpiryStatus.ACTIVE -> if (daysRemaining > 365) "${daysRemaining / 365}y left" else "${daysRemaining / 30}mo left"
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = textColor,
            modifier = Modifier.size(12.dp)
        )
        Text(
            text = label,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    }
}
