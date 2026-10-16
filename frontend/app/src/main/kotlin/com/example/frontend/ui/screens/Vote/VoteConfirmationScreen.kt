package com.example.frontend.ui.screens.Vote

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.example.frontend.R
import com.example.frontend.effects.entrance
import com.example.frontend.effects.rememberEntranceTrigger
import com.example.frontend.ui.components.AppBottomBar
import com.example.frontend.ui.components.BottomTab
import com.example.frontend.ui.components.ButtonVariant
import com.example.frontend.ui.components.PrivacyNote
import com.example.frontend.ui.components.ReceiptCodeBox
import com.example.frontend.ui.components.SectionHeading
import com.example.frontend.ui.components.SimpleButton
import com.example.frontend.ui.components.SuccessCheckmarkBadge
import com.example.frontend.ui.theme.FrontendTheme
import com.example.frontend.ui.theme.PageBackground
import com.example.frontend.ui.theme.Spacing
import com.example.frontend.util.setPlainText
import kotlinx.coroutines.launch

@Composable
fun VoteConfirmationScreen(
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
    receipts: List<BallotReceipt> = SampleReceipts,
    onViewReceipt: () -> Unit = {},
    onPrivacy: () -> Unit = {},
    selectedTab: BottomTab = BottomTab.Vote,
    onTabSelected: (BottomTab) -> Unit = {},
    backgroundColor: Color = PageBackground
) {
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    val entered by rememberEntranceTrigger()

    Scaffold(
        modifier = modifier,
        containerColor = backgroundColor,
        bottomBar = { AppBottomBar(selected = selectedTab, onSelect = onTabSelected) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.screen, vertical = Spacing.card),
            verticalArrangement = Arrangement.spacedBy(Spacing.item),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SuccessCheckmarkBadge()

            Spacer(modifier = Modifier.height(Spacing.tight))

            SectionHeading(
                title = stringResource(R.string.confirmation_title),
                description = if (receipts.size > 1) {
                    stringResource(R.string.confirmation_all_recorded, receipts.size)
                } else {
                    stringResource(R.string.confirmation_one_recorded)
                },
                textAlign = TextAlign.Center,
                modifier = Modifier.entrance(entered, index = 1)
            )

            Spacer(modifier = Modifier.height(Spacing.tight))

            // One receipt per ballot (KAN-18).
            receipts.forEach { receipt ->
                val shortLabel = stringResource(receipt.ballot.shortLabelRes)
                val clipLabel = stringResource(R.string.confirmation_clip_label, shortLabel)
                ReceiptCodeBox(
                    receiptCode = receipt.code,
                    label = stringResource(R.string.confirmation_receipt_label, shortLabel),
                    onCopyClick = {
                        scope.launch {
                            clipboard.setPlainText(clipLabel, receipt.code)
                        }
                    },
                    modifier = Modifier.entrance(entered, index = 2)
                )
            }

            PrivacyNote(
                onLinkClick = onPrivacy,
                modifier = Modifier
                    .fillMaxWidth()
                    .entrance(entered, index = 3),
                message = stringResource(R.string.confirmation_keep_receipts)
            )

            Spacer(modifier = Modifier.height(Spacing.small))

            TextButton(onClick = onViewReceipt) {
                Text(
                    text = stringResource(
                        if (receipts.size > 1) R.string.confirmation_view_receipts else R.string.confirmation_view_receipt
                    ),
                    style = MaterialTheme.typography.labelLarge
                )
            }
            SimpleButton(
                label = stringResource(R.string.action_done),
                onClick = onDone,
                modifier = Modifier.fillMaxWidth(),
                variant = ButtonVariant.Tertiary
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun VoteConfirmationScreenPreview() {
    FrontendTheme {
        VoteConfirmationScreen(onDone = {})
    }
}
