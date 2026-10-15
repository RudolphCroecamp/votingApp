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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.example.frontend.effects.entrance
import com.example.frontend.effects.rememberEntranceTrigger
import com.example.frontend.ui.Components.AppBottomBar
import com.example.frontend.ui.Components.BottomTab
import com.example.frontend.ui.Components.ButtonVariant
import com.example.frontend.ui.Components.PrivacyNote
import com.example.frontend.ui.Components.ReceiptCodeBox
import com.example.frontend.ui.Components.SectionHeading
import com.example.frontend.ui.Components.SimpleButton
import com.example.frontend.ui.Components.SuccessCheckmarkBadge
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
                title = "You made your mark!",
                description = if (receipts.size > 1) {
                    "All ${receipts.size} of your votes have been recorded."
                } else {
                    "Your vote has been recorded."
                },
                textAlign = TextAlign.Center,
                modifier = Modifier.entrance(entered, index = 1)
            )

            Spacer(modifier = Modifier.height(Spacing.tight))

            // One receipt per ballot (KAN-18).
            receipts.forEach { receipt ->
                ReceiptCodeBox(
                    receiptCode = receipt.code,
                    label = "${receipt.ballot.shortLabel} ballot receipt:",
                    onCopyClick = {
                        scope.launch {
                            clipboard.setPlainText("${receipt.ballot.shortLabel} voting receipt", receipt.code)
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
                message = "Keep these receipts. They never show who you voted for."
            )

            Spacer(modifier = Modifier.height(Spacing.small))

            TextButton(onClick = onViewReceipt) {
                Text(text = if (receipts.size > 1) "View receipts" else "View receipt", style = MaterialTheme.typography.labelLarge)
            }
            SimpleButton(
                label = "Done",
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
