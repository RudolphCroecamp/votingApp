package com.example.frontend.ui.screens.Registration.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.example.frontend.ui.Components.AppTopBar
import com.example.frontend.ui.Components.DescriptionLabel
import com.example.frontend.ui.Components.Otp.OTP_MAX_ATTEMPTS
import com.example.frontend.ui.Components.Otp.OTP_LOCKOUT_SECONDS
import com.example.frontend.ui.Components.Otp.OtpError
import com.example.frontend.ui.Components.Otp.OtpState
import com.example.frontend.ui.Components.Otp.OtpVerificationPanel
import com.example.frontend.ui.screens.Registration.OTP_STEP
import com.example.frontend.ui.screens.Registration.REGISTRATION_STEPS
import com.example.frontend.ui.screens.Registration.RegistrationUiEvent
import com.example.frontend.ui.screens.Registration.RegistrationUiState
import com.example.frontend.ui.theme.FrontendTheme
import com.example.frontend.ui.theme.MutedText
import com.example.frontend.ui.theme.PageBackground
import com.example.frontend.ui.theme.Spacing
import kotlinx.coroutines.delay

@Composable
fun RegisterOtpScreen(
    uiState: RegistrationUiState,
    onEvent: (RegistrationUiEvent) -> Unit,
    onBackClick: () -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = PageBackground
) {
    LaunchedEffect(uiState.otp.isVerified) {
        if (uiState.otp.isVerified) {
            delay(700)
            onContinue()
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = backgroundColor,
        topBar = {
            AppTopBar(
                title = "Confirm your cellphone",
                subtitle = "Step $OTP_STEP of $REGISTRATION_STEPS",
                onBackClick = onBackClick
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.screen, vertical = Spacing.card),
            verticalArrangement = Arrangement.spacedBy(Spacing.item),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            RegisterStepHeader(
                step = OTP_STEP,
                heading = "Enter your code",
                description = "This proves the cellphone number you gave us belongs to you."
            )

            OtpVerificationPanel(
                state = uiState.otp,
                onCodeChange = { onEvent(RegistrationUiEvent.OnOtpChanged(it)) },
                onSubmit = { onEvent(RegistrationUiEvent.OnOtpSubmitted) },
                onResend = { onEvent(RegistrationUiEvent.OnOtpRequested) },
                submitLabel = "Verify and continue",
                successMessage = "Cellphone number confirmed."
            )

            DescriptionLabel(
                text = "Standard network rates apply. We only use this number to confirm it is you.",
                modifier = Modifier.fillMaxWidth(),
                color = MutedText,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RegisterOtpScreenPreview() {
    FrontendTheme {
        RegisterOtpScreen(
            uiState = RegistrationUiState(
                currentStep = OTP_STEP,
                phone = "0712345678",
                otp = OtpState(
                    destination = "•••• ••• 5678",
                    code = "1234",
                    resendSecondsRemaining = 18,
                    expirySecondsRemaining = 265
                )
            ),
            onEvent = {},
            onBackClick = {},
            onContinue = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RegisterOtpScreenWrongCodePreview() {
    FrontendTheme {
        RegisterOtpScreen(
            uiState = RegistrationUiState(
                currentStep = OTP_STEP,
                phone = "0712345678",
                otp = OtpState(
                    destination = "•••• ••• 5678",
                    attemptsUsed = 1,
                    error = OtpError.WrongCode,
                    expirySecondsRemaining = 140
                )
            ),
            onEvent = {},
            onBackClick = {},
            onContinue = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RegisterOtpScreenExpiredPreview() {
    FrontendTheme {
        RegisterOtpScreen(
            uiState = RegistrationUiState(
                currentStep = OTP_STEP,
                phone = "0712345678",
                otp = OtpState(destination = "•••• ••• 5678", error = OtpError.Expired)
            ),
            onEvent = {},
            onBackClick = {},
            onContinue = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RegisterOtpScreenLockedPreview() {
    FrontendTheme {
        RegisterOtpScreen(
            uiState = RegistrationUiState(
                currentStep = OTP_STEP,
                phone = "0712345678",
                otp = OtpState(
                    destination = "•••• ••• 5678",
                    attemptsUsed = OTP_MAX_ATTEMPTS,
                    error = OtpError.TooManyAttempts,
                    lockSecondsRemaining = OTP_LOCKOUT_SECONDS
                )
            ),
            onEvent = {},
            onBackClick = {},
            onContinue = {}
        )
    }
}
