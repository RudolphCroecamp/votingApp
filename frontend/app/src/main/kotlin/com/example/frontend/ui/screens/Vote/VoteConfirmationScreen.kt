package com.example.frontend.ui.screens.Vote

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
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
    receiptCode: String = "AB7F-92K4-6ZP1-3QX8",
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
                .padding(horizontal = Spacing.screen, vertical = Spacing.card),
            verticalArrangement = Arrangement.spacedBy(Spacing.item),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(1f))

            SuccessCheckmarkBadge()

            Spacer(modifier = Modifier.height(Spacing.tight))

            SectionHeading(
                title = "You made your mark!",
                description = "Your vote has been recorded.",
                textAlign = TextAlign.Center,
                modifier = Modifier.entrance(entered, index = 1)
            )

            Spacer(modifier = Modifier.height(Spacing.tight))

            ReceiptCodeBox(
                receiptCode = receiptCode,
                onCopyClick = {
                    scope.launch { clipboard.setPlainText("Voting receipt", receiptCode) }
                },
                modifier = Modifier.entrance(entered, index = 2)
            )

            PrivacyNote(
                onLinkClick = onPrivacy,
                modifier = Modifier
                    .fillMaxWidth()
                    .entrance(entered, index = 3),
                message = "Keep this receipt. It never shows who you voted for."
            )

            Spacer(modifier = Modifier.weight(1f))

            TextButton(onClick = onViewReceipt) {
                Text(text = "View receipt", style = MaterialTheme.typography.labelLarge)
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
