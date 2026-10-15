package com.example.frontend.ui.screens.Vote

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.example.frontend.data.Party
import com.example.frontend.data.SampleParties
import com.example.frontend.ui.Components.Ballot
import com.example.frontend.ui.theme.FrontendTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Tapping a party hands exactly that party on (KAN-43). The full ballot → Review
 * journey is covered end to end in ThreeBallotNavigationTest.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PartySelectionTest {

    @get:Rule
    val compose = createComposeRule()

    @Test
    fun `each party row reports its own party`() {
        val picked = mutableListOf<Party>()
        compose.setContent {
            FrontendTheme {
                VotePartySelectionScreen(
                    onBackClick = {},
                    onPartySelected = { picked += it },
                    onContinue = {},
                    ballot = Ballot.Provincial
                )
            }
        }

        SampleParties.forEach { party ->
            compose.onNodeWithText(party.name).performScrollTo().performClick()
        }

        assertEquals(SampleParties.map { it.id }, picked.map { it.id })
    }

    @Test
    fun `the selected party survives into Review`() {
        val state = VoteFlowState()
            .select(Ballot.National, "da")
            .select(Ballot.Regional, "ifp")
            .select(Ballot.Provincial, "actionsa")

        val review = state.selections.resolveParties(SampleParties)

        assertEquals("Democratic Alliance (DA)", review.getValue(Ballot.National).name)
        assertEquals("Inkatha Freedom Party (IFP)", review.getValue(Ballot.Regional).name)
        assertEquals("ActionSA", review.getValue(Ballot.Provincial).name)
    }
}
