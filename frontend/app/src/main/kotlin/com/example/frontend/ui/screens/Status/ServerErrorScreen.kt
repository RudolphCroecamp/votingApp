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
        topBar = { AppTopBar(title = stringResource(R.string.server_error_title), onBackClick = onBackClick) }
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
                title = stringResource(R.string.server_error_heading),
                description = stringResource(R.string.server_error_description),
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
                IndeterminateLinearIndicator(label = stringResource(R.string.server_error_retrying))
            }

            if (onVerifyReceipt != null && outcome != VoteSubmissionOutcome.NOT_RECORDED) {
                SimpleButton(
                    label = stringResource(R.string.server_error_check_receipt),
                    onClick = onVerifyReceipt,
                    modifier = Modifier.fillMaxWidth(),
                    variant = ButtonVariant.Secondary,
                    leadingIcon = Icons.AutoMirrored.Filled.FactCheck,
                    enabled = !isRetrying
                )
            }

            if (errorReference != null) {
                DescriptionLabel(
                    text = stringResource(R.string.server_error_reference, errorReference),
                    modifier = Modifier.fillMaxWidth(),
                    color = MutedText,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
            }

            DescriptionLabel(
                text = stringResource(R.string.server_error_keeps_happening),
                modifier = Modifier.fillMaxWidth(),
                color = MutedText,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun retryLabel(outcome: VoteSubmissionOutcome?, isRetrying: Boolean): String = when {
    isRetrying -> stringResource(R.string.status_trying_again)
    outcome == VoteSubmissionOutcome.RECORDED -> stringResource(R.string.action_reload)
    else -> stringResource(R.string.action_try_again)
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
    val title = stringResource(
        when (outcome) {
            VoteSubmissionOutcome.NOT_RECORDED -> R.string.vote_not_recorded
            VoteSubmissionOutcome.RECORDED -> R.string.vote_was_recorded
            VoteSubmissionOutcome.UNKNOWN -> R.string.vote_outcome_unknown
        }
    )
    val body = stringResource(
        when (outcome) {
            VoteSubmissionOutcome.NOT_RECORDED -> R.string.server_error_not_recorded_detail
            VoteSubmissionOutcome.RECORDED -> R.string.server_error_recorded_detail
            VoteSubmissionOutcome.UNKNOWN -> R.string.server_error_unknown_detail
        }
    )

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
