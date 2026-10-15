package com.example.frontend.ui.screens.Registration

import androidx.annotation.StringRes
import com.example.frontend.R
import com.example.frontend.ui.Components.Otp.OtpState

const val REGISTRATION_STEPS = 6
const val OTP_STEP = 2
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
            entries.firstOrNull { it.apiName.equals(name.trim(), ignoreCase = true) }
    }
}

enum class IdentityDocumentType {
    NATIONAL_ID,
    PASSPORT
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
    val otp: OtpState = OtpState(),

    val hasConsented: Boolean = false,

    // Identity Verification Details
    val documentType: IdentityDocumentType = IdentityDocumentType.NATIONAL_ID,
    val documentNumber: String = "",
    val documentFrontImagePath: String? = null,

    // Face Verification Details
    val capturedFaceImagePath: String? = null,
    val isFaceVerified: Boolean = false,

    val isLoading: Boolean = false,
    val errorMessage: String? = null
) {
    val canSubmitDetails: Boolean
        get() = fullNames.isNotBlank() &&
            idNumber.isNotBlank() &&
            phone.isNotBlank() &&
            province != null &&
            street.isNotBlank() &&
            city.isNotBlank() &&
            postalCode.length == POSTAL_CODE_LENGTH

    val canSubmitOtp: Boolean
        get() = otp.canSubmit

    val canSubmitConsent: Boolean
        get() = hasConsented && !isLoading

    val canSubmitIdentity: Boolean
        get() = hasConsented &&
            documentNumber.isNotBlank() &&
            documentFrontImagePath != null &&
            !isLoading

    val canSubmitFace: Boolean
        get() = hasConsented && capturedFaceImagePath != null && !isLoading

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
    data class OnOtpChanged(val value: String) : RegistrationUiEvent
    data object OnOtpRequested : RegistrationUiEvent
    data object OnOtpSubmitted : RegistrationUiEvent

    data class OnConsentChanged(val granted: Boolean) : RegistrationUiEvent

    // Identity Verification Events
    data class OnDocumentTypeSelected(val type: IdentityDocumentType) : RegistrationUiEvent
    data class OnDocumentNumberChanged(val number: String) : RegistrationUiEvent
    data class OnDocumentCaptured(val imagePath: String) : RegistrationUiEvent
    data object OnSaveIdentityDetails : RegistrationUiEvent

    // Face Verification Events
    data class OnFaceCaptured(val imagePath: String) : RegistrationUiEvent
    data object OnFaceRetake : RegistrationUiEvent
    data object OnVerifyFaceSubmitted : RegistrationUiEvent

    data object OnRegistrationComplete : RegistrationUiEvent
    data object OnNextStepClicked : RegistrationUiEvent
    data object OnPreviousStepClicked : RegistrationUiEvent
}
