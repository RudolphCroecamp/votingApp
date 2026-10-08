package com.example.frontend.ui.screens.Registration.screens

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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.frontend.ui.Components.AppTopBar
import com.example.frontend.ui.Components.ButtonVariant
import com.example.frontend.ui.Components.StatusLabel
import com.example.frontend.ui.Components.SimpleButton
import com.example.frontend.ui.Components.StatusType
import com.example.frontend.ui.Components.SuccessCheckmarkBadge
import com.example.frontend.ui.screens.Registration.RegistrationUiEvent
import com.example.frontend.ui.screens.Registration.RegistrationUiState
import com.example.frontend.ui.theme.CardBorder
import com.example.frontend.ui.theme.BrandNavyDeep
import com.example.frontend.ui.theme.motionEnabled
import com.example.frontend.ui.theme.FrontendTheme
import com.example.frontend.ui.theme.PageBackground
import com.example.frontend.ui.theme.Spacing
import com.example.frontend.ui.theme.SuccessBorder
/** Simulated capture path until CameraX is wired up. */
private const val CAPTURED_FACE_PATH = "path/to/captured_face.jpg"

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

    Scaffold(
        modifier = modifier,
        containerColor = backgroundColor,
        topBar = { AppTopBar(title = "Face Registration", onBackClick = onBackClick) }
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
                heading = "Scan Your Face",
                description = "Hold your phone at eye level and keep your face inside the frame."
            )

            Spacer(modifier = Modifier.height(Spacing.small))

            FaceCaptureFrame(captured = captured, modifier = Modifier.fillMaxWidth())

            StatusLabel(
                message = if (captured) {
                    "Face captured. Retake it if your face was not clearly inside the frame."
                } else {
                    "Find a well-lit spot, remove hats and sunglasses, and look straight ahead."
                },
                type = if (captured) StatusType.Success else StatusType.Info,
                modifier = Modifier.fillMaxWidth()
            )

            if (captured) {
                SimpleButton(
                    label = if (uiState.isLoading) "Verifying…" else "Verify & Continue",
                    onClick = {
                        onEvent(RegistrationUiEvent.OnVerifyFaceSubmitted)
                        onContinue()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    variant = ButtonVariant.Tertiary,
                    enabled = uiState.canSubmitFace
                )
                SimpleButton(
                    label = "Retake Scan",
                    onClick = { onEvent(RegistrationUiEvent.OnFaceRetake) },
                    modifier = Modifier.fillMaxWidth(),
                    variant = ButtonVariant.Secondary,
                    leadingIcon = Icons.Default.Refresh,
                    enabled = !uiState.isLoading
                )
            } else {
                SimpleButton(
                    label = "Capture Face",
                    onClick = { onEvent(RegistrationUiEvent.OnFaceCaptured(CAPTURED_FACE_PATH)) },
                    modifier = Modifier.fillMaxWidth(),
                    variant = ButtonVariant.Tertiary,
                    leadingIcon = Icons.Default.CenterFocusStrong
                )
            }
        }
    }
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
            uiState = RegistrationUiState(capturedFaceImagePath = CAPTURED_FACE_PATH),
            onEvent = {},
            onBackClick = {},
            onContinue = {}
        )
    }
}
