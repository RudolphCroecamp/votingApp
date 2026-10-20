package com.example.frontend.ui.components.Camera

import android.graphics.Bitmap
import android.graphics.Rect
import android.util.Log
import com.example.frontend.R

private const val TAG = "CaptureQuality"

/** Width the measured region is resampled to, so scores don't depend on camera resolution. */
private const val MEASURE_WIDTH = 256

/** Below this mean luma (0-255) the picture is too dark to judge or match. */
private const val MIN_BRIGHTNESS = 45.0

/**
 * Variance-of-Laplacian floors. Sharp faces on phones score well above 50 at this
 * resolution, motion blur drops under ~10. Faces are smooth, so their floor is lower
 * than an ID card's, which is full of print and edges.
 */
private const val FACE_MIN_SHARPNESS = 8.0
private const val DOCUMENT_MIN_SHARPNESS = 15.0

/** Grey copy of [region] (or the whole bitmap), resampled to [MEASURE_WIDTH] wide. */
class LumaImage(val pixels: FloatArray, val width: Int, val height: Int) {
    val mean: Double get() = pixels.average()
}

fun Bitmap.lumaOf(region: Rect? = null): LumaImage {
    val box = (region ?: Rect(0, 0, width, height)).let {
        Rect(it.left.coerceIn(0, width - 1), it.top.coerceIn(0, height - 1), it.right.coerceIn(1, width), it.bottom.coerceIn(1, height))
    }
    val w = MEASURE_WIDTH
    val h = (box.height() * MEASURE_WIDTH / box.width().coerceAtLeast(1)).coerceIn(16, MEASURE_WIDTH * 2)
    val cropped = Bitmap.createBitmap(this, box.left, box.top, box.width().coerceAtLeast(1), box.height().coerceAtLeast(1))
    val scaled = Bitmap.createScaledBitmap(cropped, w, h, true)
    val argb = IntArray(w * h)
    scaled.getPixels(argb, 0, w, 0, 0, w, h)
    val luma = FloatArray(w * h) { i ->
        val p = argb[i]
        0.299f * (p shr 16 and 0xFF) + 0.587f * (p shr 8 and 0xFF) + 0.114f * (p and 0xFF)
    }
    return LumaImage(luma, w, h)
}

/**
 * Variance of the 4-neighbour Laplacian: the standard "is it in focus" score.
 * Edges give large responses; blur flattens them, so the variance collapses.
 */
fun laplacianVariance(image: LumaImage): Double {
    val (px, w, h) = Triple(image.pixels, image.width, image.height)
    if (w < 3 || h < 3) return 0.0
    var sum = 0.0
    var sumSq = 0.0
    var n = 0
    for (y in 1 until h - 1) {
        for (x in 1 until w - 1) {
            val i = y * w + x
            val lap = (px[i - 1] + px[i + 1] + px[i - w] + px[i + w] - 4 * px[i]).toDouble()
            sum += lap
            sumSq += lap * lap
            n++
        }
    }
    val mean = sum / n
    return sumSq / n - mean * mean
}

/** Grades a photo of an ID document: lit and in focus. */
fun evaluateCapture(photo: Bitmap): CaptureState = grade(photo, region = null, minSharpness = DOCUMENT_MIN_SHARPNESS, what = "document")

/** Grades only the face area, so a plain wall or dark background behind the voter doesn't count. */
internal fun evaluateFaceRegion(photo: Bitmap, face: Rect): CaptureState =
    grade(photo, region = face, minSharpness = FACE_MIN_SHARPNESS, what = "face")

private fun grade(photo: Bitmap, region: Rect?, minSharpness: Double, what: String): CaptureState {
    val luma = photo.lumaOf(region)
    val brightness = luma.mean
    val sharpness = laplacianVariance(luma)
    // Logged so thresholds can be checked against real phones: adb logcat -s CaptureQuality
    Log.d(TAG, "$what brightness=%.1f sharpness=%.1f (min %.1f)".format(brightness, sharpness, minSharpness))
    return when {
        brightness < MIN_BRIGHTNESS -> CaptureState.Problem(R.string.camera_problem_too_dark)
        sharpness < minSharpness -> CaptureState.Problem(R.string.camera_problem_blurry)
        else -> CaptureState.Captured(photo)
    }
}
