package com.example.frontend.ui.screens.Vote

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
import com.example.frontend.ui.Components.PartyOption
import com.example.frontend.ui.Components.SectionHeading
import com.example.frontend.ui.Components.SimpleButton
import com.example.frontend.ui.theme.FrontendTheme
import com.example.frontend.ui.theme.InfoContainer
import com.example.frontend.ui.theme.InfoContent
import com.example.frontend.ui.theme.Spacing
import com.example.frontend.ui.theme.MutedText
import com.example.frontend.ui.theme.PageBackground

@Composable
fun VotePartySelectionScreen(
    onBackClick: () -> Unit,
    onContinue: (Party) -> Unit,
    modifier: Modifier = Modifier,
    onManifestoClick: (Party) -> Unit = {},
    onAllManifestosClick: () -> Unit = {},
    ballot: Ballot = Ballot.National,
    ballotTitle: String = ballot.title,
    parties: List<Party> = SampleParties,
    selectedTab: BottomTab = BottomTab.Vote,
    onTabSelected: (BottomTab) -> Unit = {},
    backgroundColor: Color = PageBackground
) {
    val entered by rememberEntranceTrigger()
    var selectedId by remember { mutableStateOf<String?>(null) }
    val selectedParty = parties.firstOrNull { it.id == selectedId }

    Scaffold(
        modifier = modifier,
        containerColor = backgroundColor,
        topBar = { AppTopBar(
                title = "National Election",
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

            // Manifesto Info Banner for Voters
            Card(
                onClick = onAllManifestosClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .entrance(entered, index = 1),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = InfoContainer)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.card, vertical = Spacing.small),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.item)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.MenuBook,
                        contentDescription = null,
                        tint = InfoContent,
                        modifier = Modifier.size(24.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Need information on parties?",
                            style = MaterialTheme.typography.titleSmall,
                            color = InfoContent
                        )
                        Text(
                            text = "Tap the manifesto icon on any party or view all party manifestos.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedText
                        )
                    }
                    OutlinedButton(
                        onClick = onAllManifestosClick,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Manifestos", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            SectionHeading(
                title = ballotTitle,
                description = "Select ONE party",
                modifier = Modifier.entrance(entered, index = 2)
            )

            parties.forEachIndexed { index, party ->
                PartyOption(
                    party = party,
                    selected = party.id == selectedId,
                    onClick = { selectedId = party.id },
                    onInfoClick = onManifestoClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .entrance(entered, index = index + 3)
                )
            }

            Spacer(modifier = Modifier.height(Spacing.tight))

            SimpleButton(
                label = "Continue",
                onClick = { selectedParty?.let(onContinue) },
                modifier = Modifier.fillMaxWidth(),
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
        VotePartySelectionScreen(onBackClick = {}, onContinue = {})
    }
}
