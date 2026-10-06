package com.example.frontend.ui.Vote

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.frontend.data.Party
import com.example.frontend.data.SampleParties
import com.example.frontend.ui.Components.AppBottomBar
import com.example.frontend.ui.Components.BottomTab
import com.example.frontend.ui.Components.AppTopBar
import com.example.frontend.ui.Components.ButtonVariant
import com.example.frontend.ui.Components.HeadingLabel
import com.example.frontend.ui.Components.PartyCard
import com.example.frontend.ui.Components.SimpleButton
import com.example.frontend.ui.Components.StatusLabel
import com.example.frontend.ui.Components.StatusType
import com.example.frontend.ui.theme.FrontendTheme
import com.example.frontend.ui.theme.Spacing
import com.example.frontend.ui.theme.PageBackground

@Composable
fun VoteReviewScreen(
    party: Party,
    onChangeSelection: () -> Unit,
    onConfirmVote: () -> Unit,
    modifier: Modifier = Modifier,
    ballotTitle: String = "National Assembly",
    selectedTab: BottomTab = BottomTab.Vote,
    onTabSelected: (BottomTab) -> Unit = {},
    backgroundColor: Color = PageBackground
) {
    Scaffold(
        modifier = modifier,
        containerColor = backgroundColor,
        topBar = { AppTopBar(title = "Review Your Vote", onBackClick = onChangeSelection) },
        bottomBar = { AppBottomBar(selected = selectedTab, onSelect = onTabSelected) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(Spacing.screen),
            verticalArrangement = Arrangement.spacedBy(Spacing.item),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(Spacing.tight)
            ) {
                HeadingLabel(text = ballotTitle, modifier = Modifier.fillMaxWidth())
                HeadingLabel(
                    text = "Your selection:",
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.titleMedium
                )
            }

            PartyCard(
                title = party.name,
                slogan = party.slogan,
                logo = painterResource(party.logo),
                modifier = Modifier.fillMaxWidth()
            )

            StatusLabel(
                message = "Your vote cannot be changed after confirmation.",
                type = StatusType.Warning
            )

            Spacer(modifier = Modifier.height(Spacing.small))

            SimpleButton(
                label = "Change Selection",
                onClick = onChangeSelection,
                modifier = Modifier.fillMaxWidth(),
                variant = ButtonVariant.Secondary,
                leadingIcon = Icons.AutoMirrored.Filled.ArrowBack
            )

            SimpleButton(
                label = "Confirm Vote",
                onClick = onConfirmVote,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun VoteReviewScreenPreview() {
    FrontendTheme {
        VoteReviewScreen(
            party = SampleParties.first(),
            onChangeSelection = {},
            onConfirmVote = {}
        )
    }
}
