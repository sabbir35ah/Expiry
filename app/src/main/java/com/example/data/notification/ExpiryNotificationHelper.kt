package com.example.data.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.data.model.DocumentItem

object ExpiryNotificationHelper {

    const val CHANNEL_ID = "expiry_alerts_channel"
    private const val CHANNEL_NAME = "Document Expiry Alerts"
    private const val CHANNEL_DESC = "Notifications for upcoming and due document expiration deadlines."

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
                enableVibration(true)
                setShowBadge(true)
            }
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun sendTestNotification(context: Context, docName: String = "US Passport", daysLeft: Int = 30) {
        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            1001,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Expiry Alert: $docName")
            .setContentText("Your document expires in $daysLeft days. Tap to review renewal steps and checklist.")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Urgent renewal required for $docName. Most international destinations enforce the 6-month validity rule. Tap to view official renewal portal.")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            val manager = NotificationManagerCompat.from(context)
            manager.notify(1001, notification)
        } catch (_: SecurityException) {
        }
    }

    fun sendDocumentAlert(context: Context, item: DocumentItem) {
        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            item.id.toInt(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val days = item.daysRemaining
        val statusText = when {
            days < 0 -> "has expired! Immediate action required."
            days == 0L -> "expires TODAY!"
            days == 1L -> "expires tomorrow!"
            else -> "expires in $days days."
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Expiry Reminder: ${item.title}")
            .setContentText("${item.category.displayName} $statusText")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("${item.title} (${item.category.displayName}) $statusText\n\nOfficial Renewal: ${item.officialUrl.ifBlank { "Check Renewal Hub" }}")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            val manager = NotificationManagerCompat.from(context)
            manager.notify(item.id.toInt(), notification)
        } catch (_: SecurityException) {
        }
    }
}
