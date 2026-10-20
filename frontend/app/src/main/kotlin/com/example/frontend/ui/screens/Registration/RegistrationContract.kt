package com.example.frontend.ui.screens.Registration

import androidx.annotation.StringRes
import com.example.frontend.R
import com.example.frontend.data.address.AddressSuggestion
import com.example.frontend.data.identity.Citizenship
import com.example.frontend.data.identity.DocumentScan
import com.example.frontend.data.identity.SaIdInfo
import com.example.frontend.data.identity.SaIdNumber
import com.example.frontend.data.identity.SaIdProblem
import com.example.frontend.data.identity.SaIdResult
import java.time.LocalDate

const val REGISTRATION_STEPS = 5
const val POSTAL_CODE_LENGTH = 4

/**
 * [apiName] is the official English name the backend stores; [displayNameRes] is what the
 * voter sees in their language.
 */
enum class Province(val apiName: String, @param:StringRes val displayNameRes: Int) {
    EASTERN_CAPE("Eastern Cape", R.string.province_eastern_cape),
    FREE_STATE("Free State", R.string.province_free_state),
    GAUTENG("Gauteng", R.string.province_gauteng),
    KWAZULU_NATAL("KwaZulu-Natal", R.string.province_kwazulu_natal),
    LIMPOPO("Limpopo", R.string.province_limpopo),
    MPUMALANGA("Mpumalanga", R.string.province_mpumalanga),
    NORTHERN_CAPE("Northern Cape", R.string.province_northern_cape),
    NORTH_WEST("North West", R.string.province_north_west),
    WESTERN_CAPE("Western Cape", R.string.province_western_cape);

    companion object {
        /** Province for a stored [apiName], or null if it is not one of the nine. */
        fun fromApiName(name: String): Province? =
            entries.firstOrNull { it.apiName.lettersOnly() == name.lettersOnly() }

        private fun String.lettersOnly(): String = filter { it.isLetter() }.lowercase()
    }
}

enum class DetailsIssue(@param:StringRes val messageRes: Int) {
    NAME_MISSING(R.string.issue_name_missing),
    SURNAME_MISSING(R.string.issue_surname_missing),
    ID_MISSING(R.string.issue_id_missing),
    ID_LENGTH(R.string.issue_id_length),
    ID_BIRTH_DATE(R.string.issue_id_birth_date),
    ID_CHECKSUM(R.string.issue_id_checksum),
    ID_NOT_CITIZEN(R.string.issue_id_not_citizen),
    ID_TOO_YOUNG(R.string.issue_id_too_young),
    PHONE_MISSING(R.string.issue_phone_missing),
    PHONE_INVALID(R.string.issue_phone_invalid),
    EMAIL_INVALID(R.string.issue_email_invalid),
    PROVINCE_MISSING(R.string.issue_province_missing),
    STREET_MISSING(R.string.issue_street_missing),
    CITY_MISSING(R.string.issue_city_missing),
    POSTAL_INVALID(R.string.issue_postal_invalid)
}

private const val SA_ID_LENGTH = 13
private val SURNAME_PARTICLES = setOf("van", "von", "der", "den", "de", "du", "le", "la", "da", "dos", "ter", "ten")
private val EMAIL_PATTERN = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")

/** You may register from 16 (and vote from 18); only citizens may vote. */
const val MIN_REGISTRATION_AGE = 16

private fun idIssue(id: String): DetailsIssue? = when (val result = SaIdNumber.parse(id)) {
    is SaIdResult.Invalid -> when (result.problem) {
        SaIdProblem.BirthDate -> DetailsIssue.ID_BIRTH_DATE
        SaIdProblem.Checksum, SaIdProblem.Citizenship -> DetailsIssue.ID_CHECKSUM
        SaIdProblem.Length, SaIdProblem.NotDigits -> DetailsIssue.ID_LENGTH
    }
    is SaIdResult.Valid -> when {
        result.info.citizenship != Citizenship.Citizen -> DetailsIssue.ID_NOT_CITIZEN
        result.info.ageOn(LocalDate.now()) < MIN_REGISTRATION_AGE -> DetailsIssue.ID_TOO_YOUNG
        else -> null
    }
}

private fun isValidCellphone(phone: String): Boolean {
    val digits = phone.filter { it.isDigit() }
    return when {
        phone.trim().startsWith("+") || digits.startsWith("27") -> digits.length == 11 && digits.startsWith("27")
        else -> digits.length == 10 && digits.startsWith("0")
    }
}

enum class DocumentCheck { SCANNING, MATCHES, MISMATCH, NUMBER_UNREADABLE, NOT_IDENTIFIED }

enum class IdentityDocumentType {
    NATIONAL_ID,
    PASSPORT
}

enum class DocumentSide {
    FRONT,
    BACK
}

data class RegistrationUiState(
    val currentStep: Int = 1,
    val fullNames: String = "",
    val idNumber: String = "",
    val phone: String = "",
    val email: String = "",
    val province: Province? = null,
    val street: String = "",
    val city: String = "",
    val postalCode: String = "",

    val hasConsented: Boolean = false,

    // Identity Verification Details
    val documentType: IdentityDocumentType = IdentityDocumentType.NATIONAL_ID,
    val documentNumber: String = "",
    val documentFrontImagePath: String? = null,
    val documentBackImagePath: String? = null,
    // ID card identifier: what on-device OCR found on the front photo.
    val documentScan: DocumentScan? = null,
    val isScanningDocument: Boolean = false,

    // Face Verification Details
    val capturedFaceImagePath: String? = null,
    val isFaceVerified: Boolean = false,
    val isRegistered: Boolean = false,

    val addressSuggestions: List<AddressSuggestion> = emptyList(),
    val showDetailsIssues: Boolean = false,

    val isLoading: Boolean = false,
    @param:StringRes val errorRes: Int? = null
) {
    private val nameParts: List<String>
        get() = fullNames.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }

    private val surnameStart: Int
        get() {
            if (nameParts.size < 2) return nameParts.size
            var start = nameParts.lastIndex
            while (start > 1 && nameParts[start - 1] in SURNAME_PARTICLES) start--
            return start
        }

    val firstName: String
        get() = nameParts.take(surnameStart).joinToString(" ")

    val lastName: String
        get() = nameParts.drop(surnameStart).joinToString(" ")

    val detailsIssues: List<DetailsIssue>
        get() = buildList {
            val id = idNumber.trim()
            when {
                nameParts.isEmpty() -> add(DetailsIssue.NAME_MISSING)
                nameParts.size == 1 -> add(DetailsIssue.SURNAME_MISSING)
            }
            when {
                id.isEmpty() -> add(DetailsIssue.ID_MISSING)
                id.all { it.isDigit() } && id.length != SA_ID_LENGTH -> add(DetailsIssue.ID_LENGTH)
                id.all { it.isDigit() } -> idIssue(id)?.let { add(it) }
            }
            when {
                phone.isBlank() -> add(DetailsIssue.PHONE_MISSING)
                !isValidCellphone(phone) -> add(DetailsIssue.PHONE_INVALID)
            }
            if (email.isNotBlank() && !EMAIL_PATTERN.matches(email.trim())) add(DetailsIssue.EMAIL_INVALID)
            if (province == null) add(DetailsIssue.PROVINCE_MISSING)
            if (street.isBlank()) add(DetailsIssue.STREET_MISSING)
            if (city.isBlank()) add(DetailsIssue.CITY_MISSING)
            if (postalCode.length != POSTAL_CODE_LENGTH || !postalCode.all { it.isDigit() }) {
                add(DetailsIssue.POSTAL_INVALID)
            }
        }

    val canSubmitDetails: Boolean
        get() = detailsIssues.isEmpty()

    val canSubmitConsent: Boolean
        get() = hasConsented && !isLoading

    /** Result of comparing the scanned document with the numbers the voter typed. */
    val documentCheck: DocumentCheck?
        get() {
            if (documentFrontImagePath == null) return null
            if (isScanningDocument) return DocumentCheck.SCANNING
            val scan = documentScan ?: return null
            val typed = documentNumber.filter { it.isDigit() }
            return when {
                !scan.looksLikeSaDocument -> DocumentCheck.NOT_IDENTIFIED
                scan.idNumber == null -> DocumentCheck.NUMBER_UNREADABLE
                typed.isNotEmpty() && scan.idNumber != typed -> DocumentCheck.MISMATCH
                else -> DocumentCheck.MATCHES
            }
        }

    /** For an SA ID: a valid number that is the same one given on the details step. */
    @get:StringRes
    val documentNumberIssueRes: Int?
        get() {
            if (documentType != IdentityDocumentType.NATIONAL_ID || documentNumber.isBlank()) return null
            val number = documentNumber.trim()
            if (idIssue(number) != null || number.length != SA_ID_LENGTH) return R.string.issue_id_checksum
            val firstStep = idNumber.trim()
            return if (firstStep.all { it.isDigit() } && firstStep != number) R.string.issue_document_number_mismatch else null
        }

    val canSubmitIdentity: Boolean
        get() = hasConsented &&
            documentNumber.isNotBlank() &&
            documentNumberIssueRes == null &&
            documentFrontImagePath != null &&
            documentBackImagePath != null &&
            documentCheck != DocumentCheck.MISMATCH &&
            !isScanningDocument &&
            !isLoading

    val canSubmitFace: Boolean
        get() = hasConsented && capturedFaceImagePath != null && !isLoading

    /** Decoded birth date, gender and citizenship once a valid SA ID number is typed. */
    val idInfo: SaIdInfo?
        get() = (SaIdNumber.parse(idNumber.trim()) as? SaIdResult.Valid)?.info

    /** Blank until the voter types a name; screens show a translated fallback. */
    val voterName: String
        get() = fullNames.trim()
}

sealed interface RegistrationUiEvent {
    data class OnFullNamesChanged(val value: String) : RegistrationUiEvent
    data class OnIdNumberChanged(val value: String) : RegistrationUiEvent
    data class OnPhoneChanged(val value: String) : RegistrationUiEvent
    data class OnEmailChanged(val value: String) : RegistrationUiEvent
    data class OnProvinceSelected(val province: Province) : RegistrationUiEvent
    data class OnStreetChanged(val value: String) : RegistrationUiEvent
    data class OnCityChanged(val value: String) : RegistrationUiEvent
    data class OnPostalCodeChanged(val value: String) : RegistrationUiEvent
    data class OnAddressSuggestionPicked(val suggestion: AddressSuggestion) : RegistrationUiEvent
    data object OnDetailsSubmitAttempted : RegistrationUiEvent

    data class OnConsentChanged(val granted: Boolean) : RegistrationUiEvent

    // Identity Verification Events
    data class OnDocumentTypeSelected(val type: IdentityDocumentType) : RegistrationUiEvent
    data class OnDocumentNumberChanged(val number: String) : RegistrationUiEvent
    data class OnDocumentCaptured(val side: DocumentSide, val imagePath: String) : RegistrationUiEvent
    data object OnSaveIdentityDetails : RegistrationUiEvent

    // Face Verification Events
    data class OnFaceCaptured(val imagePath: String) : RegistrationUiEvent
    data object OnFaceRetake : RegistrationUiEvent
    data object OnVerifyFaceSubmitted : RegistrationUiEvent

    data object OnRegistrationStarted : RegistrationUiEvent
    data object OnRegistrationComplete : RegistrationUiEvent
    data object OnNextStepClicked : RegistrationUiEvent
    data object OnPreviousStepClicked : RegistrationUiEvent
}
