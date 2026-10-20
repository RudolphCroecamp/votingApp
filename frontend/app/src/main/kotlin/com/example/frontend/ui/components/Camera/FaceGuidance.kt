package com.example.frontend.ui.components.Camera

import androidx.annotation.StringRes
import com.example.frontend.R
import kotlin.math.abs

/**
 * What the voter should do next, shown under the face oval while the camera is live.
 * Order matters: framing problems are reported before liveness.
 */
enum class FaceGuidance(@param:StringRes val messageRes: Int) {
    NO_FACE(R.string.face_guidance_no_face),
    MULTIPLE_FACES(R.string.face_guidance_multiple_faces),
    MOVE_CLOSER(R.string.face_guidance_move_closer),
    MOVE_BACK(R.string.face_guidance_move_back),
    // Directions are as seen on screen; the front preview is mirrored, so they feel natural.
    MOVE_LEFT(R.string.face_guidance_move_left),
    MOVE_RIGHT(R.string.face_guidance_move_right),
    MOVE_UP(R.string.face_guidance_move_up),
    MOVE_DOWN(R.string.face_guidance_move_down),
    LOOK_STRAIGHT(R.string.face_guidance_look_straight),
    HOLD_LEVEL(R.string.face_guidance_hold_level),
    BLINK(R.string.face_guidance_blink),
    READY(R.string.face_guidance_ready)
}

/**
 * The parts of an ML Kit `Face` we care about, as plain numbers.
 * Kept free of Android types so [LivenessChecker] runs in plain JVM unit tests.
 * Coordinates are in the camera preview's own pixels.
 */
data class FaceSnapshot(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
    val leftEyeOpen: Float?,   // 0 = closed, 1 = open, null = ML Kit couldn't tell
    val rightEyeOpen: Float?,
    val yaw: Float,            // head turned left/right, degrees
    val roll: Float            // head tilted sideways, degrees
) {
    val width get() = right - left
    val centreX get() = (left + right) / 2
    val centreY get() = (top + bottom) / 2
}

/** Where the face oval sits on screen. Shared by the drawing and the framing check so they agree. */
data class OvalBounds(val left: Float, val top: Float, val width: Float, val height: Float) {
    val centreX get() = left + width / 2
    val centreY get() = top + height / 2
}

fun faceOvalBounds(viewWidth: Float, viewHeight: Float): OvalBounds {
    val w = viewWidth * 0.65f
    val h = w * 1.3f
    return OvalBounds(left = (viewWidth - w) / 2, top = (viewHeight - h) / 2.4f, width = w, height = h)
}

/**
 * Turns a stream of detected faces into one [FaceGuidance] per frame.
 *
 * Liveness = a real blink: both eyes seen closed, then both seen open again.
 * A printed photo can't do that, which is the point.
 */
class LivenessChecker(private val requireBlink: Boolean) {

    var passed = false
        private set
    private var eyesClosedSeen = false

    fun reset() {
        passed = false
        eyesClosedSeen = false
    }

    fun evaluate(faces: List<FaceSnapshot>, viewWidth: Float, viewHeight: Float): FaceGuidance {
        if (faces.isEmpty()) return FaceGuidance.NO_FACE
        if (faces.size > 1) return FaceGuidance.MULTIPLE_FACES
        if (viewWidth <= 0f || viewHeight <= 0f) return FaceGuidance.NO_FACE

        val face = faces.single()
        val oval = faceOvalBounds(viewWidth, viewHeight)

        // Framing: face roughly fills the oval and sits inside it.
        val fill = face.width / oval.width
        if (fill < MIN_FILL) return FaceGuidance.MOVE_CLOSER
        if (fill > MAX_FILL) return FaceGuidance.MOVE_BACK
        val dx = face.centreX - oval.centreX
        val dy = face.centreY - oval.centreY
        // Fix the larger offset first, so the voter gets one instruction at a time.
        if (abs(dx) / oval.width > CENTRE_TOLERANCE || abs(dy) / oval.height > CENTRE_TOLERANCE) {
            return if (abs(dx) / oval.width >= abs(dy) / oval.height) {
                if (dx > 0) FaceGuidance.MOVE_LEFT else FaceGuidance.MOVE_RIGHT
            } else {
                if (dy > 0) FaceGuidance.MOVE_UP else FaceGuidance.MOVE_DOWN
            }
        }
        if (abs(face.yaw) > MAX_ANGLE) return FaceGuidance.LOOK_STRAIGHT
        if (abs(face.roll) > MAX_ANGLE) return FaceGuidance.HOLD_LEVEL

        if (!requireBlink || passed) return FaceGuidance.READY

        val left = face.leftEyeOpen ?: return FaceGuidance.BLINK
        val right = face.rightEyeOpen ?: return FaceGuidance.BLINK
        when {
            left < EYE_CLOSED && right < EYE_CLOSED -> eyesClosedSeen = true
            eyesClosedSeen && left > EYE_OPEN && right > EYE_OPEN -> passed = true
        }
        return if (passed) FaceGuidance.READY else FaceGuidance.BLINK
    }

    private companion object {
        const val MIN_FILL = 0.5f          // face narrower than half the oval = too far
        const val MAX_FILL = 1.15f         // face wider than the oval = too close
        const val CENTRE_TOLERANCE = 0.25f
        const val MAX_ANGLE = 15f
        const val EYE_CLOSED = 0.2f
        const val EYE_OPEN = 0.8f
    }
}
