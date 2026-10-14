package com.example.frontend.ui.screens.Auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val DEMO_ENROLLED_ID = "0203015800085"

class LoginFaceViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(LoginFaceUiState())
    val uiState: StateFlow<LoginFaceUiState> = _uiState.asStateFlow()

    private var lockTicker: Job? = null

    fun start(idNumber: String) {
        if (_uiState.value.idNumber == idNumber) return
        _uiState.value = LoginFaceUiState(idNumber = idNumber)
    }

    fun onEvent(event: LoginFaceUiEvent) {
        when (event) {
            is LoginFaceUiEvent.OnFaceCaptured -> matchFace()
            is LoginFaceUiEvent.OnCaptureProblem -> _uiState.update {
                it.withCaptureProblem(event.message)
            }
            LoginFaceUiEvent.OnRetry -> _uiState.update { it.retried() }
        }
    }

    private fun matchFace() {
        if (!_uiState.value.cameraActive) return
        viewModelScope.launch {
            _uiState.update { it.checking() }
            delay(1200) // Simulate the backend matching the scan against the registration photo
            _uiState.update { state ->
                if (state.idNumber == DEMO_ENROLLED_ID) state.matched() else state.rejected()
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
