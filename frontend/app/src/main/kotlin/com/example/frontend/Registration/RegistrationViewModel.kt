package com.example.frontend.ui.Registration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RegistrationViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(RegistrationUiState())
    val uiState: StateFlow<RegistrationUiState> = _uiState.asStateFlow()

    fun onEvent(event: RegistrationUiEvent) {
        when (event) {
            is RegistrationUiEvent.OnIdNumberChanged -> _uiState.update { it.copy(idNumber = event.value) }
            is RegistrationUiEvent.OnFullNamesChanged -> _uiState.update { it.copy(fullNames = event.value) }
            is RegistrationUiEvent.OnDobChanged -> _uiState.update { it.copy(dob = event.value) }
            is RegistrationUiEvent.OnPhoneChanged -> _uiState.update { it.copy(phone = event.value) }
            is RegistrationUiEvent.OnEmailChanged -> _uiState.update { it.copy(email = event.value) }
            is RegistrationUiEvent.OnOtpChanged -> {
                if (event.value.length <= 6) _uiState.update { it.copy(otpCode = event.value) }
            }
            is RegistrationUiEvent.OnDocumentTypeSelected -> _uiState.update { it.copy(documentType = event.type) }
            is RegistrationUiEvent.OnDocumentNumberChanged -> _uiState.update { it.copy(documentNumber = event.number) }
            is RegistrationUiEvent.OnDocumentCaptured -> _uiState.update { it.copy(documentFrontImagePath = event.imagePath) }
            is RegistrationUiEvent.OnFaceCaptured -> _uiState.update { it.copy(capturedFaceImagePath = event.imagePath) }

            RegistrationUiEvent.OnVerifyFaceSubmitted -> processFaceVerification()
            RegistrationUiEvent.OnSaveIdentityDetails -> saveIdentityToSystem()
            RegistrationUiEvent.OnRegistrationComplete -> _uiState.value = RegistrationUiState()

            RegistrationUiEvent.OnNextStepClicked -> {
                _uiState.update { it.copy(currentStep = (it.currentStep + 1).coerceAtMost(4)) }
            }
            RegistrationUiEvent.OnPreviousStepClicked -> {
                _uiState.update { it.copy(currentStep = (it.currentStep - 1).coerceAtLeast(1)) }
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