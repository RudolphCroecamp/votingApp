package com.example.frontend.ui.screens.Manifestos

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.frontend.data.Party
import com.example.frontend.data.SampleParties
import com.example.frontend.ui.Components.AppTopBar
import com.example.frontend.ui.Components.LeaderAvatar
import com.example.frontend.ui.theme.CardBorder
import com.example.frontend.ui.theme.FrontendTheme
import com.example.frontend.ui.theme.InfoContainer
import com.example.frontend.ui.theme.MutedText
import com.example.frontend.ui.theme.Spacing

// TODO: Add a type-safe route, e.g. data class PartyDetail(val partyId: String), to Screen.kt
//       and look the party up by id in AppNavGraph (page 4: type-safe navigation).
// TODO: Open this screen from each party on VotePartySelectionScreen as well.

/** One party's logo, leader and numbered manifesto points (V-07). */
@Composable
fun PartyDetailScreen(
    party: Party,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = { AppTopBar(title = "Manifesto", onBackClick = onBackClick) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(Spacing.screen),
            verticalArrangement = Arrangement.spacedBy(Spacing.item)
        ) {
            item { PartyHeaderCard(party) }

            if (party.leaderName.isNotBlank()) {
                item { LeaderCard(party) }
            }

            item {
                Text(
                    text = "What they stand for",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .padding(top = Spacing.small)
                        .semantics { heading() } // screen readers can jump here
                )
            }

            if (party.manifestoPoints.isEmpty()) {
                item {
                    Text(
                        text = "This party's manifesto has not been added yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MutedText
                    )
                }
            } else {
                itemsIndexed(party.manifestoPoints) { index, point ->
                    ManifestoPoint(number = index + 1, text = point)
                }
            }
        }
    }
}

@Composable
private fun PartyHeaderCard(party: Party) {
    OutlinedSurfaceCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.card),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.small)
        ) {
            Image(
                painter = painterResource(party.logo),
                contentDescription = "${party.name} logo",
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(96.dp)
            )
            Text(
                text = party.name,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
            Text(
                text = party.slogan,
                style = MaterialTheme.typography.bodyMedium,
                color = MutedText,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun LeaderCard(party: Party) {
    OutlinedSurfaceCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.card),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.card)
        ) {
            LeaderAvatar(
                name = party.leaderName,
                photo = party.leaderPhoto,
                size = 64.dp
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Party leader",
                    style = MaterialTheme.typography.labelMedium,
                    color = MutedText
                )
                Text(
                    text = party.leaderName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun ManifestoPoint(number: Int, text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(Spacing.item)
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(InfoContainer, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number.toString(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .weight(1f)
                .padding(top = 3.dp)
        )
    }
}

@Composable
private fun OutlinedSurfaceCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        content()
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 760)
@Composable
private fun PartyDetailScreenPreview() {
    FrontendTheme {
        PartyDetailScreen(party = SampleParties.first(), onBackClick = {})
    }
}

@Preview(name = "No manifesto yet", showBackground = true, widthDp = 360, heightDp = 760)
@Composable
private fun PartyDetailEmptyPreview() {
    FrontendTheme {
        PartyDetailScreen(party = SampleParties.last(), onBackClick = {})
    }
}
