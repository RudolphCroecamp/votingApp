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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.frontend.R
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
                title = stringResource(R.string.info_title),
                subtitle = stringResource(R.string.info_subtitle),
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
                title = stringResource(R.string.info_heading),
                description = stringResource(R.string.info_description)
            )

            Column(verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
                ActionRow(
                    icon = Icons.Default.HowToVote,
                    title = stringResource(R.string.home_action_info_title),
                    description = stringResource(R.string.info_rights_description),
                    onClick = onVotingInformation,
                    modifier = Modifier.entrance(entered, index = 1),
                    iconTint = WarningContent,
                    iconBackground = WarningContainer
                )
                ActionRow(
                    icon = Icons.Default.LocationOn,
                    title = stringResource(R.string.home_action_station_title),
                    description = stringResource(R.string.home_action_station_description),
                    onClick = onVotingStation,
                    modifier = Modifier.entrance(entered, index = 2)
                )
                ActionRow(
                    icon = Icons.Default.Shield,
                    title = stringResource(R.string.info_privacy_title),
                    description = stringResource(R.string.info_privacy_description),
                    onClick = onPrivacy,
                    modifier = Modifier.entrance(entered, index = 3),
                    iconTint = SuccessContent,
                    iconBackground = SuccessContainer
                )
                ActionRow(
                    icon = Icons.AutoMirrored.Filled.HelpOutline,
                    title = stringResource(R.string.info_help_title),
                    description = stringResource(R.string.info_help_description),
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
