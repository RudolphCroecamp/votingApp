package com.example.frontend.ui.Components.Otp

const val OTP_LENGTH = 6
const val OTP_RESEND_COOLDOWN_SECONDS = 30
const val OTP_EXPIRY_SECONDS = 5 * 60
const val OTP_MAX_ATTEMPTS = 3
const val OTP_LOCKOUT_SECONDS = 15 * 60

enum class OtpError {
    WrongCode,
    Expired,
    TooManyAttempts
}

data class OtpState(
    val code: String = "",
    val destination: String = "",
    val error: OtpError? = null,
    val attemptsUsed: Int = 0,
    val resendSecondsRemaining: Int = 0,
    val expirySecondsRemaining: Int = 0,
    val lockSecondsRemaining: Int = 0,
    val isVerifying: Boolean = false,
    val isVerified: Boolean = false
) {
    val isLocked: Boolean
        get() = lockSecondsRemaining > 0

    val isExpired: Boolean
        get() = error == OtpError.Expired

    val attemptsLeft: Int
        get() = (OTP_MAX_ATTEMPTS - attemptsUsed).coerceAtLeast(0)

    val canSubmit: Boolean
        get() = code.length == OTP_LENGTH && !isVerifying && !isLocked && !isExpired && !isVerified

    val canResend: Boolean
        get() = resendSecondsRemaining == 0 && !isLocked && !isVerifying && !isVerified

    val needsTicking: Boolean
        get() = !isVerified &&
            (resendSecondsRemaining > 0 || expirySecondsRemaining > 0 || lockSecondsRemaining > 0)
}

fun OtpState.withCode(value: String): OtpState {
    if (isLocked || isVerifying || isVerified) return this
    val digits = value.filter { it.isDigit() }.take(OTP_LENGTH)
    return copy(
        code = digits,
        error = if (error == OtpError.WrongCode) null else error
    )
}

fun OtpState.sent(destination: String = this.destination): OtpState = OtpState(
    destination = destination,
    attemptsUsed = if (isLocked) attemptsUsed else 0,
    resendSecondsRemaining = OTP_RESEND_COOLDOWN_SECONDS,
    expirySecondsRemaining = OTP_EXPIRY_SECONDS,
    lockSecondsRemaining = lockSecondsRemaining
)

fun OtpState.verifying(): OtpState = copy(isVerifying = true, error = null)

fun OtpState.verified(): OtpState = copy(
    isVerifying = false,
    isVerified = true,
    error = null,
    resendSecondsRemaining = 0,
    expirySecondsRemaining = 0
)

fun OtpState.rejected(): OtpState {
    val used = attemptsUsed + 1
    val lockedOut = used >= OTP_MAX_ATTEMPTS
    return copy(
        code = "",
        isVerifying = false,
        attemptsUsed = used,
        error = if (lockedOut) OtpError.TooManyAttempts else OtpError.WrongCode,
        lockSecondsRemaining = if (lockedOut) OTP_LOCKOUT_SECONDS else lockSecondsRemaining,
        expirySecondsRemaining = if (lockedOut) 0 else expirySecondsRemaining
    )
}

fun OtpState.ticked(): OtpState {
    if (!needsTicking) return this

    val lock = (lockSecondsRemaining - 1).coerceAtLeast(0)
    if (lockSecondsRemaining > 0) {
        return copy(
            lockSecondsRemaining = lock,
            resendSecondsRemaining = (resendSecondsRemaining - 1).coerceAtLeast(0),
            error = if (lock == 0) null else error,
            attemptsUsed = if (lock == 0) 0 else attemptsUsed
        )
    }

    val expiry = (expirySecondsRemaining - 1).coerceAtLeast(0)
    return copy(
        resendSecondsRemaining = (resendSecondsRemaining - 1).coerceAtLeast(0),
        expirySecondsRemaining = expiry,
        error = if (expiry == 0 && expirySecondsRemaining > 0) OtpError.Expired else error,
        code = if (expiry == 0 && expirySecondsRemaining > 0) "" else code
    )
}

fun formatCountdown(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return if (minutes > 0) "%d:%02d".format(minutes, seconds) else "${seconds}s"
}

fun maskDestination(phone: String): String {
    val digits = phone.filter { it.isDigit() }
    if (digits.length < 4) return "your cellphone"
    return "•••• ••• " + digits.takeLast(4)
}

fun otpErrorMessage(state: OtpState): String? = when (state.error) {
    OtpError.WrongCode -> "That code is not right. ${state.attemptsLeft} tries left before we lock the step."
    OtpError.Expired -> "That code has expired. Tap resend to get a new one."
    OtpError.TooManyAttempts ->
        "Too many wrong codes. Try again in ${formatCountdown(state.lockSecondsRemaining)}."
    null -> null
}
