package com.example.frontend.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.HowToVote
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Poll
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.frontend.R
import com.example.frontend.ui.components.ActionRow
import com.example.frontend.ui.components.AppBottomBar
import com.example.frontend.ui.components.AppLanguagePickerDialog
import com.example.frontend.ui.components.BottomTab
import com.example.frontend.ui.components.HeroCard
import com.example.frontend.ui.components.HeroState
import com.example.frontend.ui.components.HomeHeader
import com.example.frontend.ui.components.PrivacyNote
import com.example.frontend.ui.components.currentAppLanguage
import com.example.frontend.ui.theme.ErrorContainer
import com.example.frontend.ui.theme.ErrorContent
import com.example.frontend.ui.theme.FrontendTheme
import com.example.frontend.ui.theme.InfoContainer
import com.example.frontend.ui.theme.InfoContent
import com.example.frontend.ui.theme.PageBackground
import com.example.frontend.ui.theme.Spacing
import com.example.frontend.ui.theme.SuccessContainer
import com.example.frontend.ui.theme.SuccessContent
import com.example.frontend.ui.theme.WarningContainer
import com.example.frontend.ui.theme.WarningContent

@Composable
fun HomeScreen(
    onVoteNow: () -> Unit,
    modifier: Modifier = Modifier,
    voterName: String = "",
    electionName: String = stringResource(R.string.home_sample_election),
    heroState: HeroState = HeroState.VotingOpen,
    closingTime: String = stringResource(R.string.home_sample_closing),
    openingTime: String = stringResource(R.string.home_sample_opening),
    comingUpFootnote: String? = null,
    receiptSummary: String = stringResource(R.string.home_sample_receipt_summary),
    selectedTab: BottomTab = BottomTab.Home,
    onTabSelected: (BottomTab) -> Unit = {},
    onMenuClick: () -> Unit = {},
    onVotingStation: () -> Unit = {},
    onVotingInformation: () -> Unit = {},
    onCheckRegistration: () -> Unit = {},
    onRequestVote: () -> Unit = {},
    onReportProblem: () -> Unit = {},
    onPrivacy: () -> Unit = {},
    onHelp: () -> Unit = {},
    onSettings: () -> Unit = {},
    onVerifyVote: () -> Unit = {},
    onViewReceipt: () -> Unit = onVerifyVote,
    onViewResults: () -> Unit = {},
    onRemindMe: () -> Unit = {},
    onLogout: () -> Unit = {},
    backgroundColor: Color = PageBackground
) {
    var menuOpen by remember { mutableStateOf(false) }
    var languagePickerOpen by remember { mutableStateOf(false) }
    val displayName = voterName.ifBlank { stringResource(R.string.voter_fallback_name) }
    Scaffold(
        modifier = modifier,
        containerColor = backgroundColor,
        topBar = {
            HomeHeader(
                greeting = stringResource(R.string.home_greeting, displayName),
                language = currentAppLanguage().shortCode,
                onLanguageClick = { languagePickerOpen = true },
                onHelpClick = {
                    menuOpen = true
                    onMenuClick()
                },
                trailingContent = {
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.menu_help_support)) },
                            onClick = {
                                menuOpen = false
                                onHelp()
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.AutoMirrored.Filled.HelpOutline,
                                    contentDescription = null
                                )
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.tab_settings)) },
                            onClick = {
                                menuOpen = false
                                onSettings()
                            },
                            leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.verify_title)) },
                            onClick = {
                                menuOpen = false
                                onVerifyVote()
                            },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Election Results") },
                            onClick = {
                                menuOpen = false
                                onViewResults()
                            },
                            leadingIcon = { Icon(Icons.Default.Poll, contentDescription = null) }
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.menu_log_out)) },
                            onClick = {
                                menuOpen = false
                                onLogout()
                            },
                            leadingIcon = {
                                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                            }
                        )
                    }
                }
            )
        },
        bottomBar = { AppBottomBar(selected = selectedTab, onSelect = onTabSelected) }
    ) { innerPadding ->
        if (languagePickerOpen) {
            AppLanguagePickerDialog(onDismiss = { languagePickerOpen = false })
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.screen, vertical = Spacing.card),
            verticalArrangement = Arrangement.spacedBy(Spacing.item)
        ) {
            // Static by design: Home, Vote and Manifestos switch instantly with no motion.
            Box(modifier = Modifier.fillMaxWidth()) {
                when (heroState) {
                    HeroState.VotingOpen -> HeroCard(
                        state = HeroState.VotingOpen,
                        title = electionName,
                        subtitle = closingTime,
                        actionLabel = stringResource(R.string.home_vote_now),
                        onAction = onVoteNow,
                        modifier = Modifier.fillMaxWidth()
                    )

                    HeroState.VoteCast -> HeroCard(
                        state = HeroState.VoteCast,
                        title = stringResource(R.string.home_made_your_mark, displayName),
                        subtitle = receiptSummary,
                        actionLabel = stringResource(R.string.home_view_receipt),
                        onAction = onViewReceipt,
                        footnote = stringResource(R.string.home_receipt_footnote),
                        modifier = Modifier.fillMaxWidth()
                    )

                    HeroState.ComingUp -> HeroCard(
                        state = HeroState.ComingUp,
                        title = electionName,
                        subtitle = openingTime,
                        actionLabel = stringResource(R.string.home_remind_me),
                        onAction = onRemindMe,
                        footnote = comingUpFootnote,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            HomeActions(
                onVotingStation = onVotingStation,
                onVotingInformation = onVotingInformation,
                onCheckRegistration = onCheckRegistration,
                onRequestVote = onRequestVote,
                onReportProblem = onReportProblem,
                onViewResults = onViewResults
            )

            PrivacyNote(
                onLinkClick = onPrivacy,
                modifier = Modifier
                    .fillMaxWidth()
            )
        }
    }
}

/** The secondary action list. Fixed for every hero state; only the hero changes. */
@Composable
private fun HomeActions(
    onVotingStation: () -> Unit,
    onVotingInformation: () -> Unit,
    onCheckRegistration: () -> Unit,
    onRequestVote: () -> Unit,
    onReportProblem: () -> Unit,
    onViewResults: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.small)
    ) {
        ActionRow(
            icon = Icons.Default.LocationOn,
            title = stringResource(R.string.home_action_station_title),
            description = stringResource(R.string.home_action_station_description),
            onClick = onVotingStation,
            modifier = Modifier
        )
        ActionRow(
            icon = Icons.AutoMirrored.Filled.MenuBook,
            title = stringResource(R.string.home_action_info_title),
            description = stringResource(R.string.home_action_info_description),
            onClick = onVotingInformation,
            modifier = Modifier,
            iconTint = WarningContent,
            iconBackground = WarningContainer
        )
        ActionRow(
            icon = Icons.Default.AssignmentTurnedIn,
            title = stringResource(R.string.home_action_registration_title),
            description = stringResource(R.string.home_action_registration_description),
            onClick = onCheckRegistration,
            modifier = Modifier,
            iconTint = SuccessContent,
            iconBackground = SuccessContainer
        )
        ActionRow(
            icon = Icons.Default.HowToVote,
            title = "Request to Vote",
            description = "Request access to vote in upcoming elections",
            onClick = onRequestVote,
            modifier = Modifier,
            iconTint = InfoContent,
            iconBackground = InfoContainer
        )
        ActionRow(
            icon = Icons.Default.Poll,
            title = "Election Results",
            description = "View official certified vote counts and percentages",
            onClick = onViewResults,
            modifier = Modifier,
            iconTint = InfoContent,
            iconBackground = InfoContainer
        )
        ActionRow(
            icon = Icons.Default.ReportProblem,
            title = stringResource(R.string.home_action_report_title),
            description = stringResource(R.string.home_action_report_description),
            onClick = onReportProblem,
            modifier = Modifier,
            iconTint = ErrorContent,
            iconBackground = ErrorContainer
        )
    }
}

@Preview(name = "Home - voting open", showBackground = true)
@Composable
private fun HomeScreenPreview() {
    FrontendTheme {
        HomeScreen(onVoteNow = {}, voterName = "Thandi")
    }
}

@Preview(name = "Home - vote cast", showBackground = true)
@Composable
private fun HomeScreenVoteCastPreview() {
    FrontendTheme {
        HomeScreen(onVoteNow = {}, voterName = "Thandi", heroState = HeroState.VoteCast)
    }
}

@Preview(name = "Home - coming up", showBackground = true)
@Composable
private fun HomeScreenComingUpPreview() {
    FrontendTheme {
        HomeScreen(onVoteNow = {}, voterName = "Thandi", heroState = HeroState.ComingUp)
    }
}
