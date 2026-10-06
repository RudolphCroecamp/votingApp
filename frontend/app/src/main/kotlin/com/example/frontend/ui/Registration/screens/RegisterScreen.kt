package com.example.frontend.ui.Registration.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.MarkunreadMailbox
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.frontend.ui.Auth.ID_NUMBER_MAX_LENGTH
import com.example.frontend.ui.Components.AppTextField
import com.example.frontend.ui.Components.BrandHeader
import com.example.frontend.ui.Components.ButtonVariant
import com.example.frontend.ui.Components.DescriptionLabel
import com.example.frontend.ui.Components.HeadingLabel
import com.example.frontend.ui.Components.IconLabel
import com.example.frontend.ui.Components.SimpleButton
import com.example.frontend.ui.Components.StatusType
import com.example.frontend.ui.Registration.REGISTRATION_STEPS
import com.example.frontend.ui.Registration.RegistrationUiEvent
import com.example.frontend.ui.Registration.RegistrationUiState
import com.example.frontend.ui.Registration.components.RegistrationStepper
import com.example.frontend.ui.theme.FrontendTheme
import com.example.frontend.ui.theme.MutedText
import com.example.frontend.ui.theme.PageBackground
import com.example.frontend.ui.theme.Spacing
@Composable
fun RegisterScreen(
    uiState: RegistrationUiState,
    onEvent: (RegistrationUiEvent) -> Unit,
    onContinue: () -> Unit,
    onSignIn: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = PageBackground
) {
    Scaffold(
        modifier = modifier,
        containerColor = backgroundColor
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.screen, vertical = Spacing.section),
            verticalArrangement = Arrangement.spacedBy(Spacing.item),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BrandHeader(
                title = "South Africa\nNational Voting App",
                subtitle = "Secure  ·  Anonymous  ·  Verifiable",
                logoSize = 72.dp
            )

            Spacer(modifier = Modifier.height(Spacing.small))

            RegisterStepHeader(
                step = 1,
                heading = "Your Details",
                description = "Register with your South African ID or passport to get your ballot."
            )

            AppTextField(
                value = uiState.fullNames,
                onValueChange = { onEvent(RegistrationUiEvent.OnFullNamesChanged(it)) },
                placeholder = "Full Name",
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = Icons.Default.Person
            )

            AppTextField(
                value = uiState.idNumber,
                onValueChange = { onEvent(RegistrationUiEvent.OnIdNumberChanged(it)) },
                placeholder = "ID Number / Passport Number",
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = Icons.Default.AccountBox,
                maxLength = ID_NUMBER_MAX_LENGTH
            )

            AppTextField(
                value = uiState.phone,
                onValueChange = { onEvent(RegistrationUiEvent.OnPhoneChanged(it)) },
                placeholder = "Cellphone Number",
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = Icons.Default.Phone,
                keyboardType = KeyboardType.Phone
            )

            AppTextField(
                value = uiState.email,
                onValueChange = { onEvent(RegistrationUiEvent.OnEmailChanged(it)) },
                placeholder = "Email Address (Optional)",
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = Icons.Default.Email,
                keyboardType = KeyboardType.Email
            )

            AppTextField(
                value = uiState.postalCode,
                onValueChange = { onEvent(RegistrationUiEvent.OnPostalCodeChanged(it)) },
                placeholder = "Postal Code",
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = Icons.Default.MarkunreadMailbox,
                keyboardType = KeyboardType.Number
            )

            SimpleButton(
                label = "Continue",
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth(),
                variant = ButtonVariant.Tertiary,
                enabled = uiState.canSubmitDetails
            )

            IconLabel(
                message = "Next you will capture your ID document, then scan your face. " +
                    "That scan replaces a password when you sign in.",
                type = StatusType.Info,
                modifier = Modifier.fillMaxWidth()
            )

            DescriptionLabel(
                text = "Your details are used only to confirm you are on the voters' roll. " +
                    "Your ballot stays anonymous.",
                modifier = Modifier.fillMaxWidth(),
                color = MutedText,
                textAlign = TextAlign.Center
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                DescriptionLabel(
                    text = "Already have an account?",
                    color = MutedText,
                    textAlign = TextAlign.Center
                )
                TextButton(onClick = onSignIn) {
                    Text(
                        text = "Sign In",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
internal fun RegisterStepHeader(
    step: Int,
    heading: String,
    description: String,
    modifier: Modifier = Modifier,
    totalSteps: Int = REGISTRATION_STEPS
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.item)
    ) {
        RegistrationStepper(currentStep = step, totalSteps = totalSteps)
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.tight)) {
            HeadingLabel(text = heading, modifier = Modifier.fillMaxWidth())
            DescriptionLabel(
                text = description,
                modifier = Modifier.fillMaxWidth(),
                color = MutedText
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RegisterScreenEmptyPreview() {
    FrontendTheme {
        RegisterScreen(
            uiState = RegistrationUiState(),
            onEvent = {},
            onContinue = {},
            onSignIn = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RegisterScreenFilledPreview() {
    FrontendTheme {
        RegisterScreen(
            uiState = RegistrationUiState(
                fullNames = "Adriaan van Wyk",
                idNumber = "0203015800085",
                phone = "0712345678",
                email = "adriaan@example.com",
                postalCode = "6001"
            ),
            onEvent = {},
            onContinue = {},
            onSignIn = {}
        )
    }
}
