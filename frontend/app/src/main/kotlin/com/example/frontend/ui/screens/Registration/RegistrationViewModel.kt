package com.example.frontend.ui.screens.Registration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.frontend.ui.Components.Otp.OtpState
import com.example.frontend.ui.Components.Otp.maskDestination
import com.example.frontend.ui.Components.Otp.rejected
import com.example.frontend.ui.Components.Otp.sent
import com.example.frontend.ui.Components.Otp.ticked
import com.example.frontend.ui.Components.Otp.verified
import com.example.frontend.ui.Components.Otp.verifying
import com.example.frontend.ui.Components.Otp.withCode
import com.example.frontend.ui.screens.Auth.ID_NUMBER_MAX_LENGTH
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val STUB_OTP_CODE = "123456"

class RegistrationViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(RegistrationUiState())
    val uiState: StateFlow<RegistrationUiState> = _uiState.asStateFlow()

    private var otpTicker: Job? = null

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
            is RegistrationUiEvent.OnProvinceSelected -> _uiState.update { it.copy(province = event.province) }
            is RegistrationUiEvent.OnStreetChanged -> _uiState.update { it.copy(street = event.value) }
            is RegistrationUiEvent.OnCityChanged -> _uiState.update { it.copy(city = event.value) }
            is RegistrationUiEvent.OnPostalCodeChanged -> {
                if (event.value.length <= POSTAL_CODE_LENGTH) {
                    _uiState.update { it.copy(postalCode = event.value) }
                }
            }
            is RegistrationUiEvent.OnOtpChanged -> _uiState.update {
                it.copy(otp = it.otp.withCode(event.value))
            }
            RegistrationUiEvent.OnOtpRequested -> sendOtp()
            RegistrationUiEvent.OnOtpSubmitted -> verifyOtp()

            is RegistrationUiEvent.OnConsentChanged -> _uiState.update { it.copy(hasConsented = event.granted) }

            is RegistrationUiEvent.OnDocumentTypeSelected -> _uiState.update { it.copy(documentType = event.type) }
            is RegistrationUiEvent.OnDocumentNumberChanged -> _uiState.update { it.copy(documentNumber = event.number) }
            is RegistrationUiEvent.OnDocumentCaptured -> _uiState.update { it.copy(documentFrontImagePath = event.imagePath) }

            is RegistrationUiEvent.OnFaceCaptured -> _uiState.update { it.copy(capturedFaceImagePath = event.imagePath) }
            RegistrationUiEvent.OnFaceRetake -> _uiState.update {
                it.copy(capturedFaceImagePath = null, isFaceVerified = false)
            }

            RegistrationUiEvent.OnVerifyFaceSubmitted -> processFaceVerification()
            RegistrationUiEvent.OnSaveIdentityDetails -> saveIdentityToSystem()
            RegistrationUiEvent.OnRegistrationComplete -> {
                otpTicker?.cancel()
                _uiState.value = RegistrationUiState()
            }

            RegistrationUiEvent.OnNextStepClicked -> _uiState.update {
                it.copy(currentStep = (it.currentStep + 1).coerceAtMost(REGISTRATION_STEPS))
            }
            RegistrationUiEvent.OnPreviousStepClicked -> _uiState.update {
                it.copy(currentStep = (it.currentStep - 1).coerceAtLeast(1))
            }
        }
    }

    private fun sendOtp() {
        _uiState.update { state ->
            state.copy(otp = state.otp.sent(destination = maskDestination(state.phone)))
        }
        startOtpTicker()
    }

    private fun verifyOtp() {
        if (!_uiState.value.otp.canSubmit) return
        viewModelScope.launch {
            _uiState.update { it.copy(otp = it.otp.verifying()) }
            delay(600) // Simulate the server checking the code
            val entered = _uiState.value.otp.code
            _uiState.update { state ->
                val otp: OtpState = if (entered == STUB_OTP_CODE) {
                    state.otp.verified()
                } else {
                    state.otp.rejected()
                }
                state.copy(otp = otp)
            }
            startOtpTicker()
        }
    }

    private fun startOtpTicker() {
        otpTicker?.cancel()
        if (!_uiState.value.otp.needsTicking) return
        otpTicker = viewModelScope.launch {
            while (_uiState.value.otp.needsTicking) {
                delay(1_000)
                _uiState.update { it.copy(otp = it.otp.ticked()) }
            }
        }
    }

    private fun saveIdentityToSystem() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            delay(500) // Simulate processing
            _uiState.update { it.copy(isLoading = false, currentStep = 5) }
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
                    currentStep = 6
                )
            }
        }
    }
}
