package com.example.frontend.ui.screens.Manifestos

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.frontend.data.Party
import com.example.frontend.data.SampleParties
import com.example.frontend.effects.pressScale
import com.example.frontend.ui.Components.AppTopBar
import com.example.frontend.ui.Components.LeaderAvatar
import com.example.frontend.ui.theme.CardBorder
import com.example.frontend.ui.theme.FrontendTheme
import com.example.frontend.ui.theme.MutedText
import com.example.frontend.ui.theme.Spacing

// TODO: Add Screen.Manifestos to Screen.kt and a composable<Screen.Manifestos> in AppNavGraph.
// TODO: Add a Manifestos slot to AppBottomBar (requirement V-07, N-07).
// TODO: Load parties from the backend once the network layer exists.

/** Manifestos tab: every party on the ballot, tap one for its full manifesto (V-07). */
@Composable
fun ManifestosScreen(
    onPartyClick: (Party) -> Unit,
    modifier: Modifier = Modifier,
    onBackClick: (() -> Unit)? = null, // null when shown as a bottom-bar tab

    parties: List<Party> = SampleParties
) {
    Scaffold(
        modifier = modifier,
        topBar = { AppTopBar(title = "Manifestos", onBackClick = onBackClick) }
    ) { innerPadding ->
        // Page 1: LazyColumn only composes the rows on screen.
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(Spacing.screen),
            verticalArrangement = Arrangement.spacedBy(Spacing.item)
        ) {
            item {
                Column(
                    modifier = Modifier.padding(bottom = Spacing.small),
                    verticalArrangement = Arrangement.spacedBy(Spacing.tight)
                ) {
                    Text(
                        text = "Political party manifestos",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Find out what each party stands for before you vote.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MutedText
                    )
                }
            }
            // Stable keys keep scroll position and animations right if the list changes.
            items(items = parties, key = { it.id }) { party ->
                ManifestoPartyCard(
                    party = party,
                    onClick = { onPartyClick(party) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/** Page 2: clickable Card with the party logo, leader and slogan. */
@Composable
private fun ManifestoPartyCard(
    party: Party,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    Card(
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = modifier.pressScale(interactionSource),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.card),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.item)
        ) {
            // Page 3: Fit keeps the whole logo visible inside a fixed box.
            Image(
                painter = painterResource(party.logo),
                contentDescription = "${party.name} logo",
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(56.dp)
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Spacing.tight)
            ) {
                Text(
                    text = party.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = party.slogan,
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedText
                )
                if (party.leaderName.isNotBlank()) {
                    Row(
                        modifier = Modifier.padding(top = Spacing.tight),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Spacing.small)
                    ) {
                        LeaderAvatar(
                            name = party.leaderName,
                            photo = party.leaderPhoto,
                            size = 24.dp
                        )
                        Text(
                            text = "Leader: ${party.leaderName}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null, // the whole card is the button
                tint = MutedText
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 760)
@Composable
private fun ManifestosScreenPreview() {
    FrontendTheme {
        ManifestosScreen(onPartyClick = {})
    }
}
