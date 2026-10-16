package com.example.frontend.ui.components.Otp

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OtpChallengeTest {

    private fun sentState() = OtpState().sent(destination = "•••• ••• 5678")

    private fun OtpState.tick(times: Int): OtpState {
        var state = this
        repeat(times) { state = state.ticked() }
        return state
    }

    @Test
    fun `sending starts both countdowns`() {
        val state = sentState()

        assertEquals(OTP_RESEND_COOLDOWN_SECONDS, state.resendSecondsRemaining)
        assertEquals(OTP_EXPIRY_SECONDS, state.expirySecondsRemaining)
        assertFalse(state.canResend)
        assertFalse(state.canSubmit)
    }

    @Test
    fun `code keeps digits only and stops at the code length`() {
        val state = sentState().withCode("12a34b567890")

        assertEquals("123456", state.code)
        assertTrue(state.canSubmit)
    }

    @Test
    fun `resend unlocks once the cooldown runs out`() {
        val state = sentState().tick(OTP_RESEND_COOLDOWN_SECONDS)

        assertEquals(0, state.resendSecondsRemaining)
        assertTrue(state.canResend)
    }

    @Test
    fun `code expires and clears what was typed`() {
        val state = sentState().withCode("123").tick(OTP_EXPIRY_SECONDS)

        assertEquals(OtpError.Expired, state.error)
        assertEquals("", state.code)
        assertFalse(state.canSubmit)
        assertTrue(state.canResend)
    }

    @Test
    fun `wrong code counts the attempt and clears the boxes`() {
        val state = sentState().withCode("000000").verifying().rejected()

        assertEquals(OtpError.WrongCode, state.error)
        assertEquals("", state.code)
        assertEquals(OTP_MAX_ATTEMPTS - 1, state.attemptsLeft)
        assertFalse(state.isLocked)
    }

    @Test
    fun `typing again clears the wrong code error`() {
        val state = sentState().verifying().rejected().withCode("1")

        assertNull(state.error)
    }

    @Test
    fun `too many wrong codes locks the step`() {
        var state = sentState()
        repeat(OTP_MAX_ATTEMPTS) { state = state.withCode("000000").verifying().rejected() }

        assertEquals(OtpError.TooManyAttempts, state.error)
        assertEquals(OTP_LOCKOUT_SECONDS, state.lockSecondsRemaining)
        assertTrue(state.isLocked)
        assertFalse(state.canSubmit)
        assertFalse(state.canResend)
    }

    @Test
    fun `a locked step ignores typing and clears when the lockout ends`() {
        var state = sentState()
        repeat(OTP_MAX_ATTEMPTS) { state = state.withCode("000000").verifying().rejected() }

        assertEquals("", state.withCode("123456").code)

        state = state.tick(OTP_LOCKOUT_SECONDS)

        assertFalse(state.isLocked)
        assertNull(state.error)
        assertEquals(OTP_MAX_ATTEMPTS, state.attemptsLeft)
        assertTrue(state.canResend)
    }

    @Test
    fun `verifying stops the countdowns`() {
        val state = sentState().withCode("123456").verifying().verified()

        assertTrue(state.isVerified)
        assertFalse(state.needsTicking)
        assertEquals(state, state.ticked())
    }

    @Test
    fun `destination hides all but the last four digits`() {
        assertEquals("•••• ••• 5678", maskDestination("071 234 5678"))
        // Too short to mask: blank, so the UI falls back to its translated "your cellphone".
        assertEquals("", maskDestination("07"))
    }

    @Test
    fun `countdown reads as minutes only when there are minutes left`() {
        assertEquals("15:00", formatCountdown(OTP_LOCKOUT_SECONDS))
        assertEquals("1:05", formatCountdown(65))
        assertEquals("30s", formatCountdown(30))
    }
}
