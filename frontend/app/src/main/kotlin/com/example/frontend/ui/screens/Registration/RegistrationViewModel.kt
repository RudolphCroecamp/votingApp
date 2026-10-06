package com.example.frontend.ui.screens.Registration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.frontend.ui.screens.Auth.ID_NUMBER_MAX_LENGTH
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val OTP_LENGTH = 6
private const val POSTAL_CODE_MAX_LENGTH = 4

class RegistrationViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(RegistrationUiState())
    val uiState: StateFlow<RegistrationUiState> = _uiState.asStateFlow()

    fun onEvent(event: RegistrationUiEvent) {
        when (event) {
            is RegistrationUiEvent.OnFullNamesChanged -> _uiState.update { it.copy(fullNames = event.value) }
            is RegistrationUiEvent.OnIdNumberChanged -> {
                if (event.value.length <= ID_NUMBER_MAX_LENGTH) {
                    _uiState.update { it.copy(idNumber = event.value) }
                }
            }
            is RegistrationUiEvent.OnPhoneChanged -> _uiState.update { it.copy(phone = event.value) }
            is RegistrationUiEvent.OnEmailChanged -> _uiState.update { it.copy(email = event.value) }
            is RegistrationUiEvent.OnPostalCodeChanged -> {
                if (event.value.length <= POSTAL_CODE_MAX_LENGTH) {
                    _uiState.update { it.copy(postalCode = event.value) }
                }
            }
            is RegistrationUiEvent.OnOtpChanged -> {
                if (event.value.length <= OTP_LENGTH) _uiState.update { it.copy(otpCode = event.value) }
            }

            is RegistrationUiEvent.OnDocumentTypeSelected -> _uiState.update { it.copy(documentType = event.type) }
            is RegistrationUiEvent.OnDocumentNumberChanged -> _uiState.update { it.copy(documentNumber = event.number) }
            is RegistrationUiEvent.OnDocumentCaptured -> _uiState.update { it.copy(documentFrontImagePath = event.imagePath) }

            is RegistrationUiEvent.OnFaceCaptured -> _uiState.update { it.copy(capturedFaceImagePath = event.imagePath) }
            RegistrationUiEvent.OnFaceRetake -> _uiState.update {
                it.copy(capturedFaceImagePath = null, isFaceVerified = false)
            }

            RegistrationUiEvent.OnVerifyFaceSubmitted -> processFaceVerification()
            RegistrationUiEvent.OnSaveIdentityDetails -> saveIdentityToSystem()
            RegistrationUiEvent.OnRegistrationComplete -> _uiState.value = RegistrationUiState()

            RegistrationUiEvent.OnNextStepClicked -> _uiState.update {
                it.copy(currentStep = (it.currentStep + 1).coerceAtMost(REGISTRATION_STEPS))
            }
            RegistrationUiEvent.OnPreviousStepClicked -> _uiState.update {
                it.copy(currentStep = (it.currentStep - 1).coerceAtLeast(1))
            }
        }
    }

    private fun saveIdentityToSystem() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            delay(500) // Simulate processing
            _uiState.update { it.copy(isLoading = false, currentStep = 3) }
        }
    }

    private fun processFaceVerification() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            delay(1000) // Simulate ML / Biometric face matching against ID document
            _uiState.update {
                it.copy(
                    isLoading = false,
                    isFaceVerified = true,
                    currentStep = 4
                )
            }
        }
    }
}
