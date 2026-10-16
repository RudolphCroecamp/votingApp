package com.example.frontend.navigation

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.frontend.ui.Components.FullScreenLoading
import com.example.frontend.ui.theme.FrontendTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Manifestos tab and party detail must be registered routes, not crashes. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ManifestosNavigationTest {

    @get:Rule
    val compose = createComposeRule()

    private lateinit var nav: NavHostController

    private fun start(vararg routes: Screen) {
        compose.setContent {
            nav = rememberNavController()
            FrontendTheme { AppNavGraph(navController = nav) }
        }
        compose.runOnIdle { routes.forEach { nav.navigate(it) } }
        compose.waitForIdle()
    }

    @Test
    fun `manifestos tab opens the party list`() {
        start(Screen.Home, Screen.Manifestos)
        assertTrue(nav.currentDestination!!.hasRoute<Screen.Manifestos>())
    }

    @Test
    fun `tapping a party opens its manifesto`() {
        start(Screen.Home, Screen.Manifestos)
        compose.onAllNodesWithText("African National Congress", substring = true)[0]
            .performScrollTo().performClick()
        compose.waitForIdle()
        assertTrue(nav.currentDestination!!.hasRoute<Screen.PartyDetail>())
    }

    @Test
    fun `unknown party id goes back instead of crashing`() {
        start(Screen.Home, Screen.Manifestos, Screen.PartyDetail("no-such-party"))
        assertTrue(nav.currentDestination!!.hasRoute<Screen.Manifestos>())
    }

    @Test
    fun `full screen loading shows its label`() {
        compose.setContent { FrontendTheme { FullScreenLoading(label = "Submitting your vote…") } }
        compose.onNodeWithText("Submitting your vote…").assertExists()
    }
}
