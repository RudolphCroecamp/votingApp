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
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.example.frontend.ui.theme.ErrorBorder
import com.example.frontend.ui.theme.ErrorContainer
import com.example.frontend.ui.theme.ErrorContent
import com.example.frontend.ui.theme.FrontendTheme
import com.example.frontend.ui.theme.MutedText
import com.example.frontend.ui.theme.PageBackground
import com.example.frontend.ui.theme.Spacing
import com.example.frontend.ui.theme.SuccessBorder
import com.example.frontend.ui.theme.SuccessContainer
import com.example.frontend.ui.theme.SuccessContent
import com.example.frontend.ui.theme.SurfaceSunken
import com.example.frontend.ui.theme.WarningBorder
import com.example.frontend.ui.theme.WarningContainer
import com.example.frontend.ui.theme.WarningContent

enum class VoteSubmissionOutcome {
    NOT_RECORDED,
    RECORDED,
    UNKNOWN
}

@Composable
fun ServerErrorScreen(
    onRetry: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    outcome: VoteSubmissionOutcome? = null,
    onVerifyReceipt: (() -> Unit)? = null,
    isRetrying: Boolean = false,
    errorReference: String? = null,
    backgroundColor: Color = PageBackground
) {
    Scaffold(
        modifier = modifier,
        containerColor = backgroundColor,
        topBar = { AppTopBar(title = "Something went wrong", onBackClick = onBackClick) }
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
                    imageVector = Icons.Default.CloudOff,
                    contentDescription = null,
                    tint = MutedText,
                    modifier = Modifier.size(44.dp)
                )
            }

            SectionHeading(
                title = "The service is not responding",
                description = "This is a problem on our side, not yours. Your phone and your " +
                    "connection are fine.",
                textAlign = TextAlign.Center
            )

            if (outcome != null) {
                VoteOutcomeCard(outcome = outcome, modifier = Modifier.fillMaxWidth())
            }

            SimpleButton(
                label = retryLabel(outcome, isRetrying),
                onClick = onRetry,
                modifier = Modifier.fillMaxWidth(),
                variant = ButtonVariant.Tertiary,
                leadingIcon = Icons.Default.Refresh,
                enabled = !isRetrying
            )

            if (isRetrying) {
                IndeterminateLinearIndicator(label = "Trying the Electoral Commission again…")
            }

            if (onVerifyReceipt != null && outcome != VoteSubmissionOutcome.NOT_RECORDED) {
                SimpleButton(
                    label = "Check with my receipt",
                    onClick = onVerifyReceipt,
                    modifier = Modifier.fillMaxWidth(),
                    variant = ButtonVariant.Secondary,
                    leadingIcon = Icons.AutoMirrored.Filled.FactCheck,
                    enabled = !isRetrying
                )
            }

            if (errorReference != null) {
                DescriptionLabel(
                    text = "Reference $errorReference — quote this if you call the helpline.",
                    modifier = Modifier.fillMaxWidth(),
                    color = MutedText,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
            }

            DescriptionLabel(
                text = "If this keeps happening, wait a few minutes and try again, or vote at " +
                    "your voting station.",
                modifier = Modifier.fillMaxWidth(),
                color = MutedText,
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun retryLabel(outcome: VoteSubmissionOutcome?, isRetrying: Boolean): String = when {
    isRetrying -> "Trying again…"
    outcome == VoteSubmissionOutcome.RECORDED -> "Reload"
    else -> "Try again"
}

@Composable
private fun VoteOutcomeCard(outcome: VoteSubmissionOutcome, modifier: Modifier = Modifier) {
    val (container, border, content) = when (outcome) {
        VoteSubmissionOutcome.NOT_RECORDED -> Triple(ErrorContainer, ErrorBorder, ErrorContent)
        VoteSubmissionOutcome.RECORDED -> Triple(SuccessContainer, SuccessBorder, SuccessContent)
        VoteSubmissionOutcome.UNKNOWN -> Triple(WarningContainer, WarningBorder, WarningContent)
    }
    val icon: ImageVector = when (outcome) {
        VoteSubmissionOutcome.NOT_RECORDED -> Icons.Default.ErrorOutline
        VoteSubmissionOutcome.RECORDED -> Icons.Default.CheckCircle
        VoteSubmissionOutcome.UNKNOWN -> Icons.AutoMirrored.Filled.HelpOutline
    }
    val title = when (outcome) {
        VoteSubmissionOutcome.NOT_RECORDED -> "Your vote was NOT recorded"
        VoteSubmissionOutcome.RECORDED -> "Your vote WAS recorded"
        VoteSubmissionOutcome.UNKNOWN -> "We cannot tell yet if your vote went through"
    }
    val body = when (outcome) {
        VoteSubmissionOutcome.NOT_RECORDED ->
            "The ballot was never accepted, so nothing was counted. You still have your vote, " +
                "and you can try again."
        VoteSubmissionOutcome.RECORDED ->
            "Your ballot was accepted and counted. The error happened after that, while we were " +
                "loading your receipt. Do not vote again."
        VoteSubmissionOutcome.UNKNOWN ->
            "The connection dropped while your ballot was being sent. Do not assume it failed. " +
                "Check your receipt before you try to vote again, so you do not vote twice."
    }

    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        color = container,
        border = BorderStroke(1.dp, border)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.card),
            horizontalArrangement = Arrangement.spacedBy(Spacing.item),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = content,
                modifier = Modifier.size(24.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.tight)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = content
                )
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = content
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ServerErrorScreenNotRecordedPreview() {
    FrontendTheme {
        ServerErrorScreen(
            onRetry = {},
            onBackClick = {},
            outcome = VoteSubmissionOutcome.NOT_RECORDED,
            errorReference = "EC-5XX-4471"
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ServerErrorScreenUnknownPreview() {
    FrontendTheme {
        ServerErrorScreen(
            onRetry = {},
            onBackClick = {},
            outcome = VoteSubmissionOutcome.UNKNOWN,
            onVerifyReceipt = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ServerErrorScreenRecordedPreview() {
    FrontendTheme {
        ServerErrorScreen(
            onRetry = {},
            onBackClick = {},
            outcome = VoteSubmissionOutcome.RECORDED,
            onVerifyReceipt = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ServerErrorScreenNoVotePreview() {
    FrontendTheme {
        ServerErrorScreen(onRetry = {}, onBackClick = {}, isRetrying = true)
    }
}
