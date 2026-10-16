package com.example.frontend.ui.screens.Vote

import com.example.frontend.ui.components.Ballot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Receipt lookup on the Verify Your Vote screen (KAN-43). */
class ReceiptLookupTest {

    private val knownCode = "AB7F-92K4-6ZP1-3QX8"

    @Test
    fun `a known receipt code finds its vote`() {
        val result = sampleLookup(knownCode)

        assertTrue(result is ReceiptLookup.Found)
        result as ReceiptLookup.Found
        assertEquals(knownCode, result.receiptCode)
        assertEquals(Ballot.National, result.ballot)
    }

    @Test
    fun `lookup ignores letter case and shows the code in capitals`() {
        val result = sampleLookup(knownCode.lowercase())

        assertTrue(result is ReceiptLookup.Found)
        assertEquals(knownCode, (result as ReceiptLookup.Found).receiptCode)
    }

    @Test
    fun `an unknown or mistyped code finds nothing`() {
        assertEquals(ReceiptLookup.NotFound, sampleLookup("AB7F-92K4-6ZP1-3QX9"))
        assertEquals(ReceiptLookup.NotFound, sampleLookup(""))
    }

    @Test
    fun `a found receipt never says who the vote was for`() {
        val found = sampleLookup(knownCode) as ReceiptLookup.Found
        // Only these four fields exist; there is no party anywhere on a receipt.
        val fields = listOf(found.receiptCode, found.ballot.name, found.castAt, found.electionName)
        assertTrue(fields.none { it.contains("Congress") || it.contains("Alliance") })
    }
}
