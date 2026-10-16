package com.example.frontend.ui.screens.Registration

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.frontend.navigation.AppNavGraph
import com.example.frontend.navigation.Screen
import com.example.frontend.ui.theme.FrontendTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Registration screens move forward and back through the real nav graph (KAN-43). */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class RegistrationNavigationTest {

    @get:Rule
    val compose = createComposeRule()

    private lateinit var nav: NavHostController
    private val registration = RegistrationViewModel()

    private fun startAtDetails() {
        listOf(
            RegistrationUiEvent.OnFullNamesChanged("Thandi Nkosi"),
            RegistrationUiEvent.OnIdNumberChanged("9201185082089"),
            RegistrationUiEvent.OnPhoneChanged("0821234567"),
            RegistrationUiEvent.OnProvinceSelected(Province.GAUTENG),
            RegistrationUiEvent.OnStreetChanged("124 Market Street"),
            RegistrationUiEvent.OnCityChanged("Johannesburg"),
            RegistrationUiEvent.OnPostalCodeChanged("2001")
        ).forEach(registration::onEvent)

        compose.setContent {
            nav = rememberNavController()
            FrontendTheme { AppNavGraph(navController = nav, registrationViewModel = registration) }
        }
        compose.runOnIdle { nav.navigate(Screen.Register) }
        compose.waitForIdle()
    }

    private fun currentIs(check: (androidx.navigation.NavDestination) -> Boolean): Boolean =
        nav.currentBackStackEntry?.destination?.let(check) == true

    private fun tapBack() {
        compose.onNodeWithContentDescription("Back").performClick()
        compose.waitForIdle()
    }

    @Test
    fun `continue goes to the SMS code step and back returns to details`() {
        startAtDetails()
        compose.onNodeWithText("Your Details").assertIsDisplayed()

        compose.onNodeWithText("Continue").performScrollTo().performClick()
        compose.waitForIdle()

        assertTrue(currentIs { it.hasRoute<Screen.RegisterOtp>() })
        compose.onNodeWithText("Enter your code").assertIsDisplayed()
        assertEquals(2, registration.uiState.value.currentStep)

        tapBack()

        assertTrue(currentIs { it.hasRoute<Screen.Register>() })
        // The form keeps its scroll position, so scroll back up before checking.
        compose.onNodeWithText("Your Details").performScrollTo().assertIsDisplayed()
        assertEquals(1, registration.uiState.value.currentStep)
    }

    @Test
    fun `typed details are still there after going back`() {
        startAtDetails()
        compose.onNodeWithText("Continue").performScrollTo().performClick()
        compose.waitForIdle()

        tapBack()

        compose.onNodeWithText("Thandi Nkosi").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("9201185082089").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun `consent step back goes to the step before it`() {
        startAtDetails()
        compose.runOnIdle {
            registration.onEvent(RegistrationUiEvent.OnNextStepClicked) // now on step 2 (SMS code)
            registration.onEvent(RegistrationUiEvent.OnNextStepClicked) // step 3 (consent)
            nav.navigate(Screen.PrivacyConsent)
        }
        compose.waitForIdle()
        compose.onNodeWithText("What we collect and why").assertIsDisplayed()

        tapBack()

        assertTrue(currentIs { it.hasRoute<Screen.Register>() })
        assertEquals(2, registration.uiState.value.currentStep)
    }
}
