package com.example.frontend.data.identity

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class SaIdNumberTest {

    private val today = LocalDate.of(2026, 10, 20)

    private fun valid(id: String) = (SaIdNumber.parse(id, today) as SaIdResult.Valid).info
    private fun problem(id: String) = (SaIdNumber.parse(id, today) as SaIdResult.Invalid).problem

    @Test
    fun `decodes birth date, gender and citizenship`() {
        val info = valid("8001015009087")
        assertEquals(LocalDate.of(1980, 1, 1), info.birthDate)
        assertEquals(Gender.Male, info.gender)
        assertEquals(Citizenship.Citizen, info.citizenship)
    }

    @Test
    fun `sequence below 5000 is female and two-digit years pick the right century`() {
        val info = valid("0412300800080")
        assertEquals(LocalDate.of(2004, 12, 30), info.birthDate)
        assertEquals(Gender.Female, info.gender)
    }

    @Test
    fun `rejects impossible dates, bad checksums, bad citizenship and wrong lengths`() {
        assertEquals(SaIdProblem.BirthDate, problem("8013015009087"))   // month 13
        assertEquals(SaIdProblem.BirthDate, problem("8002305009087"))   // 30 Feb
        assertEquals(SaIdProblem.Checksum, problem("8001015009088"))
        assertEquals(SaIdProblem.Citizenship, problem("8001015009387"))
        assertEquals(SaIdProblem.Length, problem("800101500908"))
        assertEquals(SaIdProblem.NotDigits, problem("A001015009087"))
    }

    @Test
    fun `permanent residents parse but are flagged as such`() {
        // 8001015009 1 8 Z: find the Z that passes Luhn.
        val id = (0..9).map { "800101500918$it" }.first { SaIdNumber.luhnValid(it) }
        assertEquals(Citizenship.PermanentResident, valid(id).citizenship)
    }

    @Test
    fun `identifier reads the number off OCR text despite spaces and misreads`() {
        val smartCard = """
            REPUBLIC OF SOUTH AFRICA
            IDENTITY CARD
            Surname VAN WYK
            Nationality RSA
            Identity Number
            80O1O15 OO9O87
            Country of Birth RSA
            Status CITIZEN
        """.trimIndent()
        val scan = identifyDocument(smartCard)
        assertEquals(DocumentKind.SmartIdCard, scan.kind)
        assertEquals("8001015009087", scan.idNumber)
    }

    @Test
    fun `a random photo is not identified as an ID`() {
        val scan = identifyDocument("Menu\nCoffee 25.00\nTea 20.00")
        assertEquals(DocumentKind.Unknown, scan.kind)
        assertNull(scan.idNumber)
        assertTrue(!scan.looksLikeSaDocument)
    }
}
