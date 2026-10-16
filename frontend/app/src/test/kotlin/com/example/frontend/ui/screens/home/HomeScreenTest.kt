package com.example.frontend.ui.screens.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.frontend.ui.components.HeroState
import com.example.frontend.ui.theme.FrontendTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** The home hero card shows the right state and action for each phase (KAN-43). */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class HomeScreenTest {

    @get:Rule
    val compose = createComposeRule()

    private var voteNowClicks = 0
    private var receiptClicks = 0
    private var remindClicks = 0

    private fun show(state: HeroState) = compose.setContent {
        FrontendTheme {
            HomeScreen(
                onVoteNow = { voteNowClicks++ },
                onVerifyVote = { receiptClicks++ },
                onRemindMe = { remindClicks++ },
                voterName = "Thandi",
                heroState = state
            )
        }
    }

    @Test
    fun `voting open shows the election and a vote now button`() {
        show(HeroState.VotingOpen)

        compose.onNodeWithText("VOTING OPEN").assertIsDisplayed()
        compose.onNodeWithText("2026 Local Government Election").assertIsDisplayed()
        compose.onNodeWithText("Vote now").performClick()
        assertEquals(1, voteNowClicks)
    }

    @Test
    fun `after voting it thanks the voter and links to the receipt`() {
        show(HeroState.VoteCast)

        compose.onNodeWithText("VOTE CAST").assertIsDisplayed()
        compose.onNodeWithText("You made your mark, Thandi!").assertIsDisplayed()
        compose.onNodeWithText("Vote now").assertDoesNotExist()
        compose.onNodeWithText("View vote receipt").performClick()
        assertEquals(1, receiptClicks)
    }

    @Test
    fun `before voting opens it offers a reminder instead of a ballot`() {
        show(HeroState.ComingUp)

        compose.onNodeWithText("COMING UP").assertIsDisplayed()
        compose.onNodeWithText("Vote now").assertDoesNotExist()
        compose.onNodeWithText("Remind me").performClick()
        assertEquals(1, remindClicks)
    }

    @Test
    fun `the greeting uses the voter's name`() {
        show(HeroState.VotingOpen)
        compose.onNodeWithText("Hi, Thandi").assertIsDisplayed()
    }
}
