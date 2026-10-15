package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.PreferencesManager
import com.example.data.model.*
import com.example.data.notification.ExpiryNotificationHelper
import com.example.data.repository.DocumentRepository
import com.example.data.scanner.DateOcrScanner
import com.example.data.scanner.ExtractedDocumentData
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ExpiryStats(
    val total: Int = 0,
    val expired: Int = 0,
    val critical: Int = 0,
    val upcoming: Int = 0,
    val safe: Int = 0
)

data class ExpiryUiState(
    val documents: List<DocumentItem> = emptyList(),
    val filteredDocuments: List<DocumentItem> = emptyList(),
    val stats: ExpiryStats = ExpiryStats(),
    val searchQuery: String = "",
    val selectedCategory: DocumentCategory? = null,
    val selectedStatus: ExpiryStatus? = null,
    val isPrivacyRedactActive: Boolean = true,
    val isPinEnabled: Boolean = false,
    val isVaultLocked: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val isPremiumActive: Boolean = false,
    val ocrData: ExtractedDocumentData? = null,
    val userMessage: String? = null
)

class ExpiryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: DocumentRepository
    private val prefsManager: PreferencesManager = PreferencesManager(application)

    private val _searchQuery = MutableStateFlow("")
    private val _selectedCategory = MutableStateFlow<DocumentCategory?>(null)
    private val _selectedStatus = MutableStateFlow<ExpiryStatus?>(null)
    private val _ocrData = MutableStateFlow<ExtractedDocumentData?>(null)
    private val _userMessage = MutableStateFlow<String?>(null)
    private val _isVaultLocked = MutableStateFlow(false)
    private var storedPin = "1234"

    init {
        val db = AppDatabase.getDatabase(application)
        repository = DocumentRepository(db.documentDao())
        ExpiryNotificationHelper.createNotificationChannel(application)

        // Seed initial data if first launch
        viewModelScope.launch {
            prefsManager.isInitialSeeded.first().let { seeded ->
                if (!seeded && repository.getCount() == 0) {
                    repository.seedInitialDemoDocuments()
                    prefsManager.setInitialSeeded(true)
                }
            }
        }

        // Listen to PIN lock
        viewModelScope.launch {
            prefsManager.isPinEnabled.collect { enabled ->
                if (enabled) {
                    _isVaultLocked.value = true
                }
            }
        }

        viewModelScope.launch {
            prefsManager.pinCode.collect { code ->
                storedPin = code
            }
        }
    }

    private data class FilterState(
        val query: String = "",
        val category: DocumentCategory? = null,
        val status: ExpiryStatus? = null
    )

    private data class SecurityState(
        val redact: Boolean = true,
        val pinEnabled: Boolean = false,
        val vaultLocked: Boolean = false,
        val notifs: Boolean = true,
        val premium: Boolean = false
    )

    private val filterFlow = combine(
        _searchQuery,
        _selectedCategory,
        _selectedStatus
    ) { query, category, status ->
        FilterState(query, category, status)
    }

    private val securityFlow = combine(
        prefsManager.redactSensitive,
        prefsManager.isPinEnabled,
        _isVaultLocked,
        prefsManager.notificationsEnabled,
        prefsManager.isPremiumActive
    ) { redact, pinEnabled, vaultLocked, notifs, premium ->
        SecurityState(redact, pinEnabled, vaultLocked, notifs, premium)
    }

    val uiState: StateFlow<ExpiryUiState> = combine(
        repository.allActiveDocuments,
        filterFlow,
        securityFlow,
        _ocrData,
        _userMessage
    ) { rawDocs, filter, sec, ocr, msg ->
        val total = rawDocs.size
        val expired = rawDocs.count { it.status == ExpiryStatus.EXPIRED }
        val critical = rawDocs.count { it.status == ExpiryStatus.CRITICAL }
        val upcoming = rawDocs.count { it.status == ExpiryStatus.UPCOMING }
        val safe = rawDocs.count { it.status == ExpiryStatus.ACTIVE }

        val filtered = rawDocs.filter { doc ->
            val matchesQuery = filter.query.isBlank() ||
                    doc.title.contains(filter.query, ignoreCase = true) ||
                    doc.documentNumber.contains(filter.query, ignoreCase = true) ||
                    doc.issuingAuthority.contains(filter.query, ignoreCase = true) ||
                    doc.category.displayName.contains(filter.query, ignoreCase = true) ||
                    doc.personTag.contains(filter.query, ignoreCase = true)

            val matchesCategory = filter.category == null || doc.category == filter.category
            val matchesStatus = filter.status == null || doc.status == filter.status

            matchesQuery && matchesCategory && matchesStatus
        }

        ExpiryUiState(
            documents = rawDocs,
            filteredDocuments = filtered,
            stats = ExpiryStats(total, expired, critical, upcoming, safe),
            searchQuery = filter.query,
            selectedCategory = filter.category,
            selectedStatus = filter.status,
            isPrivacyRedactActive = sec.redact,
            isPinEnabled = sec.pinEnabled,
            isVaultLocked = sec.pinEnabled && sec.vaultLocked,
            notificationsEnabled = sec.notifs,
            isPremiumActive = sec.premium,
            ocrData = ocr,
            userMessage = msg
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ExpiryUiState()
    )

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(category: DocumentCategory?) {
        _selectedCategory.value = category
    }

    fun setSelectedStatus(status: ExpiryStatus?) {
        _selectedStatus.value = status
    }

    fun saveDocument(document: DocumentEntity, onComplete: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = if (document.id == 0L) {
                repository.insert(document)
            } else {
                repository.update(document)
                document.id
            }
            _userMessage.value = "Document saved securely."
            onComplete(id)
        }
    }

    fun deleteDocument(id: Long, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.delete(id)
            _userMessage.value = "Document deleted."
            onComplete()
        }
    }

    fun togglePin(id: Long) {
        viewModelScope.launch {
            repository.togglePin(id)
        }
    }

    fun toggleChecklistItem(id: Long, index: Int) {
        viewModelScope.launch {
            repository.toggleChecklistItem(id, index)
        }
    }

    fun processOcrText(text: String) {
        val extracted = DateOcrScanner.scanTextForDocumentData(text)
        _ocrData.value = extracted
    }

    fun clearOcrData() {
        _ocrData.value = null
    }

    fun togglePrivacyRedact(enabled: Boolean) {
        viewModelScope.launch {
            prefsManager.setRedactSensitive(enabled)
            _userMessage.value = if (enabled) "Document numbers hidden on dashboard" else "Document numbers revealed"
        }
    }

    fun setPinProtection(enabled: Boolean, pin: String) {
        viewModelScope.launch {
            prefsManager.setPinLock(enabled, pin)
            storedPin = pin
            _isVaultLocked.value = enabled
            _userMessage.value = if (enabled) "Vault PIN Lock enabled." else "Vault PIN Lock disabled."
        }
    }

    fun unlockVaultWithPin(pin: String): Boolean {
        return if (pin == storedPin) {
            _isVaultLocked.value = false
            true
        } else {
            false
        }
    }

    fun lockVaultNow() {
        if (uiState.value.isPinEnabled) {
            _isVaultLocked.value = true
        }
    }

    fun toggleNotifications(enabled: Boolean) {
        viewModelScope.launch {
            prefsManager.setNotificationsEnabled(enabled)
            _userMessage.value = if (enabled) "Expiry notifications active" else "Notifications paused"
        }
    }

    fun sendTestNotification(context: Context) {
        val urgent = uiState.value.documents.firstOrNull { it.status == ExpiryStatus.CRITICAL }
            ?: uiState.value.documents.firstOrNull()

        if (urgent != null) {
            ExpiryNotificationHelper.sendDocumentAlert(context, urgent)
        } else {
            ExpiryNotificationHelper.sendTestNotification(context, "US Passport", 30)
        }
        _userMessage.value = "Test notification sent! Check notification bar."
    }

    fun togglePremiumSimulation() {
        viewModelScope.launch {
            val next = !uiState.value.isPremiumActive
            prefsManager.setPremiumActive(next)
            _userMessage.value = if (next) "Premium Plan simulated: Unlimited docs & OCR enabled" else "Free Plan active (Max 5 docs)"
        }
    }

    suspend fun exportBackupJson(): String {
        return repository.exportBackupJson()
    }

    fun importBackupJson(json: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val res = repository.importBackupJson(json)
            if (res.isSuccess) {
                _userMessage.value = "Imported ${res.getOrNull()} documents successfully."
                onResult(true, "Successfully restored ${res.getOrNull()} documents.")
            } else {
                _userMessage.value = "Import failed: Invalid JSON format."
                onResult(false, "Failed to import. Check format.")
            }
        }
    }

    fun seedDemoData() {
        viewModelScope.launch {
            repository.seedInitialDemoDocuments()
            _userMessage.value = "Demo documents reloaded!"
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAll()
            _userMessage.value = "All documents wiped from local vault."
        }
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    fun getDocumentById(id: Long): Flow<DocumentItem?> {
        return repository.getDocument(id)
    }
}
