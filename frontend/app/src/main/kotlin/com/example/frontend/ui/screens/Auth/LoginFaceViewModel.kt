package com.example.frontend.ui.screens.Auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.frontend.data.AppContainer
import com.example.frontend.data.auth.AuthException
import com.example.frontend.data.auth.AuthRepository
import com.example.frontend.data.auth.TokenStore
import com.example.frontend.data.face.FaceEmbedder
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val TAG = "LoginFace"

class LoginFaceViewModel(
    private val authRepository: AuthRepository = AppContainer.authRepository,
    private val faceEmbedder: FaceEmbedder = AppContainer.faceEmbedder,
    private val tokenStore: TokenStore = AppContainer.tokenStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginFaceUiState())
    val uiState: StateFlow<LoginFaceUiState> = _uiState.asStateFlow()

    private var lockTicker: Job? = null

    fun start(idNumber: String) {
        if (_uiState.value.idNumber == idNumber) return
        _uiState.value = LoginFaceUiState(idNumber = idNumber)
    }

    fun onEvent(event: LoginFaceUiEvent) {
        when (event) {
            is LoginFaceUiEvent.OnFaceCaptured -> matchFace(event.imagePath)
            is LoginFaceUiEvent.OnCaptureProblem -> _uiState.update {
                it.withCaptureProblem(event.messageRes)
            }
            LoginFaceUiEvent.OnRetry -> _uiState.update { it.retried() }
        }
    }

    private fun matchFace(imagePath: String) {
        if (!_uiState.value.cameraActive) return
        viewModelScope.launch {
            _uiState.update { it.checking() }
            try {
                val embedding = faceEmbedder.embed(decodePhoto(imagePath))
                tokenStore.save(authRepository.login(_uiState.value.idNumber, embedding))
                _uiState.update { it.matched() }
            } catch (e: CancellationException) {
                throw e
            } catch (e: AuthException.InvalidCredentials) {
                _uiState.update { it.rejected() }
            } catch (e: Exception) {
                Log.w(TAG, "Face login failed", e)
                _uiState.update { it.withCaptureProblem(e.toMessageRes()) }
            }
            startLockTicker()
        }
    }

    private fun startLockTicker() {
        lockTicker?.cancel()
        if (!_uiState.value.needsTicking) return
        lockTicker = viewModelScope.launch {
            while (_uiState.value.needsTicking) {
                delay(1_000)
                _uiState.update { it.ticked() }
            }
        }
    }
}
