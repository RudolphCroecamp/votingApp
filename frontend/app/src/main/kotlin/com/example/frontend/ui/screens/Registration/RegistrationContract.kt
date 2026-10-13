package com.example.frontend.ui.screens.Registration

const val REGISTRATION_STEPS = 5

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
    val postalCode: String = "",
    val otpCode: String = "",

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
            postalCode.isNotBlank()

    val canSubmitConsent: Boolean
        get() = hasConsented && !isLoading

    val canSubmitIdentity: Boolean
        get() = hasConsented &&
            documentNumber.isNotBlank() &&
            documentFrontImagePath != null &&
            !isLoading

    val canSubmitFace: Boolean
        get() = hasConsented && capturedFaceImagePath != null && !isLoading

    val voterName: String
        get() = fullNames.ifBlank { "Voter" }
}

sealed interface RegistrationUiEvent {
    data class OnFullNamesChanged(val value: String) : RegistrationUiEvent
    data class OnIdNumberChanged(val value: String) : RegistrationUiEvent
    data class OnPhoneChanged(val value: String) : RegistrationUiEvent
    data class OnEmailChanged(val value: String) : RegistrationUiEvent
    data class OnPostalCodeChanged(val value: String) : RegistrationUiEvent
    data class OnOtpChanged(val value: String) : RegistrationUiEvent

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
