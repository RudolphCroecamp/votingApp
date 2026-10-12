package com.example.frontend.ui.Components.Camera

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LivenessCheckerTest {

    // 1080 x 1920 preview → oval is 702 wide, centred at (540, ~806)
    private val w = 1080f
    private val h = 1920f
    private val oval = faceOvalBounds(w, h)

    /** A face that fills the oval nicely, looking straight ahead. */
    private fun face(
        widthFraction: Float = 0.8f,
        dx: Float = 0f,
        eyes: Float? = 0.95f,
        yaw: Float = 0f
    ): FaceSnapshot {
        val fw = oval.width * widthFraction
        val cx = oval.centreX + dx
        val cy = oval.centreY
        return FaceSnapshot(cx - fw / 2, cy - fw / 2, cx + fw / 2, cy + fw / 2, eyes, eyes, yaw, 0f)
    }

    @Test fun noFace() =
        assertEquals(FaceGuidance.NO_FACE, LivenessChecker(true).evaluate(emptyList(), w, h))

    @Test fun twoFaces() =
        assertEquals(FaceGuidance.MULTIPLE_FACES, LivenessChecker(true).evaluate(listOf(face(), face()), w, h))

    @Test fun tooFar() =
        assertEquals(FaceGuidance.MOVE_CLOSER, LivenessChecker(false).evaluate(listOf(face(widthFraction = 0.3f)), w, h))

    @Test fun tooClose() =
        assertEquals(FaceGuidance.MOVE_BACK, LivenessChecker(false).evaluate(listOf(face(widthFraction = 1.4f)), w, h))

    @Test fun offCentre() =
        assertEquals(FaceGuidance.CENTRE, LivenessChecker(false).evaluate(listOf(face(dx = 300f)), w, h))

    @Test fun headTurned() =
        assertEquals(FaceGuidance.LOOK_STRAIGHT, LivenessChecker(false).evaluate(listOf(face(yaw = 30f)), w, h))

    @Test fun readyWithoutLiveness() =
        assertEquals(FaceGuidance.READY, LivenessChecker(false).evaluate(listOf(face()), w, h))

    @Test fun blinkPassesLiveness() {
        val checker = LivenessChecker(true)
        assertEquals(FaceGuidance.BLINK, checker.evaluate(listOf(face(eyes = 0.95f)), w, h)) // open
        assertEquals(FaceGuidance.BLINK, checker.evaluate(listOf(face(eyes = 0.05f)), w, h)) // closed
        assertEquals(FaceGuidance.READY, checker.evaluate(listOf(face(eyes = 0.95f)), w, h)) // open again
        assertTrue(checker.passed)
    }

    @Test fun eyesOpenOnlyIsNotLiveness() {
        val checker = LivenessChecker(true)
        repeat(10) { checker.evaluate(listOf(face(eyes = 0.95f)), w, h) }
        assertFalse(checker.passed) // a photo never blinks
    }

    @Test fun resetClearsLiveness() {
        val checker = LivenessChecker(true)
        checker.evaluate(listOf(face(eyes = 0.05f)), w, h)
        checker.evaluate(listOf(face(eyes = 0.95f)), w, h)
        checker.reset()
        assertFalse(checker.passed)
    }
}
