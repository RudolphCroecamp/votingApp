package com.example.frontend.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.frontend.R
import com.example.frontend.data.identity.Citizenship
import com.example.frontend.data.identity.Gender
import com.example.frontend.data.identity.SA_ID_DIGITS
import com.example.frontend.data.identity.SaIdNumber
import com.example.frontend.data.identity.SaIdProblem
import com.example.frontend.data.identity.SaIdResult
import com.example.frontend.ui.theme.ErrorContent
import com.example.frontend.ui.theme.SuccessContent
import java.time.format.DateTimeFormatter

/** Problem with a typed SA ID number, as user-facing text, or null while it is still being typed. */
@StringRes
fun saIdProblemRes(id: String): Int? {
    val trimmed = id.trim()
    // Passports are letters and digits; only all-digit entries are checked as SA IDs.
    if (trimmed.isEmpty() || !trimmed.all { it.isDigit() } || trimmed.length < SA_ID_DIGITS) return null
    return when ((SaIdNumber.parse(trimmed) as? SaIdResult.Invalid)?.problem) {
        null -> null
        SaIdProblem.BirthDate -> R.string.issue_id_birth_date
        SaIdProblem.Checksum, SaIdProblem.Citizenship -> R.string.issue_id_checksum
        SaIdProblem.Length, SaIdProblem.NotDigits -> R.string.issue_id_length
    }
}

/**
 * Live feedback under an ID number field: once 13 digits are in, either what the number
 * says (birth date, gender, citizenship) or why it is not a valid South African ID.
 */
@Composable
fun IdNumberFeedback(idNumber: String, modifier: Modifier = Modifier) {
    val trimmed = idNumber.trim()
    if (trimmed.length != SA_ID_DIGITS || !trimmed.all { it.isDigit() }) return
    val locale = LocalConfiguration.current.locales[0]
    val (ok, text) = when (val result = SaIdNumber.parse(trimmed)) {
        is SaIdResult.Valid -> true to stringResource(
            R.string.id_feedback_valid,
            result.info.birthDate.format(DateTimeFormatter.ofPattern("d MMMM yyyy", locale)),
            stringResource(if (result.info.gender == Gender.Female) R.string.id_gender_female else R.string.id_gender_male),
            stringResource(
                when (result.info.citizenship) {
                    Citizenship.Citizen -> R.string.id_citizen
                    Citizenship.PermanentResident -> R.string.id_permanent_resident
                    Citizenship.Refugee -> R.string.id_refugee
                }
            )
        )
        is SaIdResult.Invalid -> false to stringResource(saIdProblemRes(trimmed) ?: R.string.issue_id_checksum)
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
            .semantics { liveRegion = LiveRegionMode.Polite },
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (ok) Icons.Default.CheckCircle else Icons.Default.ErrorOutline,
            contentDescription = null,
            tint = if (ok) SuccessContent else ErrorContent,
            modifier = Modifier.size(16.dp)
        )
        Text(text = text, style = MaterialTheme.typography.bodySmall, color = if (ok) SuccessContent else ErrorContent)
    }
}
