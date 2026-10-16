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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.frontend.R
import com.example.frontend.ui.components.AppTopBar
import com.example.frontend.ui.components.ButtonVariant
import com.example.frontend.ui.components.DescriptionLabel
import com.example.frontend.ui.components.IndeterminateLinearIndicator
import com.example.frontend.ui.components.SectionHeading
import com.example.frontend.ui.components.SimpleButton
import com.example.frontend.ui.components.StatusLabel
import com.example.frontend.ui.components.StatusType
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
        topBar = { AppTopBar(title = stringResource(R.string.offline_title), onBackClick = onBackClick) }
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
                title = stringResource(R.string.offline_heading),
                description = stringResource(R.string.offline_description),
                textAlign = TextAlign.Center
            )

            if (wasCastingVote) {
                NoVoteRecordedCard(modifier = Modifier.fillMaxWidth())
            }

            SimpleButton(
                label = stringResource(if (isRetrying) R.string.status_trying_again else R.string.action_try_again),
                onClick = onRetry,
                modifier = Modifier.fillMaxWidth(),
                variant = ButtonVariant.Tertiary,
                leadingIcon = Icons.Default.Refresh,
                enabled = !isRetrying
            )

            if (isRetrying) {
                IndeterminateLinearIndicator(label = stringResource(R.string.offline_looking))
            }

            StatusLabel(
                message = stringResource(
                    if (wasCastingVote) R.string.offline_hint_casting else R.string.offline_hint_reading
                ),
                type = StatusType.Info,
                modifier = Modifier.fillMaxWidth()
            )

            DescriptionLabel(
                text = stringResource(R.string.offline_still_stuck),
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
                    text = stringResource(R.string.vote_not_recorded),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ErrorContent
                )
                Text(
                    text = stringResource(R.string.offline_not_recorded_detail),
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
