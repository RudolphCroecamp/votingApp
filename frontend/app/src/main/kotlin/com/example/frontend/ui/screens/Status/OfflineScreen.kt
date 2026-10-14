package com.example.frontend.ui.screens.Status

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SignalWifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.frontend.ui.Components.AppTopBar
import com.example.frontend.ui.Components.ButtonVariant
import com.example.frontend.ui.Components.DescriptionLabel
import com.example.frontend.ui.Components.IndeterminateLinearIndicator
import com.example.frontend.ui.Components.SectionHeading
import com.example.frontend.ui.Components.SimpleButton
import com.example.frontend.ui.Components.StatusLabel
import com.example.frontend.ui.Components.StatusType
import com.example.frontend.ui.theme.ErrorBorder
import com.example.frontend.ui.theme.ErrorContainer
import com.example.frontend.ui.theme.ErrorContent
import com.example.frontend.ui.theme.FrontendTheme
import com.example.frontend.ui.theme.MutedText
import com.example.frontend.ui.theme.PageBackground
import com.example.frontend.ui.theme.Spacing
import com.example.frontend.ui.theme.SurfaceSunken

@Composable
fun OfflineScreen(
    onRetry: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    wasCastingVote: Boolean = true,
    isRetrying: Boolean = false,
    backgroundColor: Color = PageBackground
) {
    Scaffold(
        modifier = modifier,
        containerColor = backgroundColor,
        topBar = { AppTopBar(title = "No internet connection", onBackClick = onBackClick) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.screen, vertical = Spacing.section),
            verticalArrangement = Arrangement.spacedBy(Spacing.item),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .background(SurfaceSunken, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SignalWifiOff,
                    contentDescription = null,
                    tint = MutedText,
                    modifier = Modifier.size(44.dp)
                )
            }

            SectionHeading(
                title = "You are offline",
                description = "We could not reach the Electoral Commission. Check your Wi-Fi or " +
                    "mobile data and try again.",
                textAlign = TextAlign.Center
            )

            if (wasCastingVote) {
                NoVoteRecordedCard(modifier = Modifier.fillMaxWidth())
            }

            SimpleButton(
                label = if (isRetrying) "Trying again…" else "Try again",
                onClick = onRetry,
                modifier = Modifier.fillMaxWidth(),
                variant = ButtonVariant.Tertiary,
                leadingIcon = Icons.Default.Refresh,
                enabled = !isRetrying
            )

            if (isRetrying) {
                IndeterminateLinearIndicator(label = "Looking for a connection…")
            }

            StatusLabel(
                message = if (wasCastingVote) {
                    "When you are back online, open the ballot again and make your mark. " +
                        "Nothing you picked was sent."
                } else {
                    "You can keep reading the voting information offline. Voting needs a connection."
                },
                type = StatusType.Info,
                modifier = Modifier.fillMaxWidth()
            )

            DescriptionLabel(
                text = "Still stuck? Move somewhere with better signal, turn flight mode off, " +
                    "or try again on Wi-Fi.",
                modifier = Modifier.fillMaxWidth(),
                color = MutedText,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun NoVoteRecordedCard(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        color = ErrorContainer,
        border = BorderStroke(1.dp, ErrorBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.card),
            horizontalArrangement = Arrangement.spacedBy(Spacing.item),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.SignalWifiOff,
                contentDescription = null,
                tint = ErrorContent,
                modifier = Modifier.size(24.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.tight)) {
                Text(
                    text = "Your vote was NOT recorded",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ErrorContent
                )
                Text(
                    text = "The ballot never reached us, so nothing was counted and nothing was " +
                        "used up. You still have your vote.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ErrorContent
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OfflineScreenCastingVotePreview() {
    FrontendTheme {
        OfflineScreen(onRetry = {}, onBackClick = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun OfflineScreenRetryingPreview() {
    FrontendTheme {
        OfflineScreen(onRetry = {}, onBackClick = {}, isRetrying = true)
    }
}

@Preview(showBackground = true)
@Composable
private fun OfflineScreenBrowsingPreview() {
    FrontendTheme {
        OfflineScreen(onRetry = {}, onBackClick = {}, wasCastingVote = false)
    }
}
