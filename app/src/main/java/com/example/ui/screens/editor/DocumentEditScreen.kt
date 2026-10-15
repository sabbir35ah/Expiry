package com.example.ui.screens.editor

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.DocumentCategory
import com.example.data.model.DocumentEntity
import com.example.data.util.ExpiryDateUtils
import com.example.ui.theme.*
import com.example.ui.viewmodel.ExpiryViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentEditScreen(
    docId: Long,
    viewModel: ExpiryViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToScanner: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var title by remember { mutableStateOf("") }
    var categoryKey by remember { mutableStateOf(DocumentCategory.PASSPORT.key) }
    var documentNumber by remember { mutableStateOf("") }
    var issuingAuthority by remember { mutableStateOf("") }
    var expiryDateMillis by remember { mutableStateOf(ExpiryDateUtils.dateAfterDays(365)) }
    var issueDateMillis by remember { mutableStateOf<Long?>(null) }
    var renewalNotes by remember { mutableStateOf("") }
    var officialUrl by remember { mutableStateOf("") }
    var personTag by remember { mutableStateOf("Self") }
    var selectedIntervals by remember { mutableStateOf(setOf(180, 90, 30, 7, 1)) }
    var isInitialized by remember { mutableStateOf(false) }

    // If editing existing document, load its data
    LaunchedEffect(docId) {
        if (docId > 0) {
            val existing = uiState.documents.firstOrNull { it.id == docId }
            if (existing != null) {
                title = existing.title
                categoryKey = existing.category.key
                documentNumber = existing.documentNumber
                issuingAuthority = existing.issuingAuthority
                expiryDateMillis = existing.expiryDateMillis
                issueDateMillis = existing.issueDateMillis
                renewalNotes = existing.renewalNotes
                officialUrl = existing.officialUrl
                personTag = existing.personTag
                selectedIntervals = existing.reminderIntervals.toSet()
            }
        }
        isInitialized = true
    }

    // If OCR extracted data exists, populate with user verification banner!
    LaunchedEffect(uiState.ocrData) {
        uiState.ocrData?.let { ocr ->
            ocr.detectedExpiryDateMillis?.let { expiryDateMillis = it }
            ocr.detectedIssueDateMillis?.let { issueDateMillis = it }
            ocr.detectedDocNumber?.let { documentNumber = it }
            ocr.detectedAuthority?.let { issuingAuthority = it }
            ocr.detectedDocumentType?.let { typeStr ->
                val matched = DocumentCategory.entries.firstOrNull { it.displayName.contains(typeStr, ignoreCase = true) }
                if (matched != null) {
                    categoryKey = matched.key
                    if (officialUrl.isBlank()) officialUrl = matched.suggestedOfficialUrl
                }
            }
        }
    }

    Scaffold(
        modifier = modifier.testTag("document_edit_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (docId > 0) "Edit Document" else "New Document",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = onNavigateToScanner,
                        modifier = Modifier.testTag("btn_ocr_assistant")
                    ) {
                        Icon(
                            Icons.Filled.DocumentScanner,
                            contentDescription = "Scan Document",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // OCR Verification Card (If active OCR scan was applied)
            if (uiState.ocrData != null) {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "On-Device OCR Extracted Data",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Fields pre-populated. Review and edit before saving.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                        IconButton(onClick = { viewModel.clearOcrData() }) {
                            Icon(Icons.Filled.Close, contentDescription = "Dismiss")
                        }
                    }
                }
            }

            // Quick Category Selector
            Text(
                text = "Document Category",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(DocumentCategory.entries) { cat ->
                    FilterChip(
                        selected = categoryKey == cat.key,
                        onClick = {
                            categoryKey = cat.key
                            if (officialUrl.isBlank()) {
                                officialUrl = cat.suggestedOfficialUrl
                            }
                        },
                        label = { Text(cat.displayName) },
                        modifier = Modifier.testTag("cat_chip_${cat.key}")
                    )
                }
            }

            // Document Title
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Document Title *") },
                placeholder = { Text("e.g. US Passport, CA Driver's License") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_title"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Document Number & Issuing Authority
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = documentNumber,
                    onValueChange = { documentNumber = it },
                    label = { Text("Document ID / Number") },
                    placeholder = { Text("P98234190") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("input_doc_number"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = issuingAuthority,
                    onValueChange = { issuingAuthority = it },
                    label = { Text("Issuing Authority") },
                    placeholder = { Text("e.g. State Dept, DMV") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("input_authority"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Expiration Date Picker Field
            OutlinedCard(
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val cal = Calendar.getInstance().apply { timeInMillis = expiryDateMillis }
                        DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                val selected = Calendar.getInstance().apply {
                                    set(year, month, dayOfMonth, 0, 0, 0)
                                }
                                expiryDateMillis = selected.timeInMillis
                            },
                            cal.get(Calendar.YEAR),
                            cal.get(Calendar.MONTH),
                            cal.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    }
                    .testTag("picker_expiry_date")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Expiration Date *",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = ExpiryDateUtils.formatDisplayDate(expiryDateMillis),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = ExpiryDateUtils.getRelativeStatusText(ExpiryDateUtils.calculateDaysRemaining(expiryDateMillis)),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Icon(
                        imageVector = Icons.Filled.CalendarMonth,
                        contentDescription = "Pick Date",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Issue Date Picker (Optional)
            OutlinedCard(
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val cal = Calendar.getInstance().apply {
                            timeInMillis = issueDateMillis ?: System.currentTimeMillis()
                        }
                        DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                val selected = Calendar.getInstance().apply {
                                    set(year, month, dayOfMonth, 0, 0, 0)
                                }
                                issueDateMillis = selected.timeInMillis
                            },
                            cal.get(Calendar.YEAR),
                            cal.get(Calendar.MONTH),
                            cal.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    }
                    .testTag("picker_issue_date")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Issue Date (Optional)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (issueDateMillis != null) ExpiryDateUtils.formatDisplayDate(issueDateMillis!!) else "Tap to set issue date",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    if (issueDateMillis != null) {
                        IconButton(onClick = { issueDateMillis = null }) {
                            Icon(Icons.Filled.Clear, contentDescription = "Clear")
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Filled.DateRange,
                            contentDescription = "Set Date",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Person / Profile Tag
            Text(
                text = "Owner / Tag",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Self", "Spouse", "Family", "Work", "Vehicle").forEach { tag ->
                    FilterChip(
                        selected = personTag == tag,
                        onClick = { personTag = tag },
                        label = { Text(tag) }
                    )
                }
            }

            // Reminder Intervals (PRD: six months, three months, 30 days, seven days)
            Text(
                text = "Configurable Reminder Intervals",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )

            val availableIntervals = listOf(
                180 to "6 Months",
                90 to "3 Months",
                30 to "30 Days",
                14 to "14 Days",
                7 to "7 Days",
                1 to "1 Day"
            )

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                availableIntervals.chunked(3).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        row.forEach { (days, label) ->
                            FilterChip(
                                selected = selectedIntervals.contains(days),
                                onClick = {
                                    selectedIntervals = if (selectedIntervals.contains(days)) {
                                        selectedIntervals - days
                                    } else {
                                        selectedIntervals + days
                                    }
                                },
                                label = { Text(label) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Official Renewal Link
            OutlinedTextField(
                value = officialUrl,
                onValueChange = { officialUrl = it },
                label = { Text("Official Renewal Portal URL") },
                placeholder = { Text("https://travel.state.gov or dmv...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_official_url"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Renewal Notes
            OutlinedTextField(
                value = renewalNotes,
                onValueChange = { renewalNotes = it },
                label = { Text("Renewal Notes & Prerequisites") },
                placeholder = { Text("e.g. Requires 2 passport photos, processing takes 6-8 weeks...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .testTag("input_notes"),
                shape = RoundedCornerShape(12.dp)
            )

            // Save Button
            Button(
                onClick = {
                    val finalTitle = title.ifBlank {
                        val cat = DocumentCategory.fromKey(categoryKey)
                        cat.displayName
                    }
                    val entity = DocumentEntity(
                        id = if (docId > 0) docId else 0L,
                        title = finalTitle,
                        categoryKey = categoryKey,
                        documentNumber = documentNumber.trim(),
                        issuingAuthority = issuingAuthority.trim(),
                        expiryDateMillis = expiryDateMillis,
                        issueDateMillis = issueDateMillis,
                        renewalNotes = renewalNotes.trim(),
                        officialUrl = officialUrl.trim(),
                        reminderIntervalsCsv = selectedIntervals.sortedDescending().joinToString(","),
                        personTag = personTag,
                        updatedAtMillis = System.currentTimeMillis()
                    )
                    viewModel.saveDocument(entity) {
                        viewModel.clearOcrData()
                        onNavigateBack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("btn_save_document"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (docId > 0) "Update Document" else "Save to Vault",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
