package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.scanner.DateOcrScanner
import com.example.data.util.ExpiryDateUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Expiry", appName)
    }

    @Test
    fun `test date parsing and countdown`() {
        val parsed = ExpiryDateUtils.parseDate("2026-11-15")
        assertNotNull(parsed)
        val days = ExpiryDateUtils.calculateDaysRemaining(parsed!!)
        val statusText = ExpiryDateUtils.getRelativeStatusText(days)
        assertTrue(statusText.isNotBlank())
    }

    @Test
    fun `test on device ocr scanner extracts passport expiration`() {
        val passportSample = DateOcrScanner.sampleDocumentScans[0].sampleOcrText
        val result = DateOcrScanner.scanTextForDocumentData(passportSample)
        assertNotNull(result.detectedExpiryDateMillis)
        assertEquals("P739201948", result.detectedDocNumber)
        assertEquals("US Department of State", result.detectedAuthority)
        assertTrue(result.confidenceScore > 0.8f)
    }
}
