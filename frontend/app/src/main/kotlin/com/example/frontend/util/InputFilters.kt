package com.example.frontend.util

/**
 * Keystroke filters for form fields. They drop characters that can never be valid
 * instead of showing an error after the fact, which also cleans up pasted text.
 */
object InputFilters {

    /** Digits only, e.g. an ID number pasted as "920118 5082 083". */
    fun digits(input: String): String = input.filter { it.isDigit() }

    /**
     * Letters in any alphabet (so names like "Zoë" and "Nkosinathi" both work), plus the
     * separators real names use: spaces, hyphens and apostrophes ("van der Merwe",
     * "Smith-Jones", "O'Brien"). Digits and other symbols are dropped.
     */
    fun personName(input: String): String = input.filter { it.isLetter() || it in NAME_SEPARATORS }

    private val NAME_SEPARATORS = setOf(' ', '-', '\'', '’')
}
