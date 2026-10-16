package com.example.frontend.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Every language must have exactly the same strings as English, with the same
 * placeholders. A missing key shows English mid-screen; a wrong placeholder crashes.
 */
class TranslationParityTest {

    private val res = File("src/main/res")
    private val stringTag = Regex("""<string name="([^"]+)"[^>]*>(.*?)</string>""", RegexOption.DOT_MATCHES_ALL)
    private val placeholder = Regex("""%\d+\$[sd]""")

    private fun strings(dir: String): Map<String, String> =
        stringTag.findAll(File(res, "$dir/strings.xml").readText())
            .filterNot { it.value.contains("translatable=\"false\"") }
            .associate { it.groupValues[1] to it.groupValues[2] }

    private val english = strings("values")
    private val locales = listOf("af", "zu", "xh")

    @Test
    fun `every language has every English string`() {
        assertTrue(english.isNotEmpty())
        locales.forEach { lang ->
            val missing = english.keys - strings("values-$lang").keys
            assertEquals("values-$lang is missing", emptySet<String>(), missing)
        }
    }

    @Test
    fun `placeholders match English`() {
        locales.forEach { lang ->
            strings("values-$lang").forEach { (key, text) ->
                val expected = english[key] ?: return@forEach
                assertEquals(
                    "values-$lang/$key placeholders",
                    placeholder.findAll(expected).map { it.value }.sorted().toList(),
                    placeholder.findAll(text).map { it.value }.sorted().toList()
                )
            }
        }
    }
}
