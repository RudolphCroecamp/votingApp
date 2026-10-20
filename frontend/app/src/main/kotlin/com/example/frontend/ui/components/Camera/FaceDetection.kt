package com.example.frontend.ui.components.Camera

import android.graphics.Bitmap
import com.example.frontend.R
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * ML Kit face detector.
 * fast = live preview (every frame); accurate = one-off check on the captured photo.
 * Detection only: it finds faces, it does NOT check whose face it is. Matching the voter is the backend's job.
 */
fun createFaceDetector(accurate: Boolean = false, landmarks: Boolean = false): FaceDetector = FaceDetection.getClient(
    FaceDetectorOptions.Builder()
        .setPerformanceMode(
            if (accurate) FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE
            else FaceDetectorOptions.PERFORMANCE_MODE_FAST
        )
        // Eye positions, so the embedder can line the face up the same way every time.
        .setLandmarkMode(
            if (landmarks) FaceDetectorOptions.LANDMARK_MODE_ALL else FaceDetectorOptions.LANDMARK_MODE_NONE
        )
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL) // eye-open probabilities
        .setMinFaceSize(0.15f)
        .build()
)

fun Face.toSnapshot() = FaceSnapshot(
    left = boundingBox.left.toFloat(),
    top = boundingBox.top.toFloat(),
    right = boundingBox.right.toFloat(),
    bottom = boundingBox.bottom.toFloat(),
    leftEyeOpen = leftEyeOpenProbability,
    rightEyeOpen = rightEyeOpenProbability,
    yaw = headEulerAngleY,
    roll = headEulerAngleZ
)

/** Runs ML Kit on a still photo. Suspends until the result is back. */
suspend fun FaceDetector.detect(photo: Bitmap): List<Face> = suspendCancellableCoroutine { cont ->
    process(InputImage.fromBitmap(photo, 0))
        .addOnSuccessListener { cont.resume(it) }
        .addOnFailureListener { cont.resumeWithException(it) }
}

/**
 * Grades a captured face photo: exactly one face, then light and sharpness measured on
 * that face only. Use this for FACE mode; [evaluateCapture] is for the ID photo.
 */
suspend fun evaluateFaceCapture(photo: Bitmap): CaptureState {
    val faces = try {
        createFaceDetector(accurate = true).use { it.detect(photo) }
    } catch (e: Exception) {
        return CaptureState.Problem(R.string.camera_problem_check_failed)
    }
    return when {
        faces.isEmpty() -> CaptureState.Problem(R.string.camera_problem_no_face)
        faces.size > 1 -> CaptureState.Problem(R.string.camera_problem_multiple_faces)
        else -> evaluateFaceRegion(photo, faces.single().boundingBox)
    }
}
