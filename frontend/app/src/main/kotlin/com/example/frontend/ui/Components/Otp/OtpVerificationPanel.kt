package com.example.frontend.ui.Components.Otp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.example.frontend.ui.Components.ButtonVariant
import com.example.frontend.ui.Components.DescriptionLabel
import com.example.frontend.ui.Components.IndeterminateLinearIndicator
import com.example.frontend.ui.Components.SimpleButton
import com.example.frontend.ui.Components.StatusLabel
import com.example.frontend.ui.Components.StatusType
import com.example.frontend.ui.theme.FrontendTheme
import com.example.frontend.ui.theme.MutedText
import com.example.frontend.ui.theme.Spacing

@Composable
fun OtpVerificationPanel(
    state: OtpState,
    onCodeChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onResend: () -> Unit,
    modifier: Modifier = Modifier,
    submitLabel: String = "Verify code",
    successMessage: String = "Code confirmed."
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.item),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        DescriptionLabel(
            text = "We sent a $OTP_LENGTH digit code to ${state.destination}.",
            modifier = Modifier.fillMaxWidth(),
            color = MutedText,
            textAlign = TextAlign.Center
        )

        OtpCodeInput(
            code = state.code,
            onCodeChange = onCodeChange,
            enabled = !state.isLocked && !state.isVerifying && !state.isVerified,
            isError = state.error != null,
            autoFocus = !state.isLocked,
            onCompleted = { onSubmit() }
        )

        val errorMessage = otpErrorMessage(state)
        when {
            state.isVerified -> StatusLabel(
                message = successMessage,
                type = StatusType.Success,
                modifier = Modifier.fillMaxWidth()
            )
            errorMessage != null -> StatusLabel(
                message = errorMessage,
                type = if (state.isLocked) StatusType.Warning else StatusType.Error,
                modifier = Modifier.fillMaxWidth()
            )
            state.expirySecondsRemaining > 0 -> StatusLabel(
                message = "This code expires in ${formatCountdown(state.expirySecondsRemaining)}.",
                type = StatusType.Info,
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (state.isVerifying) {
            IndeterminateLinearIndicator(label = "Checking your code…")
        }

        SimpleButton(
            label = submitLabel,
            onClick = onSubmit,
            modifier = Modifier.fillMaxWidth(),
            variant = ButtonVariant.Tertiary,
            enabled = state.canSubmit
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            DescriptionLabel(
                text = "Did not get it?",
                color = MutedText,
                textAlign = TextAlign.Center
            )
            TextButton(onClick = onResend, enabled = state.canResend) {
                Text(
                    text = resendLabel(state),
                    style = MaterialTheme.typography.labelLarge,
                    color = if (state.canResend) MaterialTheme.colorScheme.primary else MutedText
                )
            }
        }
    }
}

private fun resendLabel(state: OtpState): String = when {
    state.isLocked -> "Resend locked"
    state.resendSecondsRemaining > 0 -> "Resend in ${formatCountdown(state.resendSecondsRemaining)}"
    else -> "Resend code"
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun OtpVerificationPanelWaitingPreview() {
    FrontendTheme {
        OtpVerificationPanel(
            state = OtpState(
                destination = "•••• ••• 5678",
                code = "123",
                resendSecondsRemaining = 24,
                expirySecondsRemaining = 272
            ),
            onCodeChange = {},
            onSubmit = {},
            onResend = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun OtpVerificationPanelWrongCodePreview() {
    FrontendTheme {
        OtpVerificationPanel(
            state = OtpState(
                destination = "•••• ••• 5678",
                attemptsUsed = 1,
                error = OtpError.WrongCode,
                resendSecondsRemaining = 0,
                expirySecondsRemaining = 180
            ),
            onCodeChange = {},
            onSubmit = {},
            onResend = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun OtpVerificationPanelLockedPreview() {
    FrontendTheme {
        OtpVerificationPanel(
            state = OtpState(
                destination = "•••• ••• 5678",
                attemptsUsed = OTP_MAX_ATTEMPTS,
                error = OtpError.TooManyAttempts,
                lockSecondsRemaining = OTP_LOCKOUT_SECONDS
            ),
            onCodeChange = {},
            onSubmit = {},
            onResend = {}
        )
    }
}
