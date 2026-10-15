package com.example.frontend.ui.Components.Camera

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import com.example.frontend.ui.theme.BrandNavyDeep
import com.example.frontend.ui.theme.CardBorder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Live face scan with liveness, shared by every "prove it is you" check.
 *
 * Shows the camera while [active] and the camera permission is granted; otherwise a
 * static oval frame. A good capture is saved to the cache as [fileName] and its path
 * handed to [onFaceCaptured]; a bad one (dark, blurry, no face) goes to [onCaptureProblem].
 */
@Composable
fun FaceCheckCamera(
    active: Boolean,
    cameraGranted: Boolean,
    fileName: String,
    onFaceCaptured: (imagePath: String) -> Unit,
    onCaptureProblem: (message: String) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    dimmed: Boolean = false
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val inPreview = LocalInspectionMode.current
    var capture by remember { mutableStateOf<CaptureState>(CaptureState.Framing) }

    LaunchedEffect(active) {
        if (active) capture = CaptureState.Framing
    }

    if (active && cameraGranted && !inPreview) {
        Box(
            modifier = modifier
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
                                onCaptureProblem(graded.message)
                            }
                            is CaptureState.Captured -> {
                                capture = CaptureState.Uploading
                                onFaceCaptured(saveFace(context, graded.photo, fileName))
                            }
                            else -> Unit
                        }
                    }
                },
                onRetake = {
                    capture = CaptureState.Framing
                    onRetry()
                },
                onConfirm = {},
                onError = { message ->
                    capture = CaptureState.Problem(message)
                    onCaptureProblem(message)
                }
            )
        }
    } else {
        FaceOvalFrame(dimmed = dimmed, modifier = modifier)
    }
}

@Composable
fun FaceOvalFrame(dimmed: Boolean, modifier: Modifier = Modifier) {
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
                color = Color.White.copy(alpha = if (dimmed) 0.3f else 0.75f),
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

private suspend fun saveFace(context: Context, photo: Bitmap, fileName: String): String =
    withContext(Dispatchers.IO) {
        val file = File(context.cacheDir, fileName)
        file.outputStream().use { out -> photo.compress(Bitmap.CompressFormat.JPEG, 90, out) }
        file.absolutePath
    }
