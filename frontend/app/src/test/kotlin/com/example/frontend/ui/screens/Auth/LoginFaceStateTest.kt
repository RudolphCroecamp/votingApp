package com.example.frontend.ui.screens.Auth

import com.example.frontend.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LoginFaceStateTest {

    private fun state() = LoginFaceUiState(idNumber = "0203015800085")

    private fun LoginFaceUiState.tick(times: Int): LoginFaceUiState {
        var state = this
        repeat(times) { state = state.ticked() }
        return state
    }

    @Test
    fun `camera is live only while nothing is pending`() {
        assertTrue(state().cameraActive)
        assertFalse(state().checking().cameraActive)
        assertFalse(state().checking().rejected().cameraActive)
        assertFalse(state().checking().matched().cameraActive)
    }

    @Test
    fun `a failed match counts the attempt and leaves the account open`() {
        val failed = state().checking().rejected()

        assertTrue(failed.didNotMatch)
        assertFalse(failed.isChecking)
        assertFalse(failed.isLocked)
        assertEquals(FACE_MAX_ATTEMPTS - 1, failed.attemptsLeft)
    }

    @Test
    fun `retry clears the failure and brings the camera back`() {
        val retried = state().checking().rejected().retried()

        assertFalse(retried.didNotMatch)
        assertTrue(retried.cameraActive)
    }

    @Test
    fun `three failed matches lock the account for fifteen minutes`() {
        var current = state()
        repeat(FACE_MAX_ATTEMPTS) { current = current.checking().rejected() }

        assertTrue(current.isLocked)
        assertEquals(FACE_LOCKOUT_SECONDS, current.lockSecondsRemaining)
        assertEquals(0, current.attemptsLeft)
        assertFalse(current.cameraActive)
    }

    @Test
    fun `retry does nothing while the account is locked`() {
        var current = state()
        repeat(FACE_MAX_ATTEMPTS) { current = current.checking().rejected() }

        assertEquals(current, current.retried())
    }

    @Test
    fun `the lock clears itself and resets the attempts`() {
        var current = state()
        repeat(FACE_MAX_ATTEMPTS) { current = current.checking().rejected() }
        current = current.tick(FACE_LOCKOUT_SECONDS)

        assertFalse(current.isLocked)
        assertFalse(current.didNotMatch)
        assertEquals(FACE_MAX_ATTEMPTS, current.attemptsLeft)
        assertTrue(current.cameraActive)
    }

    @Test
    fun `a capture problem stops the check without spending an attempt`() {
        val problem = state().checking().withCaptureProblem(R.string.camera_problem_too_dark)

        assertEquals(R.string.camera_problem_too_dark, problem.captureProblem)
        assertFalse(problem.isChecking)
        assertEquals(FACE_MAX_ATTEMPTS, problem.attemptsLeft)
        assertNull(problem.retried().captureProblem)
    }

    @Test
    fun `a verified state never ticks`() {
        val verified = state().checking().matched()

        assertTrue(verified.isVerified)
        assertFalse(verified.needsTicking)
        assertEquals(verified, verified.ticked())
    }
}
