package com.example.frontend.ui.screens.Auth

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.frontend.ui.Components.AppTopBar
import com.example.frontend.ui.Components.ButtonVariant
import com.example.frontend.ui.Components.Camera.CameraCaptureStates
import com.example.frontend.ui.Components.Camera.CameraView
import com.example.frontend.ui.Components.Camera.CaptureState
import com.example.frontend.ui.Components.Camera.evaluateFaceCapture
import com.example.frontend.ui.Components.Camera.rememberCameraPermission
import com.example.frontend.ui.Components.DescriptionLabel
import com.example.frontend.ui.Components.IndeterminateLinearIndicator
import com.example.frontend.ui.Components.Otp.formatCountdown
import com.example.frontend.ui.Components.SectionHeading
import com.example.frontend.ui.Components.SimpleButton
import com.example.frontend.ui.Components.StatusLabel
import com.example.frontend.ui.Components.StatusType
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

@Composable
fun LoginFaceScreen(
    uiState: LoginFaceUiState,
    onEvent: (LoginFaceUiEvent) -> Unit,
    onBackClick: () -> Unit,
    onVerified: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = PageBackground
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val inPreview = LocalInspectionMode.current
    val (cameraGranted, requestCamera) = rememberCameraPermission()

    var capture by remember { mutableStateOf<CaptureState>(CaptureState.Framing) }

    LaunchedEffect(uiState.isVerified) {
        if (uiState.isVerified) onVerified()
    }

    LaunchedEffect(uiState.cameraActive) {
        if (uiState.cameraActive) capture = CaptureState.Framing
    }

    Scaffold(
        modifier = modifier,
        containerColor = backgroundColor,
        topBar = { AppTopBar(title = "Face check", onBackClick = onBackClick) }
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
            SectionHeading(
                title = "Prove it is you",
                description = "Look straight at the camera and blink slowly when asked. " +
                    "A photo of you will not pass this check."
            )

            if (uiState.cameraActive && cameraGranted && !inPreview) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(0.85f)
                        .clip(MaterialTheme.shapes.medium)
                        .background(BrandNavyDeep)
                ) {
                    CameraView(
                        mode = CameraCaptureStates.FACE,
                        state = capture,
                        requireLiveness = true,
                        onPhotoTaken = { photo ->
                            capture = CaptureState.Capturing
                            scope.launch {
                                when (val graded = evaluateFaceCapture(photo)) {
                                    is CaptureState.Problem -> {
                                        capture = graded
                                        onEvent(LoginFaceUiEvent.OnCaptureProblem(graded.message))
                                    }
                                    is CaptureState.Captured -> {
                                        capture = CaptureState.Uploading
                                        onEvent(
                                            LoginFaceUiEvent.OnFaceCaptured(
                                                saveLoginFace(context, graded.photo)
                                            )
                                        )
                                    }
                                    else -> Unit
                                }
                            }
                        },
                        onRetake = {
                            capture = CaptureState.Framing
                            onEvent(LoginFaceUiEvent.OnRetry)
                        },
                        onConfirm = {},
                        onError = { message ->
                            capture = CaptureState.Problem(message)
                            onEvent(LoginFaceUiEvent.OnCaptureProblem(message))
                        }
                    )
                }
            } else {
                FaceCheckFrame(
                    locked = uiState.isLocked,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            StatusLabel(
                message = statusMessage(uiState, cameraGranted),
                type = statusType(uiState),
                modifier = Modifier.fillMaxWidth()
            )

            if (uiState.isChecking) {
                IndeterminateLinearIndicator(label = "Checking…")
            }

            when {
                uiState.isLocked -> SimpleButton(
                    label = "Back to sign in",
                    onClick = onBackClick,
                    modifier = Modifier.fillMaxWidth(),
                    variant = ButtonVariant.Tertiary
                )
                uiState.didNotMatch || uiState.captureProblem != null -> SimpleButton(
                    label = "Try again",
                    onClick = {
                        capture = CaptureState.Framing
                        onEvent(LoginFaceUiEvent.OnRetry)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    variant = ButtonVariant.Tertiary,
                    leadingIcon = Icons.Default.Refresh
                )
                !cameraGranted -> SimpleButton(
                    label = "Allow Camera Access",
                    onClick = requestCamera,
                    modifier = Modifier.fillMaxWidth(),
                    variant = ButtonVariant.Tertiary,
                    leadingIcon = Icons.Default.CenterFocusStrong
                )
            }

            DescriptionLabel(
                text = "The scan is matched against your registration photo and is never shared " +
                    "with anyone else.",
                modifier = Modifier.fillMaxWidth(),
                color = MutedText,
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun statusMessage(state: LoginFaceUiState, cameraGranted: Boolean): String = when {
    state.isVerified -> "Face matched. Signing you in…"
    state.isLocked ->
        "Account locked after $FACE_MAX_ATTEMPTS failed checks. " +
            "Try again in ${formatCountdown(state.lockSecondsRemaining)}."
    state.isChecking -> "Checking…"
    state.didNotMatch ->
        "Face didn't match, try again. ${state.attemptsLeft} tries left before we lock the account."
    state.captureProblem != null -> state.captureProblem
    !cameraGranted -> "We need the camera to check your face. Nothing is sent until the check runs."
    else -> "Keep your face in the oval and blink slowly when asked."
}

private fun statusType(state: LoginFaceUiState): StatusType = when {
    state.isVerified -> StatusType.Success
    state.isLocked -> StatusType.Warning
    state.didNotMatch || state.captureProblem != null -> StatusType.Error
    else -> StatusType.Info
}

private suspend fun saveLoginFace(context: Context, photo: Bitmap): String =
    withContext(Dispatchers.IO) {
        val file = File(context.cacheDir, "login_face.jpg")
        file.outputStream().use { out -> photo.compress(Bitmap.CompressFormat.JPEG, 90, out) }
        file.absolutePath
    }

@Composable
private fun FaceCheckFrame(locked: Boolean, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .aspectRatio(0.85f)
            .clip(MaterialTheme.shapes.medium)
            .background(BrandNavyDeep),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val width = size.width * 0.65f
            val height = width * 1.3f
            drawOval(
                color = if (locked) Color.White.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.75f),
                topLeft = Offset((size.width - width) / 2, (size.height - height) / 2.4f),
                size = Size(width, height),
                style = Stroke(width = 3.dp.toPx())
            )
        }
        Icon(
            imageVector = Icons.Default.CenterFocusStrong,
            contentDescription = null,
            tint = CardBorder,
            modifier = Modifier.height(64.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginFaceScreenLivePreview() {
    FrontendTheme {
        LoginFaceScreen(
            uiState = LoginFaceUiState(idNumber = "0203015800085"),
            onEvent = {},
            onBackClick = {},
            onVerified = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginFaceScreenCheckingPreview() {
    FrontendTheme {
        LoginFaceScreen(
            uiState = LoginFaceUiState(idNumber = "0203015800085", isChecking = true),
            onEvent = {},
            onBackClick = {},
            onVerified = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginFaceScreenMismatchPreview() {
    FrontendTheme {
        LoginFaceScreen(
            uiState = LoginFaceUiState(
                idNumber = "9001015800085",
                didNotMatch = true,
                attemptsUsed = 1
            ),
            onEvent = {},
            onBackClick = {},
            onVerified = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginFaceScreenLockedPreview() {
    FrontendTheme {
        LoginFaceScreen(
            uiState = LoginFaceUiState(
                idNumber = "9001015800085",
                didNotMatch = true,
                attemptsUsed = FACE_MAX_ATTEMPTS,
                lockSecondsRemaining = FACE_LOCKOUT_SECONDS
            ),
            onEvent = {},
            onBackClick = {},
            onVerified = {}
        )
    }
}
