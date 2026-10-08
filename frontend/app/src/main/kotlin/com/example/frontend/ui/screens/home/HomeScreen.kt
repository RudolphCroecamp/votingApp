package com.example.frontend.ui.screens.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.LocationOn
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
import androidx.compose.ui.tooling.preview.Preview
import com.example.frontend.effects.entrance
import com.example.frontend.effects.rememberEntranceTrigger
import com.example.frontend.ui.Components.ActionRow
import com.example.frontend.ui.Components.AppBottomBar
import com.example.frontend.ui.Components.BottomTab
import com.example.frontend.ui.Components.HeroCard
import com.example.frontend.ui.Components.HeroState
import com.example.frontend.ui.Components.HomeHeader
import com.example.frontend.ui.Components.PrivacyNote
import com.example.frontend.ui.theme.ErrorContainer
import com.example.frontend.ui.theme.ErrorContent
import com.example.frontend.ui.theme.FrontendTheme
import com.example.frontend.ui.theme.MotionDurations
import com.example.frontend.ui.theme.PageBackground
import com.example.frontend.ui.theme.Spacing
import com.example.frontend.ui.theme.StandardEasing
import com.example.frontend.ui.theme.motionDuration
import com.example.frontend.ui.theme.SuccessContainer
import com.example.frontend.ui.theme.SuccessContent
import com.example.frontend.ui.theme.WarningContainer
import com.example.frontend.ui.theme.WarningContent

@Composable
fun HomeScreen(
    onVoteNow: () -> Unit,
    modifier: Modifier = Modifier,
    voterName: String = "Voter",
    electionName: String = "2026 Local Government Election",
    heroState: HeroState = HeroState.VotingOpen,
    closingTime: String = "Closes today at 21:00. Make your mark!",
    openingTime: String = "Voting opens in 2 days · 07:00. Get ready!",
    receiptSummary: String = "Recorded at 10:42 · Receipt ID 7F3A-92KD",
    selectedTab: BottomTab = BottomTab.Home,
    onTabSelected: (BottomTab) -> Unit = {},
    onMenuClick: () -> Unit = {},
    onVotingStation: () -> Unit = {},
    onVotingInformation: () -> Unit = {},
    onCheckRegistration: () -> Unit = {},
    onReportProblem: () -> Unit = {},
    onPrivacy: () -> Unit = {},
    onHelp: () -> Unit = {},
    onLanguage: () -> Unit = {},
    onSettings: () -> Unit = {},
    onVerifyVote: () -> Unit = {},
    onRemindMe: () -> Unit = {},
    onLogout: () -> Unit = {},
    backgroundColor: Color = PageBackground
) {
    var menuOpen by remember { mutableStateOf(false) }
    val entered by rememberEntranceTrigger()
    val heroSwapMillis = motionDuration(MotionDurations.medium)
    Scaffold(
        modifier = modifier,
        containerColor = backgroundColor,
        topBar = {
            HomeHeader(
                greeting = "Hi, $voterName",
                onLanguageClick = onLanguage,
                onHelpClick = {
                    menuOpen = true
                    onMenuClick()
                },
                trailingContent = {
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        DropdownMenuItem(
                            text = { Text("Help & Support") },
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
                            text = { Text("Settings") },
                            onClick = {
                                menuOpen = false
                                onSettings()
                            },
                            leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Verify Your Vote") },
                            onClick = {
                                menuOpen = false
                                onVerifyVote()
                            },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("Log Out") },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.screen, vertical = Spacing.card),
            verticalArrangement = Arrangement.spacedBy(Spacing.item)
        ) {
            // A plain cross-fade between hero states: no shared-element work,
            // no size animation, so the rows below never shift.
            AnimatedContent(
                targetState = heroState,
                modifier = Modifier
                    .fillMaxWidth()
                    .entrance(entered, index = 0),
                transitionSpec = {
                    val spec = tween<Float>(heroSwapMillis, easing = StandardEasing)
                    fadeIn(spec) togetherWith fadeOut(spec)
                },
                label = "heroState"
            ) { state ->
                when (state) {
                    HeroState.VotingOpen -> HeroCard(
                        state = HeroState.VotingOpen,
                        title = electionName,
                        subtitle = closingTime,
                        actionLabel = "Vote now",
                        onAction = onVoteNow,
                        modifier = Modifier.fillMaxWidth()
                    )

                    HeroState.VoteCast -> HeroCard(
                        state = HeroState.VoteCast,
                        title = "You made your mark, $voterName!",
                        subtitle = receiptSummary,
                        actionLabel = "View vote receipt",
                        onAction = onVerifyVote,
                        footnote = "Your receipt proves you voted. It never shows who for.",
                        modifier = Modifier.fillMaxWidth()
                    )

                    HeroState.ComingUp -> HeroCard(
                        state = HeroState.ComingUp,
                        title = electionName,
                        subtitle = openingTime,
                        actionLabel = "Remind me",
                        onAction = onRemindMe,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            HomeActions(
                entered = entered,
                onVotingStation = onVotingStation,
                onVotingInformation = onVotingInformation,
                onCheckRegistration = onCheckRegistration,
                onReportProblem = onReportProblem
            )

            PrivacyNote(
                onLinkClick = onPrivacy,
                modifier = Modifier
                    .fillMaxWidth()
                    .entrance(entered, index = 5)
            )
        }
    }
}

/** The secondary action list. Fixed for every hero state; only the hero changes. */
@Composable
private fun HomeActions(
    entered: Boolean,
    onVotingStation: () -> Unit,
    onVotingInformation: () -> Unit,
    onCheckRegistration: () -> Unit,
    onReportProblem: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.small)
    ) {
        ActionRow(
            icon = Icons.Default.LocationOn,
            title = "Find my voting station",
            description = "Address, hours and directions",
            onClick = onVotingStation,
            modifier = Modifier.entrance(entered, index = 1)
        )
        ActionRow(
            icon = Icons.AutoMirrored.Filled.MenuBook,
            title = "Election info and your rights",
            description = "How voting works, step by step",
            onClick = onVotingInformation,
            modifier = Modifier.entrance(entered, index = 2),
            iconTint = WarningContent,
            iconBackground = WarningContainer
        )
        ActionRow(
            icon = Icons.Default.AssignmentTurnedIn,
            title = "Check my registration",
            description = "Confirm your details are correct",
            onClick = onCheckRegistration,
            modifier = Modifier.entrance(entered, index = 3),
            iconTint = SuccessContent,
            iconBackground = SuccessContainer
        )
        ActionRow(
            icon = Icons.Default.ReportProblem,
            title = "Report a problem",
            description = "Something wrong with your vote?",
            onClick = onReportProblem,
            modifier = Modifier.entrance(entered, index = 4),
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
