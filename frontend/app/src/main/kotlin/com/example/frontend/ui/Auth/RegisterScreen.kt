package com.example.frontend.ui.Auth

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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.frontend.ui.Components.AppTextField
import com.example.frontend.ui.Components.BrandHeader
import com.example.frontend.ui.Components.ButtonVariant
import com.example.frontend.ui.Components.DescriptionLabel
import com.example.frontend.ui.Components.HeadingLabel
import com.example.frontend.ui.Components.IconLabel
import com.example.frontend.ui.Components.SimpleButton
import com.example.frontend.ui.Components.StatusType
import com.example.frontend.ui.theme.FrontendTheme
import com.example.frontend.ui.theme.MutedText
import com.example.frontend.ui.theme.PageBackground
import com.example.frontend.ui.theme.Spacing

@Composable
fun RegisterScreen(
    onContinue: (fullName: String, idNumber: String, email: String) -> Unit,
    onSignIn: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = PageBackground
) {
    var fullName by remember { mutableStateOf("") }
    var idNumber by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    val canContinue = fullName.isNotBlank() && idNumber.isNotBlank() && email.isNotBlank()

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
                value = fullName,
                onValueChange = { fullName = it },
                placeholder = "Full Name",
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = Icons.Default.Person
            )

            AppTextField(
                value = idNumber,
                onValueChange = { idNumber = it },
                placeholder = "ID Number / Passport Number",
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = Icons.Default.AccountBox,
                maxLength = ID_NUMBER_MAX_LENGTH
            )

            AppTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = "Email Address",
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = Icons.Default.Email,
                keyboardType = KeyboardType.Email
            )

            SimpleButton(
                label = "Continue",
                onClick = { onContinue(fullName, idNumber, email) },
                modifier = Modifier.fillMaxWidth(),
                variant = ButtonVariant.Tertiary,
                enabled = canContinue
            )

            IconLabel(
                message = "Next you will scan your face. That scan replaces a password when you sign in.",
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
    totalSteps: Int = 3
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.tight)
    ) {
        DescriptionLabel(
            text = "Step $step of $totalSteps",
            modifier = Modifier.fillMaxWidth(),
            color = MutedText,
            style = MaterialTheme.typography.labelLarge
        )
        HeadingLabel(text = heading, modifier = Modifier.fillMaxWidth())
        DescriptionLabel(
            text = description,
            modifier = Modifier.fillMaxWidth(),
            color = MutedText
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RegisterScreenPreview() {
    FrontendTheme {
        RegisterScreen(
            onContinue = { _, _, _ -> },
            onSignIn = {}
        )
    }
}
