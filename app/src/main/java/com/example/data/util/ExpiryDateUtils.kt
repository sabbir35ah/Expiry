package com.example.data.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object ExpiryDateUtils {

    private val displayFormat = SimpleDateFormat("MMM d, yyyy", Locale.US)
    private val isoFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val slashFormat = SimpleDateFormat("MM/dd/yyyy", Locale.US)
    private val euroSlashFormat = SimpleDateFormat("dd/MM/yyyy", Locale.US)
    private val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.US)

    fun formatDisplayDate(epochMillis: Long): String {
        return try {
            displayFormat.format(Date(epochMillis))
        } catch (e: Exception) {
            "Invalid date"
        }
    }

    fun formatMonthYear(epochMillis: Long): String {
        return try {
            monthYearFormat.format(Date(epochMillis))
        } catch (e: Exception) {
            "Unknown"
        }
    }

    fun formatIsoDate(epochMillis: Long): String {
        return try {
            isoFormat.format(Date(epochMillis))
        } catch (e: Exception) {
            ""
        }
    }

    fun parseDate(text: String): Long? {
        val trimmed = text.trim()
        val formats = listOf(
            isoFormat,
            euroSlashFormat,
            slashFormat,
            SimpleDateFormat("d MMM yyyy", Locale.US),
            SimpleDateFormat("MMM d, yyyy", Locale.US),
            SimpleDateFormat("dd-MM-yyyy", Locale.US),
            SimpleDateFormat("yyyy/MM/dd", Locale.US)
        )
        for (format in formats) {
            try {
                format.isLenient = false
                val date = format.parse(trimmed)
                if (date != null) {
                    return date.time
                }
            } catch (_: Exception) {
            }
        }
        return null
    }

    fun getStartOfTodayMillis(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun calculateDaysRemaining(targetEpochMillis: Long): Long {
        val today = getStartOfTodayMillis()
        val targetCal = Calendar.getInstance().apply {
            timeInMillis = targetEpochMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val diffMillis = targetCal.timeInMillis - today
        return TimeUnit.MILLISECONDS.toDays(diffMillis)
    }

    fun getRelativeStatusText(daysRemaining: Long): String {
        return when {
            daysRemaining < -365 -> "Expired ${-daysRemaining / 365} years ago"
            daysRemaining < -30 -> "Expired ${-daysRemaining / 30} months ago"
            daysRemaining < -1 -> "Expired ${-daysRemaining} days ago"
            daysRemaining == -1L -> "Expired yesterday"
            daysRemaining == 0L -> "Expires today!"
            daysRemaining == 1L -> "Expires tomorrow"
            daysRemaining < 30 -> "Expires in $daysRemaining days"
            daysRemaining < 60 -> "Expires in 1 month (${daysRemaining}d)"
            daysRemaining < 365 -> "Expires in ${daysRemaining / 30} months"
            else -> {
                val years = daysRemaining / 365
                val months = (daysRemaining % 365) / 30
                if (months > 0) "Expires in $years yr $months mo" else "Expires in $years years"
            }
        }
    }

    fun calculatePercentElapsed(issueDateMillis: Long?, expiryDateMillis: Long): Float {
        if (issueDateMillis == null || issueDateMillis >= expiryDateMillis) {
            val days = calculateDaysRemaining(expiryDateMillis)
            return if (days <= 0) 1f else 0.5f
        }
        val now = System.currentTimeMillis()
        if (now <= issueDateMillis) return 0f
        if (now >= expiryDateMillis) return 1f
        val total = (expiryDateMillis - issueDateMillis).toFloat()
        val elapsed = (now - issueDateMillis).toFloat()
        return (elapsed / total).coerceIn(0f, 1f)
    }

    fun dateAfterDays(days: Int): Long {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, days)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
}
