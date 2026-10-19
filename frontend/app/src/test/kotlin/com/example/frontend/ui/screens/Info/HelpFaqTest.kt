package com.example.frontend.ui.screens.Info

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HelpFaqTest {

    @Test
    fun `search matches every word anywhere in question or answer, ignoring case`() {
        assertTrue(matchesQuery("Why is my account locked?", "After 3 face checks...", "LOCKED face"))
        assertTrue(matchesQuery("Where do I vote?", "SMS your ID number to 32810", "32810"))
        assertFalse(matchesQuery("Where do I vote?", "SMS your ID number", "passport"))
    }

    @Test
    fun `blank search shows everything`() {
        assertTrue(matchesQuery("Any", "thing", "   "))
    }

    @Test
    fun `every category has questions and every question is unique`() {
        FaqCategory.entries.forEach { category ->
            assertTrue("$category is empty", FaqEntries.any { it.category == category })
        }
        assertEquals(FaqEntries.size, FaqEntries.map { it.question }.toSet().size)
    }
}
