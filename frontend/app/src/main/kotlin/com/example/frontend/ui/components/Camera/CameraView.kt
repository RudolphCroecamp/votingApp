package com.example.frontend.ui.components.Camera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.mlkit.vision.MlKitAnalyzer
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.frontend.R
import kotlinx.coroutines.delay

@Composable
fun CameraView(
    mode: CameraCaptureStates,
    state: CaptureState,
    onPhotoTaken: (Bitmap) -> Unit,
    onRetake: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    onError: (messageRes: Int) -> Unit = {},
    // FACE mode only: require a real blink, then capture automatically (login + vote checks).
    requireLiveness: Boolean = false,
    // FACE mode only: take the photo by itself once the face is framed and steady.
    // Tapping a shutter shakes the phone, which is the main cause of blurry face photos.
    autoCapture: Boolean = requireLiveness
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val controller = remember(mode) {
        LifecycleCameraController(context).apply {
            cameraSelector = if (mode == CameraCaptureStates.FACE) {
                CameraSelector.DEFAULT_FRONT_CAMERA
            } else {
                CameraSelector.DEFAULT_BACK_CAMERA
            }
            setEnabledUseCases(
                if (mode == CameraCaptureStates.FACE) {
                    CameraController.IMAGE_CAPTURE or CameraController.IMAGE_ANALYSIS
                } else {
                    CameraController.IMAGE_CAPTURE
                }
            )
        }
    }

    LaunchedEffect(controller, lifecycleOwner) {
        controller.bindToLifecycle(lifecycleOwner)
    }

    // --- Live face detection (FACE mode only) ---
    var viewSize by remember { mutableStateOf(IntSize.Zero) }
    var guidance by remember(mode) { mutableStateOf(FaceGuidance.NO_FACE) }
    val checker = remember(mode, requireLiveness) { LivenessChecker(requireLiveness) }
    var autoCaptureFired by remember { mutableStateOf(false) }

    DisposableEffect(controller, mode) {
        val detector = if (mode == CameraCaptureStates.FACE) createFaceDetector() else null
        if (detector != null) {
            val mainExecutor = ContextCompat.getMainExecutor(context)
            controller.setImageAnalysisAnalyzer(
                mainExecutor,
                MlKitAnalyzer(
                    listOf(detector),
                    ImageAnalysis.COORDINATE_SYSTEM_VIEW_REFERENCED, // face boxes in on-screen pixels
                    mainExecutor
                ) { result ->
                    val faces = result.getValue(detector) ?: return@MlKitAnalyzer // null = frame failed
                    guidance = checker.evaluate(
                        faces.map { it.toSnapshot() },
                        viewSize.width.toFloat(),
                        viewSize.height.toFloat()
                    )
                }
            )
        }
        onDispose {
            controller.clearImageAnalysisAnalyzer()
            detector?.close()
        }
    }

    // Back to framing (first open or Retake): start the liveness check again.
    LaunchedEffect(state) {
        if (state == CaptureState.Framing) {
            checker.reset()
            autoCaptureFired = false
        }
    }

    // Framed (and blinked, if required): wait for the face to stay put, then capture.
    // Any guidance change restarts this effect, which cancels the wait.
    LaunchedEffect(guidance, state) {
        if (autoCapture && (!requireLiveness || checker.passed) && guidance == FaceGuidance.READY &&
            state == CaptureState.Framing && !autoCaptureFired
        ) {
            delay(STEADY_BEFORE_CAPTURE_MS)
            autoCaptureFired = true
            takePhoto(context, controller, onPhotoTaken, onError)
        }
    }

    Box(modifier.fillMaxSize().onSizeChanged { viewSize = it }) {
        if (state is CaptureState.Captured) {
            // Frozen photo instead of live preview
            Image(
                bitmap = state.photo.asImageBitmap(),
                contentDescription = stringResource(R.string.camera_captured_photo),
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            AndroidView(                                   // wrap classic View
                factory = { ctx ->
                    PreviewView(ctx).apply {
                        this.controller = controller
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
            FramingGuide(mode, outline = if (mode == CameraCaptureStates.FACE) guidance.tone.colour else Color.White)
            if (mode == CameraCaptureStates.FACE && state == CaptureState.Framing) {
                GuidancePill(
                    guidance = guidance,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                )
            }
        }

        // Bottom area: message + buttons per state
        Column(
            Modifier.align(Alignment.BottomCenter).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when (state) {
                CaptureState.Framing -> if (mode == CameraCaptureStates.FACE) {
                    // The instruction itself is in the pill at the top of the camera.
                    when {
                        autoCapture && guidance == FaceGuidance.READY ->
                            CircularProgressIndicator(color = GuidanceTone.Ready.colour) // about to capture
                        !autoCapture ->
                            ShutterButton(enabled = guidance == FaceGuidance.READY) {
                                takePhoto(context, controller, onPhotoTaken, onError)
                            }
                    }
                } else {
                    Text(stringResource(R.string.camera_fit_id), color = Color.White)
                    ShutterButton { takePhoto(context, controller, onPhotoTaken, onError) }
                }
                CaptureState.Capturing -> CircularProgressIndicator(color = Color.White)
                is CaptureState.Problem -> {
                    Text(stringResource(state.messageRes), color = Color.White)
                    Button(onClick = onRetake) { Text(stringResource(R.string.action_try_again)) }
                }
                is CaptureState.Captured -> Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = onRetake) { Text(stringResource(R.string.camera_retake)) }
                    Button(onClick = onConfirm) { Text(stringResource(R.string.camera_use_photo)) }
                }
                CaptureState.Uploading -> {
                    CircularProgressIndicator(color = Color.White)
                    Text(stringResource(R.string.camera_uploading), color = Color.White)
                }
            }
        }
    }
}

/** Captures a single frame and hands back an upright [Bitmap] on the main thread. */
private fun takePhoto(
    context: Context,
    controller: LifecycleCameraController,
    onPhotoTaken: (Bitmap) -> Unit,
    onFailure: (messageRes: Int) -> Unit
) {
    controller.takePicture(
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                try {
                    onPhotoTaken(image.toBitmap().rotate(image.imageInfo.rotationDegrees))
                } finally {
                    image.close()
                }
            }

            override fun onError(exception: ImageCaptureException) {
                // CameraX's exception text is technical and English-only; show our own message.
                onFailure(R.string.camera_problem_capture_failed)
            }
        }
    )
}

/** CameraX reports the sensor orientation; rotate so the photo matches what the user saw. */
private fun Bitmap.rotate(degrees: Int): Bitmap =
    if (degrees == 0) {
        this
    } else {
        Bitmap.createBitmap(this, 0, 0, width, height, Matrix().apply { postRotate(degrees.toFloat()) }, true)
    }

private const val STEADY_BEFORE_CAPTURE_MS = 600L

/** Colour of the oval and pill: green when ready, gold while blinking, amber when adjusting. */
enum class GuidanceTone(val colour: Color) {
    Ready(Color(0xFF4ADE80)),
    Action(Color(0xFFFFB612)),
    Adjust(Color.White)
}

val FaceGuidance.tone: GuidanceTone
    get() = when (this) {
        FaceGuidance.READY -> GuidanceTone.Ready
        FaceGuidance.BLINK -> GuidanceTone.Action
        else -> GuidanceTone.Adjust
    }

private val FaceGuidance.icon: ImageVector
    get() = when (this) {
        FaceGuidance.NO_FACE -> Icons.Default.Face
        FaceGuidance.MULTIPLE_FACES -> Icons.Default.Groups
        FaceGuidance.MOVE_CLOSER -> Icons.Default.ZoomIn
        FaceGuidance.MOVE_BACK -> Icons.Default.ZoomOut
        FaceGuidance.MOVE_LEFT -> Icons.AutoMirrored.Filled.ArrowBack
        FaceGuidance.MOVE_RIGHT -> Icons.AutoMirrored.Filled.ArrowForward
        FaceGuidance.MOVE_UP -> Icons.Default.ArrowUpward
        FaceGuidance.MOVE_DOWN -> Icons.Default.ArrowDownward
        FaceGuidance.LOOK_STRAIGHT -> Icons.Default.CenterFocusStrong
        FaceGuidance.HOLD_LEVEL -> Icons.Default.ScreenRotation
        FaceGuidance.BLINK -> Icons.Default.Visibility
        FaceGuidance.READY -> Icons.Default.CheckCircle
    }

/** One clear instruction at a time, read out by TalkBack as it changes. */
@Composable
private fun GuidancePill(guidance: FaceGuidance, modifier: Modifier = Modifier) {
    val tone = guidance.tone
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.65f))
            .border(1.5.dp, tone.colour, CircleShape)
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .semantics { liveRegion = LiveRegionMode.Polite },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(guidance.icon, contentDescription = null, tint = tone.colour, modifier = Modifier.size(22.dp))
        Text(
            text = stringResource(guidance.messageRes),
            color = Color.White,
            style = MaterialTheme.typography.titleSmall
        )
    }
}

@Composable
private fun FramingGuide(mode: CameraCaptureStates, outline: Color = Color.White) {
    Canvas(
        Modifier.fillMaxSize()
            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen } // needed for Clear
    ) {
        drawRect(Color.Black.copy(alpha = 0.55f))                 // dim everything
        if (mode == CameraCaptureStates.FACE) {
            // Same bounds LivenessChecker uses, so "centre your face" matches what's drawn.
            val oval = faceOvalBounds(size.width, size.height)
            val topLeft = Offset(oval.left, oval.top)
            val ovalSize = Size(oval.width, oval.height)
            drawOval(Color.Transparent, topLeft, ovalSize, blendMode = BlendMode.Clear)  // punch hole
            drawOval(outline, topLeft, ovalSize, style = Stroke(4.dp.toPx()))
        } else {
            val w = size.width * 0.85f
            val h = w / 1.586f                                     // 1.586 = ID card ratio
            val topLeft = Offset((size.width - w) / 2, (size.height - h) / 2.4f)
            val r = CornerRadius(16.dp.toPx())
            drawRoundRect(Color.Transparent, topLeft, Size(w, h), r, blendMode = BlendMode.Clear)
            drawRoundRect(Color.White, topLeft, Size(w, h), r, style = Stroke(3.dp.toPx()))
        }
    }
}

@Composable
private fun ShutterButton(enabled: Boolean = true, onClick: () -> Unit) {
    val clickLabel = stringResource(R.string.camera_take_photo)
    Box(
        Modifier.size(72.dp).clip(CircleShape)
            .background(Color.White.copy(alpha = if (enabled) 1f else 0.35f)) // dimmed until face is ready
            .border(4.dp, Color.White.copy(alpha = 0.5f), CircleShape)
            .clickable(enabled = enabled, onClickLabel = clickLabel, onClick = onClick)
    )
}
