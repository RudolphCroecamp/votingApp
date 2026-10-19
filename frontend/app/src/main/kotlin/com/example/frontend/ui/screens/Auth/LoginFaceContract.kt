package com.example.frontend.ui.screens.Auth

import androidx.annotation.StringRes

const val FACE_MAX_ATTEMPTS = 3
const val FACE_LOCKOUT_SECONDS = 15 * 60

data class LoginFaceUiState(
    val idNumber: String = "",
    val isChecking: Boolean = false,
    val isVerified: Boolean = false,
    val attemptsUsed: Int = 0,
    val lockSecondsRemaining: Int = 0,
    val didNotMatch: Boolean = false,
    @param:StringRes val captureProblem: Int? = null
) {
    val isLocked: Boolean
        get() = lockSecondsRemaining > 0

    val attemptsLeft: Int
        get() = (FACE_MAX_ATTEMPTS - attemptsUsed).coerceAtLeast(0)

    val cameraActive: Boolean
        get() = !isChecking && !isLocked && !isVerified && !didNotMatch && captureProblem == null

    val needsTicking: Boolean
        get() = lockSecondsRemaining > 0
}

fun LoginFaceUiState.checking(): LoginFaceUiState =
    copy(isChecking = true, didNotMatch = false, captureProblem = null)

fun LoginFaceUiState.matched(): LoginFaceUiState =
    copy(isChecking = false, isVerified = true, didNotMatch = false, captureProblem = null)

fun LoginFaceUiState.rejected(): LoginFaceUiState {
    val used = attemptsUsed + 1
    val lockedOut = used >= FACE_MAX_ATTEMPTS
    return copy(
        isChecking = false,
        didNotMatch = true,
        attemptsUsed = used,
        lockSecondsRemaining = if (lockedOut) FACE_LOCKOUT_SECONDS else lockSecondsRemaining
    )
}

fun LoginFaceUiState.withCaptureProblem(@StringRes messageRes: Int): LoginFaceUiState =
    copy(isChecking = false, captureProblem = messageRes)

fun LoginFaceUiState.retried(): LoginFaceUiState =
    if (isLocked) this else copy(didNotMatch = false, captureProblem = null)

fun LoginFaceUiState.ticked(): LoginFaceUiState {
    if (!needsTicking) return this
    val remaining = (lockSecondsRemaining - 1).coerceAtLeast(0)
    return copy(
        lockSecondsRemaining = remaining,
        attemptsUsed = if (remaining == 0) 0 else attemptsUsed,
        didNotMatch = if (remaining == 0) false else didNotMatch
    )
}

sealed interface LoginFaceUiEvent {
    data class OnFaceCaptured(val imagePath: String) : LoginFaceUiEvent
    data class OnCaptureProblem(@param:StringRes val messageRes: Int) : LoginFaceUiEvent
    data object OnRetry : LoginFaceUiEvent
}

fun formatCountdown(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return if (minutes > 0) "%d:%02d".format(minutes, seconds) else "${seconds}s"
}
