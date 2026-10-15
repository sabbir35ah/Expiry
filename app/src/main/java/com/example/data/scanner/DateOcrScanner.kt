package com.example.data.scanner

import com.example.data.util.ExpiryDateUtils
import java.util.regex.Pattern

data class ExtractedDocumentData(
    val detectedExpiryDateMillis: Long? = null,
    val detectedIssueDateMillis: Long? = null,
    val detectedDocNumber: String? = null,
    val detectedAuthority: String? = null,
    val rawExtractedLines: List<String> = emptyList(),
    val confidenceScore: Float = 0.92f,
    val detectedDocumentType: String? = null
)

object DateOcrScanner {

    // Regex for matching dates in various formats
    private val dateRegexes = listOf(
        // YYYY-MM-DD or YYYY/MM/DD
        Pattern.compile("""\b(20[2-4]\d)[-/.](0[1-9]|1[0-2])[-/.](0[1-9]|[12]\d|3[01])\b"""),
        // DD/MM/YYYY or DD-MM-YYYY
        Pattern.compile("""\b(0[1-9]|[12]\d|3[01])[-/.](0[1-9]|1[0-2])[-/.](20[2-4]\d)\b"""),
        // Month name format: 15 OCT 2028 or OCT 15, 2028
        Pattern.compile("""\b(0[1-9]|[12]\d|3[01])\s+(JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC)[A-Z]*\s+(20[2-4]\d)\b""", Pattern.CASE_INSENSITIVE),
        Pattern.compile("""\b(JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC)[A-Z]*\s+(0[1-9]|[12]\d|3[01])(?:,)?\s+(20[2-4]\d)\b""", Pattern.CASE_INSENSITIVE)
    )

    private val expiryKeywords = listOf(
        "EXP", "EXPIRY", "EXPIRES", "EXPIRATION", "VALID UNTIL", "VAL", "VALID TO", "END DATE", "DUE DATE"
    )

    private val issueKeywords = listOf(
        "ISS", "ISSUE", "ISSUED", "VALID FROM", "START DATE", "DATE OF ISSUE"
    )

    fun scanTextForDocumentData(text: String): ExtractedDocumentData {
        val lines = text.lines().map { it.trim() }.filter { it.isNotBlank() }
        var expiryMillis: Long? = null
        var issueMillis: Long? = null
        var docNumber: String? = null
        var authority: String? = null
        var detectedType: String? = null

        val foundDates = mutableListOf<Pair<Long, String>>() // (epochMillis, contextLine)

        for (line in lines) {
            val upper = line.uppercase()

            if (upper.contains("PASSPORT")) detectedType = "Passport"
            else if (upper.contains("DRIVER") || upper.contains("DRIVING") || upper.contains("DL ")) detectedType = "Driving License"
            else if (upper.contains("VISA") || upper.contains("SCHENGEN")) detectedType = "Visa & Residence"
            else if (upper.contains("WARRANTY") || upper.contains("APPLECARE")) detectedType = "Product Warranty"
            else if (upper.contains("INSURANCE")) detectedType = "Insurance Policy"

            // Look for dates
            for (pattern in dateRegexes) {
                val matcher = pattern.matcher(line)
                while (matcher.find()) {
                    val matchStr = matcher.group()
                    val parsed = ExpiryDateUtils.parseDate(matchStr)
                    if (parsed != null) {
                        foundDates.add(Pair(parsed, line))
                    }
                }
            }

            // Look for Document Number
            if (docNumber == null) {
                val docNumPattern = Pattern.compile("""(?:DOC|NO|NUMBER|ID|PASSPORT NO|LIC)\s*[:#.]?\s*([A-Z0-9\-]{6,15})""", Pattern.CASE_INSENSITIVE)
                val matcher = docNumPattern.matcher(line)
                if (matcher.find()) {
                    docNumber = matcher.group(1)
                }
            }

            // Look for Issuing Authority
            if (authority == null) {
                if (upper.contains("STATE DEPARTMENT") || upper.contains("DEPARTMENT OF STATE")) {
                    authority = "US Department of State"
                } else if (upper.contains("DMV") || upper.contains("DEPARTMENT OF MOTOR")) {
                    authority = "Department of Motor Vehicles"
                } else if (upper.contains("GOVERNMENT OF") || upper.contains("MINISTRY")) {
                    authority = line
                } else if (upper.contains("APPLE")) {
                    authority = "Apple Inc."
                }
            }
        }

        // Categorize found dates
        for ((millis, context) in foundDates) {
            val upper = context.uppercase()
            val isExpiry = expiryKeywords.any { upper.contains(it) }
            val isIssue = issueKeywords.any { upper.contains(it) }

            if (isExpiry && expiryMillis == null) {
                expiryMillis = millis
            } else if (isIssue && issueMillis == null) {
                issueMillis = millis
            }
        }

        // Fallback: If not explicitly marked, later date is usually expiry, earlier is issue
        if (expiryMillis == null && foundDates.isNotEmpty()) {
            val sorted = foundDates.map { it.first }.sorted()
            if (sorted.size >= 2) {
                issueMillis = sorted.first()
                expiryMillis = sorted.last()
            } else {
                expiryMillis = sorted.first()
            }
        }

        return ExtractedDocumentData(
            detectedExpiryDateMillis = expiryMillis,
            detectedIssueDateMillis = issueMillis,
            detectedDocNumber = docNumber,
            detectedAuthority = authority,
            rawExtractedLines = lines,
            confidenceScore = if (expiryMillis != null) 0.94f else 0.50f,
            detectedDocumentType = detectedType
        )
    }

    // Realistic pre-configured document presets for rapid zero-friction on-device testing
    val sampleDocumentScans = listOf(
        SampleDocumentPreset(
            title = "US Passport (Biometric)",
            category = "passport",
            authority = "US Department of State",
            docNumber = "P739201948",
            daysFromNow = 42, // Expiring soon!
            sampleOcrText = """
                UNITED STATES OF AMERICA
                PASSPORT / PASSEPORT
                Type: P   Code: USA   Passport No: P739201948
                Surname: REYNOLDS
                Given Names: ALEXANDRA JANE
                Nationality: UNITED STATES OF AMERICA
                Date of birth: 14 MAY 1991
                Sex: F   Place of birth: CALIFORNIA, U.S.A.
                Date of issue: 12 NOV 2016
                Authority: United States Department of State
                Date of expiration: 12 NOV 2026
                P<USAMORRISON<<ALEXANDRA<<<<<<<<<<<<<<<<<<<
                P7392019484USA9105146F2611126<<<<<<<<<<<<<<04
            """.trimIndent()
        ),
        SampleDocumentPreset(
            title = "California Driver License REAL ID",
            category = "driving_license",
            authority = "California DMV",
            docNumber = "DL-C8492015",
            daysFromNow = 18, // Critical!
            sampleOcrText = """
                CALIFORNIA DRIVER LICENSE REAL ID
                DL: DL-C8492015   CLASS: C   EXP: 02/11/2026
                NAME: MORRISON, ALEXANDRA JANE
                ADDRESS: 742 EVERGREEN TERRACE, SAN FRANCISCO, CA
                DOB: 05/14/1991   SEX: F   HAIR: BRN   EYES: BLU
                ISS: 02/11/2021   DONOR: VETERAN:
                RESTRICTIONS: NONE   ENDORSEMENTS: NONE
            """.trimIndent()
        ),
        SampleDocumentPreset(
            title = "Schengen Multi-Entry Visa",
            category = "visa",
            authority = "Federal Republic of Germany Embassy",
            docNumber = "DEU982341",
            daysFromNow = 8, // Urgent!
            sampleOcrText = """
                SCHENGENER STAATEN / SCHENGEN STATES
                VALID FOR: SCHENGEN STATES
                FROM: 15/05/2026   UNTIL: 23/10/2026
                NUMBER OF ENTRIES: MULT
                DURATION OF STAY: 90 DAYS
                ISSUED IN: SAN FRANCISCO   ON: 10/05/2026
                PASSPORT NUMBER: P739201948
                SURNAME: REYNOLDS, ALEXANDRA
                TYPE OF VISA: C   REMARKS: TOURISM / BUSINESS
            """.trimIndent()
        ),
        SampleDocumentPreset(
            title = "AppleCare+ Hardware Warranty",
            category = "warranty",
            authority = "Apple Inc.",
            docNumber = "AC-992384910",
            daysFromNow = 120, // Active
            sampleOcrText = """
                APPLECARE+ PROOF OF COVERAGE
                Agreement Number: AC-992384910
                Product: MacBook Pro 16-inch (M3 Max)
                Serial Number: W8819230KLQ
                Coverage End Date: 2027-04-18
                Original Purchase Date: 2024-04-18
                Status: Active Hardware & Accidental Damage Protection
                Authorized Service Provider: Apple Store & Apple Authorized
            """.trimIndent()
        )
    )
}

data class SampleDocumentPreset(
    val title: String,
    val category: String,
    val authority: String,
    val docNumber: String,
    val daysFromNow: Int,
    val sampleOcrText: String
)
