package com.example.frontend.ui.screens.Registration.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.frontend.R
import com.example.frontend.ui.screens.Auth.ID_NUMBER_MAX_LENGTH
import com.example.frontend.ui.screens.Auth.PASSPORT_NUMBER_MAX_LENGTH
import com.example.frontend.ui.components.AppTextField
import com.example.frontend.ui.components.AppTopBar
import com.example.frontend.ui.components.ButtonVariant
import com.example.frontend.ui.components.DescriptionLabel
import com.example.frontend.ui.components.StatusLabel
import com.example.frontend.ui.components.SimpleButton
import com.example.frontend.ui.components.StatusType
import com.example.frontend.ui.components.SuccessCheckmarkBadge
import com.example.frontend.ui.screens.Registration.IdentityDocumentType
import com.example.frontend.ui.screens.Registration.RegistrationUiEvent
import com.example.frontend.ui.screens.Registration.RegistrationUiState
import com.example.frontend.ui.theme.CardBorder
import com.example.frontend.ui.theme.FrontendTheme
import com.example.frontend.ui.theme.MutedText
import com.example.frontend.ui.theme.PageBackground
import com.example.frontend.ui.theme.Spacing
/** Simulated capture path until CameraX is wired up. */
private const val CAPTURED_DOCUMENT_PATH = "path/to/captured_document.jpg"

@Composable
fun RegisterIdentityScreen(
    uiState: RegistrationUiState,
    onEvent: (RegistrationUiEvent) -> Unit,
    onBackClick: () -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = PageBackground
) {
    Scaffold(
        modifier = modifier,
        containerColor = backgroundColor,
        topBar = { AppTopBar(title = stringResource(R.string.register_identity_title), onBackClick = onBackClick) }
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
            RegisterStepHeader(
                step = 4,
                heading = stringResource(R.string.register_identity_heading),
                description = stringResource(R.string.register_identity_description)
            )

            Spacer(modifier = Modifier.height(Spacing.small))

            DescriptionLabel(
                text = stringResource(R.string.register_document_type),
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.labelLarge
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.item)
            ) {
                DocumentOptionChip(
                    label = stringResource(R.string.register_document_national_id),
                    isSelected = uiState.documentType == IdentityDocumentType.NATIONAL_ID,
                    onClick = {
                        onEvent(RegistrationUiEvent.OnDocumentTypeSelected(IdentityDocumentType.NATIONAL_ID))
                    },
                    modifier = Modifier.weight(1f)
                )
                DocumentOptionChip(
                    label = stringResource(R.string.register_document_passport),
                    isSelected = uiState.documentType == IdentityDocumentType.PASSPORT,
                    onClick = {
                        onEvent(RegistrationUiEvent.OnDocumentTypeSelected(IdentityDocumentType.PASSPORT))
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            AppTextField(
                value = uiState.documentNumber,
                onValueChange = { onEvent(RegistrationUiEvent.OnDocumentNumberChanged(it)) },
                placeholder = when (uiState.documentType) {
                    IdentityDocumentType.NATIONAL_ID -> stringResource(R.string.field_id_number)
                    IdentityDocumentType.PASSPORT -> stringResource(R.string.field_passport_number)
                },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = Icons.Default.Badge,
                maxLength = when (uiState.documentType) {
                    IdentityDocumentType.NATIONAL_ID -> ID_NUMBER_MAX_LENGTH
                    IdentityDocumentType.PASSPORT -> PASSPORT_NUMBER_MAX_LENGTH
                }
            )

            DescriptionLabel(
                text = stringResource(R.string.register_document_photo),
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.labelLarge
            )

            DocumentCaptureBox(
                captured = uiState.documentFrontImagePath != null,
                onCapture = { onEvent(RegistrationUiEvent.OnDocumentCaptured(CAPTURED_DOCUMENT_PATH)) },
                modifier = Modifier.fillMaxWidth()
            )

            StatusLabel(
                message = if (uiState.documentFrontImagePath != null) {
                    stringResource(R.string.register_document_captured_retake)
                } else {
                    stringResource(R.string.register_document_hint)
                },
                type = if (uiState.documentFrontImagePath != null) StatusType.Success else StatusType.Info,
                modifier = Modifier.fillMaxWidth()
            )

            SimpleButton(
                label = stringResource(
                    if (uiState.isLoading) R.string.register_verifying else R.string.register_save_verify_identity
                ),
                onClick = {
                    onEvent(RegistrationUiEvent.OnSaveIdentityDetails)
                    onContinue()
                },
                modifier = Modifier.fillMaxWidth(),
                variant = ButtonVariant.Tertiary,
                enabled = uiState.canSubmitIdentity
            )

            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun DocumentOptionChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accent = MaterialTheme.colorScheme.primary

    Surface(
        modifier = modifier.clickable(onClick = onClick, role = Role.Button),
        shape = MaterialTheme.shapes.small,
        color = if (isSelected) accent.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface,
        border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, if (isSelected) accent else CardBorder)
    ) {
        Text(
            text = label,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = Spacing.item),
            style = MaterialTheme.typography.labelLarge,
            color = if (isSelected) accent else MutedText,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun DocumentCaptureBox(
    captured: Boolean,
    onCapture: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clickable(onClick = onCapture, role = Role.Button),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Spacing.small)
            ) {
                if (captured) {
                    SuccessCheckmarkBadge(size = 48.dp)
                    DescriptionLabel(text = stringResource(R.string.register_document_captured), color = MutedText)
                } else {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                    DescriptionLabel(text = stringResource(R.string.register_document_tap_capture), color = MutedText)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RegisterIdentityScreenEmptyPreview() {
    FrontendTheme {
        RegisterIdentityScreen(
            uiState = RegistrationUiState(),
            onEvent = {},
            onBackClick = {},
            onContinue = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RegisterIdentityScreenCapturedPreview() {
    FrontendTheme {
        RegisterIdentityScreen(
            uiState = RegistrationUiState(
                documentType = IdentityDocumentType.PASSPORT,
                documentNumber = "A01234567",
                documentFrontImagePath = CAPTURED_DOCUMENT_PATH
            ),
            onEvent = {},
            onBackClick = {},
            onContinue = {}
        )
    }
}
