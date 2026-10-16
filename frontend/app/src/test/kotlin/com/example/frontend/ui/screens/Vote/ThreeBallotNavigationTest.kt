package com.example.frontend.ui.screens.Vote

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.frontend.navigation.AppNavGraph
import com.example.frontend.navigation.Screen
import com.example.frontend.ui.components.Ballot
import com.example.frontend.ui.screens.Auth.LoginFaceUiState
import com.example.frontend.ui.screens.Auth.checking
import com.example.frontend.ui.screens.Auth.matched
import com.example.frontend.ui.theme.FrontendTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Drives the real nav graph through the three-ballot flow (KAN-17/18/19). */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ThreeBallotNavigationTest {

    @get:Rule
    val compose = createComposeRule()

    private lateinit var nav: NavHostController
    private lateinit var voteFlow: VoteFlowViewModel

    private fun startAtBallots() {
        voteFlow = VoteFlowViewModel(SavedStateHandle())
        compose.setContent {
            nav = rememberNavController()
            FrontendTheme { AppNavGraph(navController = nav, voteFlowViewModel = voteFlow) }
        }
        compose.runOnIdle {
            nav.navigate(Screen.Home)
            nav.navigate(Screen.PartySelection())
        }
        compose.waitForIdle()
    }

    private fun pick(partyName: String, continueLabel: String) {
        compose.onNodeWithText(partyName).performScrollTo().performClick()
        compose.onNodeWithText(continueLabel).performScrollTo().performClick()
        compose.waitForIdle()
    }

    private fun currentIs(check: (androidx.navigation.NavDestination) -> Boolean): Boolean =
        nav.currentBackStackEntry?.destination?.let(check) == true

    @Test
    fun `voter walks all three ballots in order and reviews every pick`() {
        startAtBallots()

        compose.onNodeWithText("Ballot 1 of 3").assertIsDisplayed()
        compose.onNodeWithText("Next: Regional ballot").assertIsNotEnabled() // nothing picked yet
        pick("African National Congress (ANC)", "Next: Regional ballot")

        compose.onNodeWithText("Ballot 2 of 3").assertIsDisplayed()
        pick("Democratic Alliance (DA)", "Next: Provincial ballot")

        compose.onNodeWithText("Ballot 3 of 3").assertIsDisplayed()
        pick("Economic Freedom Fighters (EFF)", "Review my votes")

        assertTrue(currentIs { it.hasRoute<Screen.Review>() })
        compose.onNodeWithText("African National Congress (ANC)").assertExists()
        compose.onNodeWithText("Democratic Alliance (DA)").assertExists()
        compose.onNodeWithText("Economic Freedom Fighters (EFF)").assertExists()
        assertEquals(
            mapOf(Ballot.National to "anc", Ballot.Regional to "da", Ballot.Provincial to "eff"),
            voteFlow.state.value.selections
        )
    }

    @Test
    fun `change on review edits one ballot and comes straight back`() {
        startAtBallots()
        pick("African National Congress (ANC)", "Next: Regional ballot")
        pick("Democratic Alliance (DA)", "Next: Provincial ballot")
        pick("Economic Freedom Fighters (EFF)", "Review my votes")

        compose.onAllNodesWithText("Change")[1].performScrollTo().performClick() // Regional
        compose.waitForIdle()
        compose.onNodeWithText("Ballot 2 of 3").assertIsDisplayed()
        pick("Inkatha Freedom Party (IFP)", "Back to review")

        assertTrue(currentIs { it.hasRoute<Screen.Review>() })
        compose.onNodeWithText("Inkatha Freedom Party (IFP)").assertExists()
        compose.onNodeWithText("Democratic Alliance (DA)").assertDoesNotExist()
        assertEquals("anc", voteFlow.state.value.selectionFor(Ballot.National))
        assertEquals("ifp", voteFlow.state.value.selectionFor(Ballot.Regional))
    }

    @Test
    fun `going back keeps the earlier pick selected`() {
        startAtBallots()
        pick("African National Congress (ANC)", "Next: Regional ballot")

        compose.runOnIdle { nav.popBackStack() }
        compose.waitForIdle()

        // Scroll position is restored on Back, so the header can sit off-screen.
        compose.onNodeWithText("Ballot 1 of 3").assertExists()
        assertEquals("anc", voteFlow.state.value.selectionFor(Ballot.National))
        // Continue is enabled only because the pick is still there.
        compose.onNodeWithText("Next: Regional ballot").performScrollTo().performClick()
        compose.waitForIdle()
        compose.onNodeWithText("Ballot 2 of 3").assertIsDisplayed()
    }

    @Test
    fun `confirm vote opens the face check`() {
        startAtBallots()
        pick("African National Congress (ANC)", "Next: Regional ballot")
        pick("Democratic Alliance (DA)", "Next: Provincial ballot")
        pick("Economic Freedom Fighters (EFF)", "Review my votes")

        compose.onNodeWithText("Confirm Vote").performScrollTo().performClick()
        compose.waitForIdle()

        assertTrue(currentIs { it.hasRoute<Screen.VoteFaceCheck>() })
        compose.onNodeWithText("Scan your face to cast your vote").assertIsDisplayed()
        assertFalse("nothing is cast before the scan", voteFlow.state.value.hasVoted)
    }

    @Test
    fun `failed face check shows the error on review and locks after three`() {
        startAtBallots()
        pick("African National Congress (ANC)", "Next: Regional ballot")
        pick("Democratic Alliance (DA)", "Next: Provincial ballot")
        pick("Economic Freedom Fighters (EFF)", "Review my votes")

        compose.runOnIdle { voteFlow.onFaceCheckFailed() }
        compose.onNodeWithText("Face didn't match. Your vote was NOT submitted. 2 tries left.")
            .assertIsDisplayed()

        compose.runOnIdle { repeat(2) { voteFlow.onFaceCheckFailed() } }
        compose.onNodeWithText("Confirm Vote").performScrollTo().assertIsNotEnabled()
    }

    @Test
    fun `after voting, home shows the receipts instead of new ballots`() {
        startAtBallots()
        pick("African National Congress (ANC)", "Next: Regional ballot")
        pick("Democratic Alliance (DA)", "Next: Provincial ballot")
        pick("Economic Freedom Fighters (EFF)", "Review my votes")

        compose.runOnIdle {
            voteFlow.onFaceCheckPassed(castAt = "2026-10-15 10:00")
            nav.navigate(Screen.Confirmation)
        }
        compose.onNodeWithText("All 3 of your votes have been recorded.").assertExists()
        voteFlow.state.value.receipts.forEach { compose.onNodeWithText(it.code).assertExists() }

        compose.onNodeWithText("Done").performScrollTo().performClick()
        compose.waitForIdle()
        assertTrue(currentIs { it.hasRoute<Screen.Home>() })
        // Home swaps Vote now for the receipt card once the vote is cast (KAN-54).
        compose.onNodeWithText("Vote now").assertDoesNotExist()
        compose.onNodeWithText("View vote receipt").performScrollTo().performClick()
        compose.waitForIdle()
        assertTrue(currentIs { it.hasRoute<Screen.Receipt>() })
    }
}

/** Face check screen hands its outcome to the flow exactly as KAN-19 asks. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class VoteFaceCheckScreenTest {

    @get:Rule
    val compose = createComposeRule()

    private fun render(state: LoginFaceUiState): Pair<Int, Int> {
        var verified = 0
        var failed = 0
        compose.setContent {
            FrontendTheme {
                VoteFaceCheckScreen(
                    uiState = state,
                    onEvent = {},
                    onBackClick = {},
                    onVerified = { verified++ },
                    onFailed = { failed++ }
                )
            }
        }
        compose.waitForIdle()
        return verified to failed
    }

    @Test
    fun `pass submits`() {
        assertEquals(1 to 0, render(LoginFaceUiState(idNumber = "1").checking().matched()))
    }

    @Test
    fun `fail returns to review`() {
        assertEquals(0 to 1, render(LoginFaceUiState(idNumber = "1").checking().voteRejected()))
    }

    @Test
    fun `still checking does neither`() {
        assertEquals(0 to 0, render(LoginFaceUiState(idNumber = "1").checking()))
    }
}
