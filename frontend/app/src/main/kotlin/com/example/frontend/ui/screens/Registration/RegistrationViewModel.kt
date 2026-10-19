package com.example.frontend.ui.screens.Registration

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.frontend.data.AppContainer
import com.example.frontend.data.auth.AuthException
import com.example.frontend.data.auth.AuthRepository
import com.example.frontend.data.auth.RegistrationForm
import com.example.frontend.data.auth.TokenStore
import com.example.frontend.data.face.FaceEmbedder
import com.example.frontend.ui.screens.Auth.ID_NUMBER_MAX_LENGTH
import com.example.frontend.ui.screens.Auth.decodePhoto
import com.example.frontend.ui.screens.Auth.toMessageRes
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

private const val TAG = "Registration"

class RegistrationViewModel(
    private val authRepository: AuthRepository = AppContainer.authRepository,
    private val faceEmbedder: FaceEmbedder = AppContainer.faceEmbedder,
    private val tokenStore: TokenStore = AppContainer.tokenStore
) : ViewModel() {

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
            is RegistrationUiEvent.OnProvinceSelected -> _uiState.update { it.copy(province = event.province) }
            is RegistrationUiEvent.OnStreetChanged -> _uiState.update { it.copy(street = event.value) }
            is RegistrationUiEvent.OnCityChanged -> _uiState.update { it.copy(city = event.value) }
            is RegistrationUiEvent.OnPostalCodeChanged -> {
                if (event.value.length <= POSTAL_CODE_LENGTH) {
                    _uiState.update { it.copy(postalCode = event.value) }
                }
            }

            is RegistrationUiEvent.OnConsentChanged -> _uiState.update { it.copy(hasConsented = event.granted) }

            is RegistrationUiEvent.OnDocumentTypeSelected -> _uiState.update { it.copy(documentType = event.type) }
            is RegistrationUiEvent.OnDocumentNumberChanged -> _uiState.update { it.copy(documentNumber = event.number) }
            is RegistrationUiEvent.OnDocumentCaptured -> _uiState.update {
                when (event.side) {
                    DocumentSide.FRONT -> it.copy(documentFrontImagePath = event.imagePath)
                    DocumentSide.BACK -> it.copy(documentBackImagePath = event.imagePath)
                }
            }

            is RegistrationUiEvent.OnFaceCaptured -> _uiState.update {
                it.copy(capturedFaceImagePath = event.imagePath, errorRes = null)
            }
            RegistrationUiEvent.OnFaceRetake -> _uiState.update {
                it.copy(capturedFaceImagePath = null, isFaceVerified = false, errorRes = null)
            }

            RegistrationUiEvent.OnVerifyFaceSubmitted -> submitRegistration()
            RegistrationUiEvent.OnSaveIdentityDetails -> _uiState.update {
                if (it.canSubmitIdentity) it.copy(currentStep = 4) else it
            }
            RegistrationUiEvent.OnRegistrationComplete -> _uiState.value = RegistrationUiState()

            RegistrationUiEvent.OnNextStepClicked -> _uiState.update {
                it.copy(currentStep = (it.currentStep + 1).coerceAtMost(REGISTRATION_STEPS))
            }
            RegistrationUiEvent.OnPreviousStepClicked -> _uiState.update {
                it.copy(currentStep = (it.currentStep - 1).coerceAtLeast(1))
            }
        }
    }

    private fun submitRegistration() {
        val state = _uiState.value
        val facePath = state.capturedFaceImagePath ?: return
        val frontPath = state.documentFrontImagePath ?: return
        val backPath = state.documentBackImagePath ?: return
        val province = state.province ?: return
        val postalCode = state.postalCode.toIntOrNull() ?: return
        if (!state.canSubmitFace) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorRes = null) }
            try {
                val embedding = faceEmbedder.embed(decodePhoto(facePath))
                authRepository.register(
                    RegistrationForm(
                        idNumber = state.idNumber,
                        firstName = state.firstName,
                        lastName = state.lastName,
                        province = province.apiName,
                        city = state.city.trim(),
                        street = state.street.trim(),
                        postalCode = postalCode,
                        faceEmbedding = embedding,
                        idFront = File(frontPath),
                        idBack = File(backPath)
                    )
                )
                signIn(state.idNumber, embedding)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isFaceVerified = true,
                        isRegistered = true,
                        currentStep = REGISTRATION_STEPS
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w(TAG, "Registration failed", e)
                _uiState.update { it.copy(isLoading = false, errorRes = e.toMessageRes()) }
            }
        }
    }

    private suspend fun signIn(idNumber: String, embedding: DoubleArray) {
        try {
            tokenStore.save(authRepository.login(idNumber, embedding))
        } catch (e: AuthException) {
            Log.w(TAG, "Registered, but automatic sign-in failed", e)
        }
    }
}
