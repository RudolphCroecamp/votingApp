package com.example.frontend.ui.screens.Splash

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.frontend.data.AppContainer
import com.example.frontend.data.auth.AuthException
import com.example.frontend.data.auth.AuthRepository
import com.example.frontend.data.auth.TokenStore
import com.example.frontend.data.auth.VoterProfile
import com.example.frontend.data.health.HealthRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val TAG = "Session"

sealed interface SessionState {
    data object Checking : SessionState
    data object SignedIn : SessionState
    data object SignedOut : SessionState
    data object Offline : SessionState
}

class SessionViewModel(
    private val authRepository: AuthRepository = AppContainer.authRepository,
    private val healthRepository: HealthRepository = AppContainer.healthRepository,
    private val tokenStore: TokenStore = AppContainer.tokenStore
) : ViewModel() {

    private val _state = MutableStateFlow<SessionState>(SessionState.Checking)
    val state: StateFlow<SessionState> = _state.asStateFlow()

    private val _profile = MutableStateFlow<VoterProfile?>(null)
    val profile: StateFlow<VoterProfile?> = _profile.asStateFlow()

    private var checkJob: Job? = null

    fun check() {
        if (checkJob?.isActive == true) return
        _state.value = SessionState.Checking
        checkJob = viewModelScope.launch {
            _state.value = try {
                resolve().also { if (it == SessionState.SignedIn) loadProfile() }
            } catch (e: CancellationException) {
                throw e
            } catch (e: AuthException) {
                Log.w(TAG, "Session check failed", e)
                SessionState.Offline
            }
        }
    }

    fun onSignedIn() {
        _state.value = SessionState.SignedIn
        loadProfile()
    }

    private fun loadProfile() {
        val token = tokenStore.token ?: return
        viewModelScope.launch {
            try {
                authRepository.profile(token)?.let { _profile.value = it }
            } catch (e: CancellationException) {
                throw e
            } catch (e: AuthException) {
                Log.w(TAG, "Profile load failed", e)
            }
        }
    }

    fun signOut() {
        checkJob?.cancel()
        _profile.value = null
        val token = tokenStore.token
        tokenStore.clear()
        _state.value = SessionState.SignedOut
        if (token == null) return
        viewModelScope.launch {
            try {
                authRepository.logout(token)
            } catch (e: CancellationException) {
                throw e
            } catch (e: AuthException) {
                Log.w(TAG, "Server logout failed", e)
            }
        }
    }

    private suspend fun resolve(): SessionState {
        if (!healthRepository.isHealthy()) return SessionState.Offline
        val token = tokenStore.token ?: return SessionState.SignedOut
        if (authRepository.validate(token)) return SessionState.SignedIn
        val refreshed = authRepository.refresh(token)
        if (refreshed == null) {
            tokenStore.clear()
            return SessionState.SignedOut
        }
        tokenStore.save(refreshed)
        return SessionState.SignedIn
    }
}
