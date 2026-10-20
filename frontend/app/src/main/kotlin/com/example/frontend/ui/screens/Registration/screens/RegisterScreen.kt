package com.example.frontend.ui.screens.Registration.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Email
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.example.frontend.R
import com.example.frontend.ui.screens.Auth.ID_NUMBER_MAX_LENGTH
import com.example.frontend.ui.components.AppTextField
import com.example.frontend.ui.components.AppTopBar
import com.example.frontend.ui.components.ButtonVariant
import com.example.frontend.ui.components.DescriptionLabel
import com.example.frontend.ui.components.HeadingLabel
import com.example.frontend.ui.components.IdNumberFeedback
import com.example.frontend.ui.components.StatusLabel
import com.example.frontend.ui.components.SectionHeading
import com.example.frontend.ui.components.SimpleButton
import com.example.frontend.ui.components.StatusType
import com.example.frontend.ui.screens.Registration.DetailsIssue
import com.example.frontend.ui.screens.Registration.Province
import com.example.frontend.ui.screens.Registration.REGISTRATION_STEPS
import com.example.frontend.ui.screens.Registration.RegistrationUiEvent
import com.example.frontend.ui.screens.Registration.RegistrationUiState
import com.example.frontend.ui.screens.Registration.components.AddressFields
import com.example.frontend.ui.screens.Registration.components.RegistrationStepper
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
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = PageBackground
) {
    val issues = if (uiState.showDetailsIssues) uiState.detailsIssues else emptyList()
    val errorFor: @Composable (DetailsIssue) -> String? = { issue ->
        if (issue in issues) stringResource(issue.messageRes) else null
    }

    Scaffold(
        modifier = modifier,
        containerColor = backgroundColor,
        topBar = {
            AppTopBar(
                title = stringResource(R.string.register_title),
                subtitle = stringResource(R.string.register_step_of, 1, REGISTRATION_STEPS),
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
                step = 1,
                heading = stringResource(R.string.register_details_heading),
                description = stringResource(R.string.register_details_description)
            )

            AppTextField(
                value = uiState.fullNames,
                onValueChange = { onEvent(RegistrationUiEvent.OnFullNamesChanged(it)) },
                placeholder = stringResource(R.string.field_full_name),
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = Icons.Default.Person,
                errorText = errorFor(DetailsIssue.NAME_MISSING) ?: errorFor(DetailsIssue.SURNAME_MISSING)
            )

            AppTextField(
                value = uiState.idNumber,
                onValueChange = { onEvent(RegistrationUiEvent.OnIdNumberChanged(it)) },
                placeholder = stringResource(R.string.field_id_or_passport),
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = Icons.Default.AccountBox,
                maxLength = ID_NUMBER_MAX_LENGTH,
                errorText = errorFor(DetailsIssue.ID_MISSING) ?: errorFor(DetailsIssue.ID_LENGTH)
                    ?: errorFor(DetailsIssue.ID_BIRTH_DATE) ?: errorFor(DetailsIssue.ID_CHECKSUM)
                    ?: errorFor(DetailsIssue.ID_NOT_CITIZEN) ?: errorFor(DetailsIssue.ID_TOO_YOUNG)
            )
            // Decoded as they type: birth date, gender and citizenship, or what is wrong.
            IdNumberFeedback(idNumber = uiState.idNumber)

            AppTextField(
                value = uiState.phone,
                onValueChange = { onEvent(RegistrationUiEvent.OnPhoneChanged(it)) },
                placeholder = stringResource(R.string.field_cellphone),
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = Icons.Default.Phone,
                keyboardType = KeyboardType.Phone,
                errorText = errorFor(DetailsIssue.PHONE_MISSING) ?: errorFor(DetailsIssue.PHONE_INVALID)
            )

            AppTextField(
                value = uiState.email,
                onValueChange = { onEvent(RegistrationUiEvent.OnEmailChanged(it)) },
                placeholder = stringResource(R.string.field_email_optional),
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = Icons.Default.Email,
                keyboardType = KeyboardType.Email,
                errorText = errorFor(DetailsIssue.EMAIL_INVALID)
            )

            AddressFields(
                uiState = uiState,
                onEvent = onEvent,
                modifier = Modifier.fillMaxWidth()
            )

            if (issues.isNotEmpty()) {
                DetailsIssuesCard(issues = issues)
            }

            SimpleButton(
                label = stringResource(R.string.action_continue),
                onClick = {
                    if (uiState.canSubmitDetails) onContinue()
                    else onEvent(RegistrationUiEvent.OnDetailsSubmitAttempted)
                },
                modifier = Modifier.fillMaxWidth(),
                variant = ButtonVariant.Tertiary
            )

            StatusLabel(
                message = stringResource(R.string.register_next_steps_info),
                type = StatusType.Info,
                modifier = Modifier.fillMaxWidth()
            )

            DescriptionLabel(
                text = stringResource(R.string.register_details_privacy),
                modifier = Modifier.fillMaxWidth(),
                color = MutedText,
                textAlign = TextAlign.Center
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                DescriptionLabel(
                    text = stringResource(R.string.register_have_account),
                    color = MutedText,
                    textAlign = TextAlign.Center
                )
                TextButton(onClick = onSignIn) {
                    Text(
                        text = stringResource(R.string.register_sign_in),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailsIssuesCard(issues: List<DetailsIssue>) {
    StatusLabel(
        message = buildString {
            append(stringResource(R.string.details_issues_title))
            issues.forEach { append("\n• ").append(stringResource(it.messageRes)) }
        },
        type = StatusType.Warning,
        modifier = Modifier
            .fillMaxWidth()
            .semantics { liveRegion = LiveRegionMode.Polite }
    )
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
            SectionHeading(title = heading, description = description)
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
            onSignIn = {},
            onBackClick = {}
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
                province = Province.EASTERN_CAPE,
                street = "12 Govan Mbeki Avenue",
                city = "Gqeberha",
                postalCode = "6001"
            ),
            onEvent = {},
            onContinue = {},
            onSignIn = {},
            onBackClick = {}
        )
    }
}
