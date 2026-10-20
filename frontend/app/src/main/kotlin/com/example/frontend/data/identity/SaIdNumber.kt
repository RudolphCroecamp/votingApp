package com.example.frontend.data.identity

import java.time.DateTimeException
import java.time.LocalDate
import java.time.Period

/**
 * A South African ID number, YYMMDD SSSS C A Z:
 * - YYMMDD date of birth
 * - SSSS sequence: 0000-4999 female, 5000-9999 male
 * - C citizenship: 0 citizen, 1 permanent resident, 2 refugee
 * - A historically race, now always 8 or 9; not validated
 * - Z Luhn check digit over the first 12
 */
enum class Gender { Female, Male }

enum class Citizenship { Citizen, PermanentResident, Refugee }

data class SaIdInfo(val birthDate: LocalDate, val gender: Gender, val citizenship: Citizenship) {
    fun ageOn(day: LocalDate): Int = Period.between(birthDate, day).years
}

enum class SaIdProblem { Length, NotDigits, BirthDate, Citizenship, Checksum }

sealed interface SaIdResult {
    data class Valid(val info: SaIdInfo) : SaIdResult
    data class Invalid(val problem: SaIdProblem) : SaIdResult
}

const val SA_ID_DIGITS = 13

object SaIdNumber {

    fun parse(raw: String, today: LocalDate = LocalDate.now()): SaIdResult {
        val id = raw.filterNot { it.isWhitespace() }
        if (!id.all { it.isDigit() }) return SaIdResult.Invalid(SaIdProblem.NotDigits)
        if (id.length != SA_ID_DIGITS) return SaIdResult.Invalid(SaIdProblem.Length)

        val birth = birthDate(id, today) ?: return SaIdResult.Invalid(SaIdProblem.BirthDate)
        val citizenship = when (id[10]) {
            '0' -> Citizenship.Citizen
            '1' -> Citizenship.PermanentResident
            '2' -> Citizenship.Refugee
            else -> return SaIdResult.Invalid(SaIdProblem.Citizenship)
        }
        if (!luhnValid(id)) return SaIdResult.Invalid(SaIdProblem.Checksum)

        val gender = if (id.substring(6, 10).toInt() < 5000) Gender.Female else Gender.Male
        return SaIdResult.Valid(SaIdInfo(birth, gender, citizenship))
    }

    /** Two-digit years: the most recent year that is not in the future. */
    private fun birthDate(id: String, today: LocalDate): LocalDate? {
        val yy = id.substring(0, 2).toInt()
        val month = id.substring(2, 4).toInt()
        val day = id.substring(4, 6).toInt()
        val century = if (2000 + yy <= today.year) 2000 else 1900
        val date = try {
            LocalDate.of(century + yy, month, day)
        } catch (e: DateTimeException) {
            return null
        }
        return date.takeUnless { it.isAfter(today) }
    }

    /** Luhn (mod 10): double every second digit from the right, excluding the check digit. */
    fun luhnValid(digits: String): Boolean {
        var sum = 0
        digits.reversed().forEachIndexed { index, c ->
            var d = c - '0'
            if (index % 2 == 1) {
                d *= 2
                if (d > 9) d -= 9
            }
            sum += d
        }
        return sum % 10 == 0
    }
}
