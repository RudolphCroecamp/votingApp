package com.example.frontend.ui.screens.Info

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.HowToVote
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.example.frontend.effects.entrance
import com.example.frontend.effects.rememberEntranceTrigger
import com.example.frontend.ui.Components.ActionRow
import com.example.frontend.ui.Components.AppBottomBar
import com.example.frontend.ui.Components.AppTopBar
import com.example.frontend.ui.Components.BottomTab
import com.example.frontend.ui.Components.PrivacyNote
import com.example.frontend.ui.Components.SectionHeading
import com.example.frontend.ui.theme.FrontendTheme
import com.example.frontend.ui.theme.PageBackground
import com.example.frontend.ui.theme.Spacing
import com.example.frontend.ui.theme.SuccessContainer
import com.example.frontend.ui.theme.SuccessContent
import com.example.frontend.ui.theme.WarningContainer
import com.example.frontend.ui.theme.WarningContent

@Composable
fun InfoScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    onVotingInformation: () -> Unit = {},
    onVotingStation: () -> Unit = {},
    onPrivacy: () -> Unit = {},
    onHelp: () -> Unit = {},
    selectedTab: BottomTab = BottomTab.Info,
    onTabSelected: (BottomTab) -> Unit = {},
    backgroundColor: Color = PageBackground
) {
    val entered by rememberEntranceTrigger()

    Scaffold(
        modifier = modifier,
        containerColor = backgroundColor,
        topBar = {
            AppTopBar(
                title = "Information",
                subtitle = "Everything you need before you vote",
                onBackClick = onBackClick
            )
        },
        bottomBar = { AppBottomBar(selected = selectedTab, onSelect = onTabSelected) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.screen, vertical = Spacing.card),
            verticalArrangement = Arrangement.spacedBy(Spacing.item)
        ) {
            SectionHeading(
                modifier = Modifier.entrance(entered, index = 0),
                title = "How voting works",
                description = "Short guides covering the ballot, your rights and your privacy."
            )

            Column(verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
                ActionRow(
                    icon = Icons.Default.HowToVote,
                    title = "Election info and your rights",
                    description = "The election, your ballots and what you may expect",
                    onClick = onVotingInformation,
                    modifier = Modifier.entrance(entered, index = 1),
                    iconTint = WarningContent,
                    iconBackground = WarningContainer
                )
                ActionRow(
                    icon = Icons.Default.LocationOn,
                    title = "Find my voting station",
                    description = "Address, hours and directions",
                    onClick = onVotingStation,
                    modifier = Modifier.entrance(entered, index = 2)
                )
                ActionRow(
                    icon = Icons.Default.Shield,
                    title = "Your privacy",
                    description = "How your vote stays anonymous and separate from you",
                    onClick = onPrivacy,
                    modifier = Modifier.entrance(entered, index = 3),
                    iconTint = SuccessContent,
                    iconBackground = SuccessContainer
                )
                ActionRow(
                    icon = Icons.AutoMirrored.Filled.HelpOutline,
                    title = "Help and support",
                    description = "Signing in, voting and verifying your receipt",
                    onClick = onHelp,
                    modifier = Modifier.entrance(entered, index = 4)
                )
            }

            PrivacyNote(
                onLinkClick = onPrivacy,
                modifier = Modifier
                    .fillMaxWidth()
                    .entrance(entered, index = 5)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun InfoScreenPreview() {
    FrontendTheme {
        InfoScreen(onBackClick = {})
    }
}
