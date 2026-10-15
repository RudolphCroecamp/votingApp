package com.example.frontend.ui.screens.Vote

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.example.frontend.effects.entrance
import com.example.frontend.effects.rememberEntranceTrigger
import androidx.compose.ui.unit.dp
import com.example.frontend.data.Party
import com.example.frontend.data.SampleParties
import com.example.frontend.ui.Components.AppBottomBar
import com.example.frontend.ui.Components.BottomTab
import com.example.frontend.ui.Components.AppTopBar
import com.example.frontend.ui.Components.Ballot
import com.example.frontend.ui.Components.BallotStepIndicator
import com.example.frontend.ui.Components.ButtonVariant
import com.example.frontend.ui.Components.DescriptionLabel
import com.example.frontend.ui.Components.HeadingLabel
import com.example.frontend.ui.Components.PartyOption
import com.example.frontend.ui.Components.SectionHeading
import com.example.frontend.ui.Components.SimpleButton
import com.example.frontend.ui.theme.FrontendTheme
import com.example.frontend.ui.theme.Spacing
import com.example.frontend.ui.theme.MutedText
import com.example.frontend.ui.theme.PageBackground

@Composable
fun VotePartySelectionScreen(
    onBackClick: () -> Unit,
    onPartySelected: (Party) -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
    ballot: Ballot = Ballot.National,
    selectedPartyId: String? = null,
    continueLabel: String = "Continue",
    ballotTitle: String = ballot.title,
    parties: List<Party> = SampleParties,
    selectedTab: BottomTab = BottomTab.Vote,
    onTabSelected: (BottomTab) -> Unit = {},
    backgroundColor: Color = PageBackground
) {
    val entered by rememberEntranceTrigger()

    Scaffold(
        modifier = modifier,
        containerColor = backgroundColor,
        topBar = { AppTopBar(
                title = ballot.title,
                subtitle = "Choose your party",
                onBackClick = onBackClick
            ) },
        bottomBar = { AppBottomBar(selected = selectedTab, onSelect = onTabSelected) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(Spacing.screen),
            verticalArrangement = Arrangement.spacedBy(Spacing.item)
        ) {
            BallotStepIndicator(
                ballot = ballot,
                modifier = Modifier.entrance(entered, index = 0)
            )

            SectionHeading(
                title = ballotTitle,
                description = "Select ONE party",
                modifier = Modifier.entrance(entered, index = 1)
            )

            parties.forEachIndexed { index, party ->
                PartyOption(
                    party = party,
                    selected = party.id == selectedPartyId,
                    onClick = { onPartySelected(party) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .entrance(entered, index = index + 2)
                )
            }

            Spacer(modifier = Modifier.height(Spacing.tight))

            SimpleButton(
                label = continueLabel,
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth(),
                enabled = parties.any { it.id == selectedPartyId },
                variant = ButtonVariant.Tertiary,
                trailingIcon = Icons.AutoMirrored.Filled.ArrowForward
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun VotePartySelectionScreenPreview() {
    FrontendTheme {
        VotePartySelectionScreen(
            onBackClick = {},
            onPartySelected = {},
            onContinue = {},
            ballot = Ballot.Regional,
            selectedPartyId = SampleParties.first().id
        )
    }
}
