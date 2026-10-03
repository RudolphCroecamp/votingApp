package com.example.frontend.ui.Info

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
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.frontend.ui.Components.AppTopBar
import com.example.frontend.ui.Components.InfoTile
import com.example.frontend.ui.theme.FrontendTheme
import com.example.frontend.ui.theme.Spacing
import com.example.frontend.ui.theme.PageBackground

@Composable
fun InfoScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    onVotingInformation: () -> Unit = {},
    onPrivacy: () -> Unit = {},
    onHelp: () -> Unit = {},
    backgroundColor: Color = PageBackground
) {
    Scaffold(
        modifier = modifier,
        containerColor = backgroundColor,
        topBar = { AppTopBar(title = "Information", onBackClick = onBackClick) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(Spacing.screen),
            verticalArrangement = Arrangement.spacedBy(Spacing.item)
        ) {
            InfoTile(
                icon = Icons.Default.HowToVote,
                title = "Voting Information",
                description = "Learn about the election, the ballots you may receive and your rights as a voter.",
                modifier = Modifier.fillMaxWidth(),
                onClick = onVotingInformation
            )
            InfoTile(
                icon = Icons.Default.Shield,
                title = "Your Privacy",
                description = "How your vote is kept safe, anonymous and separate from your identity.",
                modifier = Modifier.fillMaxWidth(),
                onClick = onPrivacy
            )
            InfoTile(
                icon = Icons.AutoMirrored.Filled.HelpOutline,
                title = "Help & Support",
                description = "Get assistance with signing in, voting and verifying your receipt.",
                modifier = Modifier.fillMaxWidth(),
                onClick = onHelp
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
