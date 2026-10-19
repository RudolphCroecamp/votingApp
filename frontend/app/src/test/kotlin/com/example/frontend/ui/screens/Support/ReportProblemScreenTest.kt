package com.example.frontend.ui.screens.Support

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import com.example.frontend.ui.theme.FrontendTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Report a Problem builds a message for the dev team and explains what's missing. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ReportProblemScreenTest {

    @get:Rule
    val compose = createComposeRule()

    private var sent: ProblemReport? = null

    private fun start(emailAppInstalled: Boolean = true) = compose.setContent {
        FrontendTheme {
            ReportProblemScreen(onBackClick = {}, onSend = { sent = it; emailAppInstalled })
        }
    }

    @Test
    fun `the report carries the category, message, reply address and device`() {
        val report = buildProblemReport(ProblemCategory.VOTING, "  My vote did not show  ", "me@example.com", "App 1.0")
        assertEquals("[VotingApp] Voting concern", report.subject)
        assertTrue(report.body.startsWith("My vote did not show\n"))
        assertTrue(report.body.contains("Reply to: me@example.com"))
        assertTrue(report.body.endsWith("App 1.0"))
    }

    @Test
    fun `short messages and bad emails are caught before sending`() {
        assertEquals(ReportStatus.MessageTooShort, validateReport("broken", ""))
        assertEquals(ReportStatus.EmailInvalid, validateReport("The receipt screen is blank", "me@"))
        assertEquals(ReportStatus.Idle, validateReport("The receipt screen is blank", ""))
    }

    @Test
    fun `sending a short message asks for more detail`() {
        start()
        compose.onNodeWithText("Send to the dev team").performScrollTo().performClick()
        assertNull(sent)
        compose.onNodeWithText("Tell us a little more", substring = true).assertExists()
    }

    @Test
    fun `a complete report opens the email app`() {
        start()
        compose.onNodeWithText("Account & login").performClick()
        compose.onNodeWithText("Describe the problem…").performTextInput("I can't sign in with my face")
        compose.onNodeWithText("Send to the dev team").performScrollTo().performClick()

        assertEquals("[VotingApp] Account & login", sent?.subject)
        compose.onNodeWithText("Your email app is open", substring = true).performScrollTo().assertExists()
    }

    @Test
    fun `without an email app the voter gets the address instead`() {
        start(emailAppInstalled = false)
        compose.onNodeWithText("Describe the problem…").performTextInput("I can't sign in with my face")
        compose.onNodeWithText("Send to the dev team").performScrollTo().performClick()
        compose.onNodeWithText("No email app on this phone", substring = true).performScrollTo().assertExists()
    }
}
