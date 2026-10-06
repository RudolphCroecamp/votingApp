package com.example.frontend.ui.Registration

enum class IdentityDocumentType {
    NATIONAL_ID,
    PASSPORT
}

data class RegistrationUiState(
    val currentStep: Int = 1,
    val idNumber: String = "",
    val fullNames: String = "",
    val dob: String = "",
    val phone: String = "",
    val email: String = "",
    val otpCode: String = "",

    // Identity Verification Details
    val documentType: IdentityDocumentType = IdentityDocumentType.NATIONAL_ID,
    val documentNumber: String = "",
    val documentFrontImagePath: String? = null,

    // Face Verification Details
    val capturedFaceImagePath: String? = null,
    val isFaceVerified: Boolean = false,

    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

sealed interface RegistrationUiEvent {
    data class OnIdNumberChanged(val value: String) : RegistrationUiEvent
    data class OnFullNamesChanged(val value: String) : RegistrationUiEvent
    data class OnDobChanged(val value: String) : RegistrationUiEvent
    data class OnPhoneChanged(val value: String) : RegistrationUiEvent
    data class OnEmailChanged(val value: String) : RegistrationUiEvent
    data class OnOtpChanged(val value: String) : RegistrationUiEvent

    // Identity Verification Events
    data class OnDocumentTypeSelected(val type: IdentityDocumentType) : RegistrationUiEvent
    data class OnDocumentNumberChanged(val number: String) : RegistrationUiEvent
    data class OnDocumentCaptured(val imagePath: String) : RegistrationUiEvent
    data object OnSaveIdentityDetails : RegistrationUiEvent

    // Face Verification Events
    data class OnFaceCaptured(val imagePath: String) : RegistrationUiEvent
    data object OnVerifyFaceSubmitted : RegistrationUiEvent

    data object OnRegistrationComplete : RegistrationUiEvent
    data object OnNextStepClicked : RegistrationUiEvent
    data object OnPreviousStepClicked : RegistrationUiEvent
}