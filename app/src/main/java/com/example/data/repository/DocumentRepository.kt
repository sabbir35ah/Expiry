package com.example.data.repository

import com.example.data.local.DocumentDao
import com.example.data.model.DocumentEntity
import com.example.data.model.DocumentItem
import com.example.data.util.ExpiryDateUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

class DocumentRepository(
    private val documentDao: DocumentDao
) {

    val allActiveDocuments: Flow<List<DocumentItem>> = documentDao.getAllActiveDocuments().map { list ->
        list.map { DocumentItem(it) }
    }

    val allDocuments: Flow<List<DocumentItem>> = documentDao.getAllDocuments().map { list ->
        list.map { DocumentItem(it) }
    }

    fun getDocument(id: Long): Flow<DocumentItem?> = documentDao.getDocumentById(id).map { entity ->
        entity?.let { DocumentItem(it) }
    }

    suspend fun insert(document: DocumentEntity): Long = documentDao.insert(document)

    suspend fun update(document: DocumentEntity) = documentDao.update(document)

    suspend fun delete(id: Long) = documentDao.deleteById(id)

    suspend fun togglePin(id: Long) {
        val existing = documentDao.getDocumentByIdDirect(id) ?: return
        documentDao.update(existing.copy(isPinned = !existing.isPinned, updatedAtMillis = System.currentTimeMillis()))
    }

    suspend fun toggleChecklistItem(id: Long, itemIndex: Int) {
        val existing = documentDao.getDocumentByIdDirect(id) ?: return
        val currentSet = existing.completedChecklistCsv
            .split(",")
            .mapNotNull { it.trim().toIntOrNull() }
            .toMutableSet()

        if (currentSet.contains(itemIndex)) {
            currentSet.remove(itemIndex)
        } else {
            currentSet.add(itemIndex)
        }

        val updatedCsv = currentSet.sorted().joinToString(",")
        documentDao.update(existing.copy(completedChecklistCsv = updatedCsv, updatedAtMillis = System.currentTimeMillis()))
    }

    suspend fun exportBackupJson(): String {
        val list = documentDao.getAllSnapshot()
        val jsonArray = JSONArray()
        for (item in list) {
            val obj = JSONObject().apply {
                put("id", item.id)
                put("title", item.title)
                put("categoryKey", item.categoryKey)
                put("documentNumber", item.documentNumber)
                put("issuingAuthority", item.issuingAuthority)
                put("expiryDateMillis", item.expiryDateMillis)
                put("issueDateMillis", item.issueDateMillis ?: JSONObject.NULL)
                put("imageUri", item.imageUri ?: JSONObject.NULL)
                put("renewalNotes", item.renewalNotes)
                put("officialUrl", item.officialUrl)
                put("reminderIntervalsCsv", item.reminderIntervalsCsv)
                put("personTag", item.personTag)
                put("isPinned", item.isPinned)
                put("isArchived", item.isArchived)
                put("completedChecklistCsv", item.completedChecklistCsv)
                put("createdAtMillis", item.createdAtMillis)
                put("updatedAtMillis", item.updatedAtMillis)
            }
            jsonArray.put(obj)
        }
        val wrapper = JSONObject().apply {
            put("app", "EXPIRY")
            put("version", "1.0")
            put("exportTimestamp", System.currentTimeMillis())
            put("exportDateReadable", ExpiryDateUtils.formatDisplayDate(System.currentTimeMillis()))
            put("documentCount", list.size)
            put("documents", jsonArray)
        }
        return wrapper.toString(2)
    }

    suspend fun importBackupJson(jsonString: String): Result<Int> {
        return try {
            val wrapper = JSONObject(jsonString)
            val jsonArray = wrapper.getJSONArray("documents")
            val entities = mutableListOf<DocumentEntity>()

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                entities.add(
                    DocumentEntity(
                        title = obj.getString("title"),
                        categoryKey = obj.optString("categoryKey", "other"),
                        documentNumber = obj.optString("documentNumber", ""),
                        issuingAuthority = obj.optString("issuingAuthority", ""),
                        expiryDateMillis = obj.getLong("expiryDateMillis"),
                        issueDateMillis = if (obj.isNull("issueDateMillis")) null else obj.getLong("issueDateMillis"),
                        imageUri = if (obj.isNull("imageUri")) null else obj.getString("imageUri"),
                        renewalNotes = obj.optString("renewalNotes", ""),
                        officialUrl = obj.optString("officialUrl", ""),
                        reminderIntervalsCsv = obj.optString("reminderIntervalsCsv", "180,90,30,7,1"),
                        personTag = obj.optString("personTag", "Self"),
                        isPinned = obj.optBoolean("isPinned", false),
                        isArchived = obj.optBoolean("isArchived", false),
                        completedChecklistCsv = obj.optString("completedChecklistCsv", ""),
                        createdAtMillis = obj.optLong("createdAtMillis", System.currentTimeMillis()),
                        updatedAtMillis = System.currentTimeMillis()
                    )
                )
            }

            documentDao.insertAll(entities)
            Result.success(entities.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun clearAll() {
        documentDao.deleteAll()
    }

    suspend fun getCount(): Int = documentDao.getCount()

    suspend fun seedInitialDemoDocuments() {
        val now = System.currentTimeMillis()
        val demoItems = listOf(
            DocumentEntity(
                title = "US Passport (Biometric)",
                categoryKey = "passport",
                documentNumber = "P739201948",
                issuingAuthority = "US Department of State",
                expiryDateMillis = ExpiryDateUtils.dateAfterDays(38), // 38 days left - Urgent!
                issueDateMillis = ExpiryDateUtils.dateAfterDays(-3600),
                renewalNotes = "Remember to order DS-82 photo packet. Most airlines block check-in if within 6 months validity.",
                officialUrl = "https://travel.state.gov/content/travel/en/passports.html",
                reminderIntervalsCsv = "180,90,30,14,7,1",
                personTag = "Self",
                isPinned = true,
                completedChecklistCsv = "0,1"
            ),
            DocumentEntity(
                title = "California Driver License REAL ID",
                categoryKey = "driving_license",
                documentNumber = "DL-C8492015",
                issuingAuthority = "California DMV",
                expiryDateMillis = ExpiryDateUtils.dateAfterDays(14), // 14 days left - Critical!
                issueDateMillis = ExpiryDateUtils.dateAfterDays(-1810),
                renewalNotes = "Vision test required at local DMV branch. Appointment booked.",
                officialUrl = "https://www.dmv.ca.gov/portal/driver-licenses-id-cards/",
                reminderIntervalsCsv = "90,60,30,14,7,1",
                personTag = "Self",
                isPinned = true,
                completedChecklistCsv = "0"
            ),
            DocumentEntity(
                title = "Schengen Multi-Entry Tourist Visa",
                categoryKey = "visa",
                documentNumber = "DEU982341",
                issuingAuthority = "German Consulate General",
                expiryDateMillis = ExpiryDateUtils.dateAfterDays(-5), // Expired 5 days ago!
                issueDateMillis = ExpiryDateUtils.dateAfterDays(-180),
                renewalNotes = "Expired. Need to submit new appointment at VFS Global center before next European conference.",
                officialUrl = "https://home-affairs.ec.europa.eu/policies/schengen-borders-and-visa_en",
                reminderIntervalsCsv = "60,30,14,7,1",
                personTag = "Self",
                isPinned = false,
                completedChecklistCsv = ""
            ),
            DocumentEntity(
                title = "MacBook Pro M3 AppleCare+",
                categoryKey = "warranty",
                documentNumber = "AC-992384910",
                issuingAuthority = "Apple Inc.",
                expiryDateMillis = ExpiryDateUtils.dateAfterDays(125), // Active safe
                issueDateMillis = ExpiryDateUtils.dateAfterDays(-600),
                renewalNotes = "Inspect battery health (currently 94%). Screen coating and keyboard covered under AppleCare.",
                officialUrl = "https://checkcoverage.apple.com/",
                reminderIntervalsCsv = "60,30,7",
                personTag = "Work",
                isPinned = false,
                completedChecklistCsv = ""
            ),
            DocumentEntity(
                title = "Auto Insurance Policy (Comprehensive)",
                categoryKey = "insurance",
                documentNumber = "GEI-84920199",
                issuingAuthority = "Geico Insurance",
                expiryDateMillis = ExpiryDateUtils.dateAfterDays(72), // Upcoming (~2.5 months)
                issueDateMillis = ExpiryDateUtils.dateAfterDays(-110),
                renewalNotes = "Compare with Progressive & State Farm quotes before automatic premium renewal debit.",
                officialUrl = "https://www.geico.com",
                reminderIntervalsCsv = "60,30,14,7",
                personTag = "Family",
                isPinned = false,
                completedChecklistCsv = "0"
            ),
            DocumentEntity(
                title = "Vehicle Smog & Registration",
                categoryKey = "vehicle",
                documentNumber = "REG-7XY1092",
                issuingAuthority = "Bureau of Automotive Repair",
                expiryDateMillis = ExpiryDateUtils.dateAfterDays(25), // Critical!
                issueDateMillis = ExpiryDateUtils.dateAfterDays(-340),
                renewalNotes = "Pass smog check at Star Station on 4th Street prior to paying registration renewal.",
                officialUrl = "https://dmv.ny.gov/registration/how-renew-registration",
                reminderIntervalsCsv = "60,30,14,7,1",
                personTag = "Vehicle",
                isPinned = false,
                completedChecklistCsv = ""
            )
        )
        documentDao.insertAll(demoItems)
    }
}
