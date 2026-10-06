package com.example.frontend.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.HowToVote
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
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
import androidx.compose.ui.unit.dp
import com.example.frontend.ui.Components.AppBottomBar
import com.example.frontend.ui.Components.BottomTab
import com.example.frontend.ui.Components.InfoTile
import com.example.frontend.ui.Components.VoteNowCard
import com.example.frontend.ui.Components.WelcomeHeader
import com.example.frontend.ui.theme.FrontendTheme
import com.example.frontend.ui.theme.Spacing
import com.example.frontend.ui.theme.PageBackground

@Composable
fun HomeScreen(
    onVoteNow: () -> Unit,
    modifier: Modifier = Modifier,
    voterName: String = "Voter",
    electionName: String = "2026 National Election",
    electionStatus: String = "Voting is open",
    selectedTab: BottomTab = BottomTab.Home,
    onTabSelected: (BottomTab) -> Unit = {},
    onMenuClick: () -> Unit = {},
    onVotingInformation: () -> Unit = {},
    onPrivacy: () -> Unit = {},
    onHelp: () -> Unit = {},
    onSettings: () -> Unit = {},
    onVerifyVote: () -> Unit = {},
    onLogout: () -> Unit = {},
    backgroundColor: Color = PageBackground
) {
    var menuOpen by remember { mutableStateOf(false) }
    Scaffold(
        modifier = modifier,
        containerColor = backgroundColor,
        topBar = {
            WelcomeHeader(
                title = "Welcome, $voterName",
                status = "You are verified and eligible to vote.",
                onMenuClick = {
                    menuOpen = true
                    onMenuClick()
                },
                menuContent = {
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
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
                .padding(Spacing.screen),
            verticalArrangement = Arrangement.spacedBy(Spacing.item)
        ) {
            VoteNowCard(
                title = electionName,
                status = electionStatus,
                onClick = onVoteNow,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(Spacing.item)
            ) {
                InfoTile(
                    icon = Icons.Default.HowToVote,
                    title = "Voting\nInformation",
                    description = "Learn about the election and your rights.",
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    onClick = onVotingInformation
                )
                InfoTile(
                    icon = Icons.Default.Shield,
                    title = "Your Privacy",
                    description = "How we keep your vote safe and anonymous.",
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    onClick = onPrivacy
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(Spacing.item)
            ) {
                InfoTile(
                    icon = Icons.AutoMirrored.Filled.HelpOutline,
                    title = "Help & Support",
                    description = "Get assistance and contact details.",
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    onClick = onHelp
                )
                InfoTile(
                    icon = Icons.Default.Search,
                    title = "Verify\nYour Vote",
                    description = "Check your receipt code to confirm your vote was recorded.",
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    onClick = onVerifyVote
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    FrontendTheme {
        HomeScreen(onVoteNow = {})
    }
}
