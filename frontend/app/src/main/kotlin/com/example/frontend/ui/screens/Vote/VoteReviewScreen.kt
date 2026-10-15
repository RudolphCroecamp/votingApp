package com.example.frontend.ui.screens.Vote

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.frontend.data.Party
import com.example.frontend.data.SampleParties
import com.example.frontend.effects.entrance
import com.example.frontend.effects.rememberEntranceTrigger
import com.example.frontend.ui.Components.AppBottomBar
import com.example.frontend.ui.Components.AppTopBar
import com.example.frontend.ui.Components.BALLOT_ORDER
import com.example.frontend.ui.Components.Ballot
import com.example.frontend.ui.Components.BottomTab
import com.example.frontend.ui.Components.PartyCard
import com.example.frontend.ui.Components.SectionHeading
import com.example.frontend.ui.Components.SimpleButton
import com.example.frontend.ui.Components.StatusLabel
import com.example.frontend.ui.Components.StatusType
import com.example.frontend.ui.theme.FrontendTheme
import com.example.frontend.ui.theme.MutedText
import com.example.frontend.ui.theme.PageBackground
import com.example.frontend.ui.theme.Spacing

/**
 * Review all three picks before the face check (KAN-18).
 * [selections] maps each ballot to its chosen party; a missing entry shows "Not chosen yet".
 */
@Composable
fun VoteReviewScreen(
    selections: Map<Ballot, Party>,
    onChangeBallot: (Ballot) -> Unit,
    onConfirmVote: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    errorMessage: String? = null,
    confirmEnabled: Boolean = BALLOT_ORDER.all { it in selections },
    selectedTab: BottomTab = BottomTab.Vote,
    onTabSelected: (BottomTab) -> Unit = {},
    backgroundColor: Color = PageBackground
) {
    val entered by rememberEntranceTrigger()

    Scaffold(
        modifier = modifier,
        containerColor = backgroundColor,
        topBar = { AppTopBar(
                title = "Review your vote",
                subtitle = "Check all ${BALLOT_ORDER.size} ballots",
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
            verticalArrangement = Arrangement.spacedBy(Spacing.item),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (errorMessage != null) {
                StatusLabel(
                    message = errorMessage,
                    type = StatusType.Error,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            BALLOT_ORDER.forEachIndexed { index, ballot ->
                BallotChoice(
                    ballot = ballot,
                    party = selections[ballot],
                    onChange = { onChangeBallot(ballot) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .entrance(entered, index = index)
                )
            }

            StatusLabel(
                message = "Your votes cannot be changed after confirmation. " +
                    "You will scan your face once more to cast them.",
                type = StatusType.Warning,
                modifier = Modifier
                    .fillMaxWidth()
                    .entrance(entered, index = BALLOT_ORDER.size)
            )

            Spacer(modifier = Modifier.height(Spacing.small))

            SimpleButton(
                label = "Confirm Vote",
                onClick = onConfirmVote,
                enabled = confirmEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .entrance(entered, index = BALLOT_ORDER.size + 1)
            )
        }
    }
}

@Composable
private fun BallotChoice(
    ballot: Ballot,
    party: Party?,
    onChange: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Spacing.tight)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SectionHeading(
                title = ballot.title,
                description = "Ballot ${BALLOT_ORDER.indexOf(ballot) + 1} of ${BALLOT_ORDER.size}",
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onChange) {
                Icon(Icons.Default.Edit, contentDescription = null)
                Text(
                    text = if (party == null) "Choose" else "Change",
                    modifier = Modifier.padding(start = Spacing.tight),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
        if (party != null) {
            PartyCard(
                title = party.name,
                slogan = party.slogan,
                logo = painterResource(party.logo),
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Text(
                text = "Not chosen yet",
                style = MaterialTheme.typography.bodyMedium,
                color = MutedText
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun VoteReviewScreenPreview() {
    FrontendTheme {
        VoteReviewScreen(
            selections = BALLOT_ORDER.zip(SampleParties).toMap(),
            onChangeBallot = {},
            onConfirmVote = {},
            onBackClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun VoteReviewScreenFaceFailedPreview() {
    FrontendTheme {
        VoteReviewScreen(
            selections = BALLOT_ORDER.zip(SampleParties).toMap(),
            onChangeBallot = {},
            onConfirmVote = {},
            onBackClick = {},
            errorMessage = "Face didn't match. Your vote was NOT submitted. 2 tries left."
        )
    }
}
