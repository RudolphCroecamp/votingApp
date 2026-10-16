package com.example.frontend.core

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.frontend.navigation.AppNavGraph
import com.example.frontend.navigation.Screen
import com.example.frontend.ui.Components.BALLOT_ORDER
import com.example.frontend.ui.screens.Vote.VoteFlowViewModel
import com.example.frontend.ui.theme.FrontendTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Core smoke test, run before every commit and in CI: a voter goes from Home through
 * all three ballots, casts the vote, and sees the receipts. If this breaks, the
 * app's main job is broken.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class CoreVotingJourneyTest {

    @get:Rule
    val compose = createComposeRule()

    private lateinit var nav: NavHostController
    private lateinit var voteFlow: VoteFlowViewModel

    private fun on(check: (NavDestination) -> Boolean) =
        assertTrue("unexpected screen ${nav.currentDestination?.route}", nav.currentDestination!!.let(check))

    private fun pick(party: String, next: String) {
        compose.onNodeWithText(party).performScrollTo().performClick()
        compose.onNodeWithText(next).performScrollTo().performClick()
        compose.waitForIdle()
    }

    @Test
    fun `voter casts all three ballots and gets three receipts`() {
        voteFlow = VoteFlowViewModel(SavedStateHandle())
        compose.setContent {
            nav = rememberNavController()
            FrontendTheme { AppNavGraph(navController = nav, voteFlowViewModel = voteFlow) }
        }
        compose.runOnIdle { nav.navigate(Screen.Home) }
        compose.waitForIdle()

        // Home -> Vote now opens the first ballot.
        compose.onAllNodesWithText("Vote now")[0].performScrollTo().performClick()
        compose.waitForIdle()
        on { it.hasRoute<Screen.PartySelection>() }

        pick("African National Congress (ANC)", "Next: Regional ballot")
        pick("Democratic Alliance (DA)", "Next: Provincial ballot")
        pick("Economic Freedom Fighters (EFF)", "Review my votes")
        on { it.hasRoute<Screen.Review>() }

        compose.onNodeWithText("Confirm Vote").performScrollTo().performClick()
        compose.waitForIdle()
        on { it.hasRoute<Screen.VoteFaceCheck>() }

        // The camera can't run on the JVM; pass the face check the way the screen does.
        compose.runOnIdle {
            voteFlow.onFaceCheckPassed(castAt = "2026-10-16 10:00")
            nav.navigate(Screen.Confirmation) { popUpTo<Screen.Home> { inclusive = false } }
        }
        compose.waitForIdle()

        val receipts = voteFlow.state.value.receipts
        assertEquals(BALLOT_ORDER, receipts.map { it.ballot })
        assertTrue("picks must be wiped once cast", voteFlow.state.value.selections.isEmpty())
        receipts.forEach { compose.onNodeWithText(it.code).assertExists() }

        // Done -> Home now shows the receipt card, and it opens the receipts.
        compose.onNodeWithText("Done").performScrollTo().performClick()
        compose.waitForIdle()
        on { it.hasRoute<Screen.Home>() }
        compose.onNodeWithText("View vote receipt").performScrollTo().performClick()
        compose.waitForIdle()
        on { it.hasRoute<Screen.Receipt>() }
        // Each code shows twice on a receipt card (heading and Receipt ID row).
        receipts.forEach { compose.onAllNodesWithText(it.code).onFirst().assertExists() }
    }
}
