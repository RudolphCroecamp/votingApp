package com.example.frontend.ui.screens.Registration.screens

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
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
import com.example.frontend.ui.components.Camera.CameraCaptureStates
import com.example.frontend.ui.components.Camera.CameraView
import com.example.frontend.ui.components.Camera.CaptureState
import com.example.frontend.ui.components.Camera.evaluateCapture
import com.example.frontend.ui.components.Camera.rememberCameraPermission
import com.example.frontend.ui.components.DescriptionLabel
import com.example.frontend.ui.components.StatusLabel
import com.example.frontend.ui.components.SimpleButton
import com.example.frontend.ui.components.StatusType
import com.example.frontend.ui.components.SuccessCheckmarkBadge
import com.example.frontend.ui.screens.Registration.DocumentSide
import com.example.frontend.ui.screens.Registration.IdentityDocumentType
import com.example.frontend.ui.screens.Registration.RegistrationUiEvent
import com.example.frontend.ui.screens.Registration.RegistrationUiState
import com.example.frontend.ui.theme.BrandNavyDeep
import com.example.frontend.ui.theme.CardBorder
import com.example.frontend.ui.theme.FrontendTheme
import com.example.frontend.ui.theme.MutedText
import com.example.frontend.ui.theme.PageBackground
import com.example.frontend.ui.theme.Spacing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
private const val PREVIEW_DOCUMENT_PATH = "path/to/captured_document.jpg"

@Composable
fun RegisterIdentityScreen(
    uiState: RegistrationUiState,
    onEvent: (RegistrationUiEvent) -> Unit,
    onBackClick: () -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = PageBackground
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val inPreview = LocalInspectionMode.current
    val (cameraGranted, requestCamera) = rememberCameraPermission()

    var capturingSide by remember { mutableStateOf<DocumentSide?>(null) }
    var capture by remember { mutableStateOf<CaptureState>(CaptureState.Framing) }
    val bothCaptured = uiState.documentFrontImagePath != null && uiState.documentBackImagePath != null

    fun startCapture(side: DocumentSide) {
        if (!cameraGranted) requestCamera()
        capture = CaptureState.Framing
        capturingSide = side
    }

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
                step = 3,
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

            val side = capturingSide
            if (side != null && cameraGranted && !inPreview) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(0.85f)
                        .clip(MaterialTheme.shapes.medium)
                        .background(BrandNavyDeep)
                ) {
                    CameraView(
                        mode = CameraCaptureStates.ID_DOCUMENT,
                        state = capture,
                        onPhotoTaken = { photo -> capture = evaluateCapture(photo) },
                        onRetake = { capture = CaptureState.Framing },
                        onConfirm = {
                            val photo = (capture as? CaptureState.Captured)?.photo
                            if (photo != null) {
                                capture = CaptureState.Uploading
                                scope.launch {
                                    val path = saveDocumentImage(context, photo, side)
                                    onEvent(RegistrationUiEvent.OnDocumentCaptured(side, path))
                                    capturingSide = null
                                }
                            }
                        },
                        onError = { messageRes -> capture = CaptureState.Problem(messageRes) }
                    )
                }
            } else {
                DocumentCaptureBox(
                    captured = uiState.documentFrontImagePath != null,
                    capturedLabel = stringResource(R.string.register_document_front_captured),
                    captureLabel = stringResource(R.string.register_document_tap_capture),
                    onCapture = { startCapture(DocumentSide.FRONT) },
                    modifier = Modifier.fillMaxWidth()
                )
                DocumentCaptureBox(
                    captured = uiState.documentBackImagePath != null,
                    capturedLabel = stringResource(R.string.register_document_back_captured),
                    captureLabel = stringResource(R.string.register_document_tap_capture_back),
                    onCapture = { startCapture(DocumentSide.BACK) },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            val problem = (capture as? CaptureState.Problem)?.takeIf { capturingSide != null }
            StatusLabel(
                message = when {
                    problem != null -> stringResource(problem.messageRes)
                    bothCaptured -> stringResource(R.string.register_document_captured_retake)
                    else -> stringResource(R.string.register_document_hint)
                },
                type = when {
                    problem != null -> StatusType.Error
                    bothCaptured -> StatusType.Success
                    else -> StatusType.Info
                },
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
                enabled = uiState.canSubmitIdentity && capturingSide == null
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

private suspend fun saveDocumentImage(context: Context, photo: Bitmap, side: DocumentSide): String =
    withContext(Dispatchers.IO) {
        val name = when (side) {
            DocumentSide.FRONT -> "id_front.jpg"
            DocumentSide.BACK -> "id_back.jpg"
        }
        val file = File(context.cacheDir, name)
        file.outputStream().use { out -> photo.compress(Bitmap.CompressFormat.JPEG, 85, out) }
        file.absolutePath
    }

@Composable
private fun DocumentCaptureBox(
    captured: Boolean,
    capturedLabel: String,
    captureLabel: String,
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
                    DescriptionLabel(text = capturedLabel, color = MutedText)
                } else {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                    DescriptionLabel(text = captureLabel, color = MutedText)
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
                documentFrontImagePath = PREVIEW_DOCUMENT_PATH,
                documentBackImagePath = PREVIEW_DOCUMENT_PATH
            ),
            onEvent = {},
            onBackClick = {},
            onContinue = {}
        )
    }
}
