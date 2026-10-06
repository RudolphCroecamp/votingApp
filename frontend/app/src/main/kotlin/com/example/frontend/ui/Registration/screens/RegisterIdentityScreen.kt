package com.example.frontend.ui.Registration.screens

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.frontend.ui.Auth.ID_NUMBER_MAX_LENGTH
import com.example.frontend.ui.Auth.PASSPORT_NUMBER_MAX_LENGTH
import com.example.frontend.ui.Components.AppTextField
import com.example.frontend.ui.Components.AppTopBar
import com.example.frontend.ui.Components.ButtonVariant
import com.example.frontend.ui.Components.DescriptionLabel
import com.example.frontend.ui.Components.IconLabel
import com.example.frontend.ui.Components.SimpleButton
import com.example.frontend.ui.Components.StatusType
import com.example.frontend.ui.Components.SuccessCheckmarkBadge
import com.example.frontend.ui.Registration.IdentityDocumentType
import com.example.frontend.ui.Registration.RegistrationUiEvent
import com.example.frontend.ui.Registration.RegistrationUiState
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
        topBar = { AppTopBar(title = "Verify Identity", onBackClick = onBackClick) }
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
                step = 2,
                heading = "Identity Document",
                description = "Pick your document type and capture a clear photo of the front."
            )

            Spacer(modifier = Modifier.height(Spacing.small))

            DescriptionLabel(
                text = "Document Type",
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.labelLarge
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.item)
            ) {
                DocumentOptionChip(
                    label = "National ID",
                    isSelected = uiState.documentType == IdentityDocumentType.NATIONAL_ID,
                    onClick = {
                        onEvent(RegistrationUiEvent.OnDocumentTypeSelected(IdentityDocumentType.NATIONAL_ID))
                    },
                    modifier = Modifier.weight(1f)
                )
                DocumentOptionChip(
                    label = "Passport",
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
                    IdentityDocumentType.NATIONAL_ID -> "ID Number"
                    IdentityDocumentType.PASSPORT -> "Passport Number"
                },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = Icons.Default.Badge,
                maxLength = when (uiState.documentType) {
                    IdentityDocumentType.NATIONAL_ID -> ID_NUMBER_MAX_LENGTH
                    IdentityDocumentType.PASSPORT -> PASSPORT_NUMBER_MAX_LENGTH
                }
            )

            DescriptionLabel(
                text = "Document Photo",
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.labelLarge
            )

            DocumentCaptureBox(
                captured = uiState.documentFrontImagePath != null,
                onCapture = { onEvent(RegistrationUiEvent.OnDocumentCaptured(CAPTURED_DOCUMENT_PATH)) },
                modifier = Modifier.fillMaxWidth()
            )

            IconLabel(
                message = if (uiState.documentFrontImagePath != null) {
                    "Document captured. Tap it again to retake the photo."
                } else {
                    "Lay the document flat, fill the frame, and avoid glare on the photo page."
                },
                type = if (uiState.documentFrontImagePath != null) StatusType.Success else StatusType.Info,
                modifier = Modifier.fillMaxWidth()
            )

            SimpleButton(
                label = if (uiState.isLoading) "Verifying…" else "Save & Verify Identity",
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
        modifier = modifier.clickable(onClick = onClick),
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
        modifier = modifier.clickable(onClick = onCapture),
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
                    DescriptionLabel(text = "Document captured", color = MutedText)
                } else {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                    DescriptionLabel(text = "Tap to capture front of document", color = MutedText)
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
