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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.frontend.R
import androidx.compose.ui.unit.dp
import com.example.frontend.data.Party
import com.example.frontend.data.SampleParties
import com.example.frontend.ui.components.AppBottomBar
import com.example.frontend.ui.components.BottomTab
import com.example.frontend.ui.components.AppTopBar
import com.example.frontend.ui.components.Ballot
import com.example.frontend.ui.components.BallotStepIndicator
import com.example.frontend.ui.components.ButtonVariant
import com.example.frontend.ui.components.DescriptionLabel
import com.example.frontend.ui.components.HeadingLabel
import com.example.frontend.ui.components.PartyOption
import com.example.frontend.ui.components.SectionHeading
import com.example.frontend.ui.components.SimpleButton
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
    continueLabel: String = stringResource(R.string.action_continue),
    ballotTitle: String = stringResource(ballot.titleRes),
    parties: List<Party> = SampleParties,
    selectedTab: BottomTab = BottomTab.Vote,
    onTabSelected: (BottomTab) -> Unit = {},
    backgroundColor: Color = PageBackground
) {

    Scaffold(
        modifier = modifier,
        containerColor = backgroundColor,
        topBar = { AppTopBar(
                title = stringResource(ballot.titleRes),
                subtitle = stringResource(R.string.vote_choose_party),
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
                modifier = Modifier
            )

            SectionHeading(
                title = ballotTitle,
                description = stringResource(R.string.vote_select_one_party),
                modifier = Modifier
            )

            parties.forEachIndexed { index, party ->
                PartyOption(
                    party = party,
                    selected = party.id == selectedPartyId,
                    onClick = { onPartySelected(party) },
                    modifier = Modifier
                        .fillMaxWidth()
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
