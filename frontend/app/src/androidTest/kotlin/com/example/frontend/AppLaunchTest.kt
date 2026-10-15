package com.example.frontend

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * On-device smoke test (KAN-43): the real activity starts, the splash hands over to
 * Welcome, and Get Started opens sign in. Run with ./gradlew connectedDebugAndroidTest.
 */
@RunWith(AndroidJUnit4::class)
class AppLaunchTest {

    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun appOpensOnWelcomeAndGetStartedLeadsToSignIn() {
        // The splash waits a moment before moving on.
        compose.waitUntil(timeoutMillis = 10_000) {
            compose.onAllNodes(androidx.compose.ui.test.hasText("Get Started"))
                .fetchSemanticsNodes().isNotEmpty()
        }

        compose.onNodeWithText("Get Started").performClick()

        compose.waitUntil(timeoutMillis = 5_000) {
            compose.onAllNodes(androidx.compose.ui.test.hasText("Sign in with Face Verification"))
                .fetchSemanticsNodes().isNotEmpty()
        }
    }
}
