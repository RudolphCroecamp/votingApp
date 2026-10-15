package com.example.frontend.ui.screens.Status

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.frontend.R
import com.example.frontend.ui.Components.AppTopBar
import com.example.frontend.ui.Components.ButtonVariant
import com.example.frontend.ui.Components.DescriptionLabel
import com.example.frontend.ui.Components.HeadingLabel
import com.example.frontend.ui.Components.ReceiptCodeBox
import com.example.frontend.ui.Components.SectionHeading
import com.example.frontend.ui.Components.SimpleButton
import com.example.frontend.ui.Components.StatusLabel
import com.example.frontend.ui.Components.StatusType
import com.example.frontend.ui.Components.SuccessCheckmarkBadge
import com.example.frontend.ui.theme.CardBorder
import com.example.frontend.ui.theme.FrontendTheme
import com.example.frontend.ui.theme.MutedText
import com.example.frontend.ui.theme.PageBackground
import com.example.frontend.ui.theme.Spacing
import com.example.frontend.util.setPlainText
import kotlinx.coroutines.launch

@Composable
fun AlreadyVotedScreen(
    onViewReceipt: () -> Unit,
    onVerifyReceipt: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    receiptCode: String? = null,
    castAt: String? = null,
    ballotTitle: String? = null,
    backgroundColor: Color = PageBackground
) {
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    val clipLabel = stringResource(R.string.already_voted_clip_label)

    Scaffold(
        modifier = modifier,
        containerColor = backgroundColor,
        topBar = { AppTopBar(title = stringResource(R.string.already_voted_title), onBackClick = onBackClick) }
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
            SuccessCheckmarkBadge()

            SectionHeading(
                title = stringResource(R.string.already_voted_heading),
                description = stringResource(R.string.already_voted_description),
                textAlign = TextAlign.Center
            )

            if (castAt != null || ballotTitle != null) {
                CastDetailsCard(
                    ballotTitle = ballotTitle,
                    castAt = castAt,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (receiptCode != null) {
                ReceiptCodeBox(
                    receiptCode = receiptCode,
                    onCopyClick = {
                        scope.launch { clipboard.setPlainText(clipLabel, receiptCode) }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            StatusLabel(
                message = stringResource(R.string.already_voted_receipt_info),
                type = StatusType.Info,
                modifier = Modifier.fillMaxWidth()
            )

            SimpleButton(
                label = stringResource(R.string.already_voted_view_receipt),
                onClick = onViewReceipt,
                modifier = Modifier.fillMaxWidth(),
                variant = ButtonVariant.Tertiary,
                leadingIcon = Icons.Default.Receipt,
                trailingIcon = Icons.AutoMirrored.Filled.ArrowForward
            )

            SimpleButton(
                label = stringResource(R.string.verify_button),
                onClick = onVerifyReceipt,
                modifier = Modifier.fillMaxWidth(),
                variant = ButtonVariant.Secondary,
                leadingIcon = Icons.AutoMirrored.Filled.FactCheck
            )

            DescriptionLabel(
                text = stringResource(R.string.already_voted_not_you),
                modifier = Modifier.fillMaxWidth(),
                color = MutedText,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun CastDetailsCard(
    ballotTitle: String?,
    castAt: String?,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.card, vertical = Spacing.card),
            verticalArrangement = Arrangement.spacedBy(Spacing.small),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (ballotTitle != null) {
                HeadingLabel(
                    text = ballotTitle,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            if (castAt != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.small)
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = MutedText,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = stringResource(R.string.already_voted_cast_at, castAt),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MutedText
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AlreadyVotedScreenPreview() {
    FrontendTheme {
        AlreadyVotedScreen(
            onViewReceipt = {},
            onVerifyReceipt = {},
            onBackClick = {},
            receiptCode = "AB7F-92K4-6ZP1-3QX8",
            castAt = "27 May 2026 at 09:42",
            ballotTitle = "National Assembly"
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AlreadyVotedScreenNoReceiptPreview() {
    FrontendTheme {
        AlreadyVotedScreen(
            onViewReceipt = {},
            onVerifyReceipt = {},
            onBackClick = {}
        )
    }
}
