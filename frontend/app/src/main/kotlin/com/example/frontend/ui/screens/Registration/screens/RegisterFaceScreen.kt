package com.example.frontend.ui.screens.Registration.screens

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.frontend.R
import com.example.frontend.ui.components.AppTopBar
import com.example.frontend.ui.components.ButtonVariant
import com.example.frontend.ui.components.Camera.CameraCaptureStates
import com.example.frontend.ui.components.Camera.CameraView
import com.example.frontend.ui.components.Camera.CaptureState
import com.example.frontend.ui.components.Camera.evaluateCapture
import com.example.frontend.ui.components.Camera.rememberCameraPermission
import com.example.frontend.ui.components.SimpleButton
import com.example.frontend.ui.components.StatusLabel
import com.example.frontend.ui.components.StatusType
import com.example.frontend.ui.components.SuccessCheckmarkBadge
import com.example.frontend.ui.screens.Registration.RegistrationUiEvent
import com.example.frontend.ui.screens.Registration.RegistrationUiState
import com.example.frontend.ui.theme.BrandNavyDeep
import com.example.frontend.ui.theme.CardBorder
import com.example.frontend.ui.theme.FrontendTheme
import com.example.frontend.ui.theme.PageBackground
import com.example.frontend.ui.theme.Spacing
import com.example.frontend.ui.theme.SuccessBorder
import com.example.frontend.ui.theme.motionEnabled
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/** Only used by the previews, which cannot open a camera. */
private const val PREVIEW_FACE_PATH = "path/to/captured_face.jpg"

@Composable
fun RegisterFaceScreen(
    uiState: RegistrationUiState,
    onEvent: (RegistrationUiEvent) -> Unit,
    onBackClick: () -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = PageBackground
) {
    val captured = uiState.capturedFaceImagePath != null
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val inPreview = LocalInspectionMode.current
    val (cameraGranted, requestCamera) = rememberCameraPermission()

    // Capture state lives here: the screen owns the camera, the view model owns the result.
    var capture by remember { mutableStateOf<CaptureState>(CaptureState.Framing) }

    LaunchedEffect(uiState.isRegistered) {
        if (uiState.isRegistered) onContinue()
    }

    Scaffold(
        modifier = modifier,
        containerColor = backgroundColor,
        topBar = { AppTopBar(title = stringResource(R.string.register_face_title), onBackClick = onBackClick) }
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
                step = 5,
                heading = stringResource(R.string.register_face_heading),
                description = stringResource(R.string.register_face_description)
            )

            Spacer(modifier = Modifier.height(Spacing.small))

            if (captured || !cameraGranted || inPreview) {
                // Nothing to show live: either we already have a photo, or we cannot open the camera.
                FaceCaptureFrame(captured = captured, modifier = Modifier.fillMaxWidth())
            } else {
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
                        onPhotoTaken = { photo -> capture = evaluateCapture(photo) },
                        onRetake = { capture = CaptureState.Framing },
                        onConfirm = {
                            val photo = (capture as? CaptureState.Captured)?.photo
                            if (photo != null) {
                                capture = CaptureState.Uploading
                                scope.launch {
                                    onEvent(
                                        RegistrationUiEvent.OnFaceCaptured(saveFaceImage(context, photo))
                                    )
                                }
                            }
                        },
                        onError = { messageRes -> capture = CaptureState.Problem(messageRes) }
                    )
                }
            }

            StatusLabel(
                message = uiState.errorRes?.let { stringResource(it) }
                    ?: statusMessage(captured, cameraGranted, capture),
                type = when {
                    uiState.errorRes != null -> StatusType.Error
                    captured -> StatusType.Success
                    else -> StatusType.Info
                },
                modifier = Modifier.fillMaxWidth()
            )

            if (captured) {
                SimpleButton(
                    label = stringResource(
                        if (uiState.isLoading) R.string.register_verifying else R.string.register_verify_continue
                    ),
                    onClick = { onEvent(RegistrationUiEvent.OnVerifyFaceSubmitted) },
                    modifier = Modifier.fillMaxWidth(),
                    variant = ButtonVariant.Tertiary,
                    enabled = uiState.canSubmitFace
                )
                SimpleButton(
                    label = stringResource(R.string.register_face_retake),
                    onClick = {
                        capture = CaptureState.Framing
                        onEvent(RegistrationUiEvent.OnFaceRetake)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    variant = ButtonVariant.Secondary,
                    leadingIcon = Icons.Default.Refresh,
                    enabled = !uiState.isLoading
                )
            } else if (!cameraGranted) {
                SimpleButton(
                    label = stringResource(R.string.camera_allow_access),
                    onClick = requestCamera,
                    modifier = Modifier.fillMaxWidth(),
                    variant = ButtonVariant.Tertiary,
                    leadingIcon = Icons.Default.CenterFocusStrong
                )
            }
            // Permission granted and nothing captured yet: the shutter lives inside CameraView.
        }
    }
}

@Composable
private fun statusMessage(
    captured: Boolean,
    cameraGranted: Boolean,
    capture: CaptureState
): String = when {
    captured -> stringResource(R.string.register_face_captured)
    !cameraGranted -> stringResource(R.string.register_face_camera_needed)
    capture is CaptureState.Problem -> stringResource(capture.messageRes)
    else -> stringResource(R.string.register_face_hint)
}

/** Registration sends a file path, so the capture is written to the app cache first. */
private suspend fun saveFaceImage(context: Context, photo: Bitmap): String =
    withContext(Dispatchers.IO) {
        val file = File(context.cacheDir, "face_capture.jpg")
        file.outputStream().use { out -> photo.compress(Bitmap.CompressFormat.JPEG, 90, out) }
        file.absolutePath
    }

@Composable
private fun FaceCaptureFrame(captured: Boolean, modifier: Modifier = Modifier) {
    // The sweep only runs while we are actually waiting for a capture, and not
    // at all when the device has animations turned off.
    val animate = motionEnabled && !captured
    val sweep = if (animate) {
        val transition = rememberInfiniteTransition(label = "faceScan")
        val value by transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 2200, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "sweep"
        )
        value
    } else {
        0.5f
    }
    val outline = if (captured) SuccessBorder else Color.White.copy(alpha = 0.75f)

    Box(
        modifier = modifier
            .aspectRatio(0.85f)
            .clip(MaterialTheme.shapes.medium)
            .background(BrandNavyDeep),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val inset = size.minDimension * 0.12f
            val frame = Size(size.width - inset * 2, size.height - inset * 2)
            val corner = frame.minDimension * 0.45f
            drawRoundRect(
                color = outline,
                topLeft = Offset(inset, inset),
                size = frame,
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(corner),
                style = Stroke(width = 3.dp.toPx())
            )
            if (!captured) {
                val y = inset + frame.height * sweep
                drawLine(
                    color = outline.copy(alpha = 0.7f),
                    start = Offset(inset * 1.6f, y),
                    end = Offset(size.width - inset * 1.6f, y),
                    strokeWidth = 2.dp.toPx()
                )
            }
        }
        if (captured) {
            SuccessCheckmarkBadge(size = 72.dp)
        } else {
            Icon(
                imageVector = Icons.Default.CenterFocusStrong,
                contentDescription = null,
                tint = CardBorder,
                modifier = Modifier.height(64.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RegisterFaceScreenEmptyPreview() {
    FrontendTheme {
        RegisterFaceScreen(
            uiState = RegistrationUiState(),
            onEvent = {},
            onBackClick = {},
            onContinue = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RegisterFaceScreenCapturedPreview() {
    FrontendTheme {
        RegisterFaceScreen(
            uiState = RegistrationUiState(capturedFaceImagePath = PREVIEW_FACE_PATH),
            onEvent = {},
            onBackClick = {},
            onContinue = {}
        )
    }
}
