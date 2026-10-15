package com.example.frontend.ui.Components.Camera

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
import androidx.compose.material3.Button
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
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.frontend.R
import kotlin.math.abs

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
    requireLiveness: Boolean = false
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

    // Liveness passed: take the photo without waiting for a tap.
    LaunchedEffect(guidance, state) {
        if (requireLiveness && checker.passed && guidance == FaceGuidance.READY &&
            state == CaptureState.Framing && !autoCaptureFired
        ) {
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
            FramingGuide(mode)                             // oval or card outline
        }

        // Bottom area: message + buttons per state
        Column(
            Modifier.align(Alignment.BottomCenter).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when (state) {
                CaptureState.Framing -> if (mode == CameraCaptureStates.FACE) {
                    Text(
                        stringResource(guidance.messageRes),
                        color = Color.White,
                        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite } // TalkBack reads hints
                    )
                    when {
                        requireLiveness && guidance == FaceGuidance.READY ->
                            CircularProgressIndicator(color = Color.White) // auto-capturing
                        !requireLiveness ->
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

@Composable
private fun FramingGuide(mode: CameraCaptureStates) {
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
            drawOval(Color.White, topLeft, ovalSize, style = Stroke(3.dp.toPx()))
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
fun Bitmap.brightness(): Double {
    val small = Bitmap.createScaledBitmap(this, 64, 64, true)
    var sum = 0.0
    for (x in 0 until 64) for (y in 0 until 64) {
        val p = small.getPixel(x, y)
        sum += 0.299 * p.red + 0.587 * p.green + 0.114 * p.blue
    }
    return sum / (64 * 64)
}

/** Sharpness = how much neighbouring pixels differ. Low value = blurry. Tune threshold on a real phone. */
fun Bitmap.sharpness(): Double {
    val s = Bitmap.createScaledBitmap(this, 200, 200, true)
    var total = 0.0
    for (x in 1 until 199) for (y in 0 until 200) {
        total += abs(s.getPixel(x, y).red - s.getPixel(x - 1, y).red)
    }
    return total / (198 * 200)
}
/** Grades a fresh capture. The caller owns the state, so this just reports the verdict. */
fun evaluateCapture(photo: Bitmap): CaptureState = when {
    photo.brightness() < 60 -> CaptureState.Problem(R.string.camera_problem_too_dark)
    photo.sharpness() < 6 -> CaptureState.Problem(R.string.camera_problem_blurry)
    else -> CaptureState.Captured(photo)
}
