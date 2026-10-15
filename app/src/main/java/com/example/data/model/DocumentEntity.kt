package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.util.ExpiryDateUtils

@Entity(tableName = "documents")
data class DocumentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val categoryKey: String,
    val documentNumber: String = "",
    val issuingAuthority: String = "",
    val expiryDateMillis: Long,
    val issueDateMillis: Long? = null,
    val imageUri: String? = null,
    val renewalNotes: String = "",
    val officialUrl: String = "",
    val reminderIntervalsCsv: String = "180,90,30,7,1",
    val personTag: String = "Self",
    val isPinned: Boolean = false,
    val isArchived: Boolean = false,
    val completedChecklistCsv: String = "",
    val createdAtMillis: Long = System.currentTimeMillis(),
    val updatedAtMillis: Long = System.currentTimeMillis()
)

data class DocumentItem(
    val entity: DocumentEntity,
    val category: DocumentCategory = DocumentCategory.fromKey(entity.categoryKey),
    val daysRemaining: Long = ExpiryDateUtils.calculateDaysRemaining(entity.expiryDateMillis),
    val percentElapsed: Float = ExpiryDateUtils.calculatePercentElapsed(
        entity.issueDateMillis,
        entity.expiryDateMillis
    )
) {
    val id: Long get() = entity.id
    val title: String get() = entity.title
    val documentNumber: String get() = entity.documentNumber
    val issuingAuthority: String get() = entity.issuingAuthority
    val expiryDateMillis: Long get() = entity.expiryDateMillis
    val issueDateMillis: Long? get() = entity.issueDateMillis
    val imageUri: String? get() = entity.imageUri
    val renewalNotes: String get() = entity.renewalNotes
    val officialUrl: String get() = entity.officialUrl.ifBlank { category.suggestedOfficialUrl }
    val personTag: String get() = entity.personTag
    val isPinned: Boolean get() = entity.isPinned
    val isArchived: Boolean get() = entity.isArchived

    val status: ExpiryStatus
        get() = when {
            daysRemaining < 0 -> ExpiryStatus.EXPIRED
            daysRemaining <= 30 -> ExpiryStatus.CRITICAL
            daysRemaining <= 90 -> ExpiryStatus.UPCOMING
            else -> ExpiryStatus.ACTIVE
        }

    val relativeStatusText: String
        get() = ExpiryDateUtils.getRelativeStatusText(daysRemaining)

    val reminderIntervals: List<Int>
        get() = entity.reminderIntervalsCsv
            .split(",")
            .mapNotNull { it.trim().toIntOrNull() }
            .sortedDescending()

    val completedChecklistIndices: Set<Int>
        get() = entity.completedChecklistCsv
            .split(",")
            .mapNotNull { it.trim().toIntOrNull() }
            .toSet()

    fun getMaskedDocumentNumber(redact: Boolean = true): String {
        if (!redact || documentNumber.isBlank()) return documentNumber
        val clean = documentNumber.trim()
        return if (clean.length <= 4) {
            "••••"
        } else {
            val last4 = clean.takeLast(4)
            val prefixCount = (clean.length - 4).coerceAtMost(6)
            "•".repeat(prefixCount) + " " + last4
        }
    }
}
