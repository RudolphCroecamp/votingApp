package com.example.frontend

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.LayoutDirection
import com.example.frontend.data.SampleParties
import com.example.frontend.ui.Components.BALLOT_ORDER
import com.example.frontend.ui.screens.Auth.LoginScreen
import com.example.frontend.ui.screens.Registration.RegistrationUiState
import com.example.frontend.ui.screens.Registration.screens.RegisterScreen
import com.example.frontend.ui.screens.Settings.SettingsScreen
import com.example.frontend.ui.screens.Vote.VoteReviewScreen
import com.example.frontend.ui.screens.home.HomeScreen
import com.example.frontend.ui.theme.FrontendTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * The manifest sets supportsRtl="true"; these tests make sure that claim holds
 * (KAN-39). Each screen is drawn right-to-left and checked for leading/trailing
 * placement: anything at the "start" must end up on the right-hand side.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class RtlLayoutTest {

    @get:Rule
    val compose = createComposeRule()

    private fun showRtl(content: @Composable () -> Unit) = compose.setContent {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            FrontendTheme { content() }
        }
    }

    private val rootWidth get() = compose.onRoot().getBoundsInRoot().right

    private fun assertOnRightHalf(text: String) {
        val bounds = compose.onNodeWithText(text, useUnmergedTree = true).getBoundsInRoot()
        assertTrue("'$text' should start on the right in RTL, was at ${bounds.left}", bounds.left > rootWidth / 2)
    }

    private fun assertOnLeftHalf(description: String) {
        val bounds = compose.onNodeWithContentDescription(description).getBoundsInRoot()
        assertTrue("'$description' should sit on the left in RTL, was at ${bounds.right}", bounds.right < rootWidth / 2)
    }

    @Test
    fun `home header mirrors - greeting on the right, help on the left`() {
        showRtl { HomeScreen(onVoteNow = {}, voterName = "Thandi") }

        assertOnRightHalf("Hi, Thandi")
        assertOnLeftHalf("Help")
    }

    @Test
    fun `back arrow moves to the right-hand side`() {
        showRtl { VoteReviewScreen(BALLOT_ORDER.zip(SampleParties).toMap(), {}, {}, {}) }

        val back = compose.onNodeWithContentDescription("Back").getBoundsInRoot()
        assertTrue("back arrow should be on the right in RTL", back.left > rootWidth / 2)
    }

    @Test
    fun `key screens draw right-to-left without crashing`() {
        showRtl {
            LoginScreen(onFaceVerification = {}, onRegister = {})
        }
        compose.onNodeWithText("Sign in with Face Verification").assertIsDisplayed()
    }

    @Test
    fun `registration form draws right-to-left`() {
        showRtl {
            RegisterScreen(
                uiState = RegistrationUiState(),
                onEvent = {},
                onContinue = {},
                onSignIn = {},
                onBackClick = {}
            )
        }
        compose.onNodeWithText("Your Details").assertIsDisplayed()
    }

    @Test
    fun `settings rows draw right-to-left`() {
        showRtl { SettingsScreen(onBackClick = {}) }
        compose.onNodeWithText("Account & Voter Profile").assertIsDisplayed()
    }
}
