package com.example.frontend.ui.Vote

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.frontend.ui.Components.AppBottomBar
import com.example.frontend.ui.Components.BottomTab
import com.example.frontend.ui.Components.AppTopBar
import com.example.frontend.ui.Components.ButtonVariant
import com.example.frontend.ui.Components.DescriptionLabel
import com.example.frontend.ui.Components.HeadingLabel
import com.example.frontend.ui.Components.IconLabel
import com.example.frontend.ui.Components.ReceiptCodeBox
import com.example.frontend.ui.Components.SimpleButton
import com.example.frontend.ui.Components.StatusType
import com.example.frontend.ui.Components.SuccessCheckmarkBadge
import com.example.frontend.ui.theme.FrontendTheme
import com.example.frontend.ui.theme.Spacing
import com.example.frontend.ui.theme.PageBackground

@Composable
fun VoteConfirmationScreen(
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
    receiptCode: String = "AB7F-92K4-6ZP1-3QX8",
    topBarTitle: String = "Vote Confirmation",
    onViewReceipt: () -> Unit = {},
    selectedTab: BottomTab = BottomTab.Vote,
    onTabSelected: (BottomTab) -> Unit = {},
    backgroundColor: Color = PageBackground
) {
    val clipboard = LocalClipboardManager.current

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
                .padding(Spacing.screen),
            verticalArrangement = Arrangement.spacedBy(Spacing.item),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(1f))

            SuccessCheckmarkBadge()

            Spacer(modifier = Modifier.height(Spacing.tight))

            HeadingLabel(
                text = "Vote Recorded",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            DescriptionLabel(
                text = "Your vote has been successfully\nrecorded.",
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(Spacing.tight))

            ReceiptCodeBox(
                receiptCode = receiptCode,
                onCopyClick = { clipboard.setText(AnnotatedString(receiptCode)) }
            )

            androidx.compose.foundation.layout.Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                androidx.compose.material3.Icon(
                    Icons.Default.Lock, null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 4.dp).height(18.dp)
                )
                DescriptionLabel(
                    text = "Keep this receipt for verification.\nYour receipt does NOT reveal\nwho you voted for.",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp, lineHeight = 18.sp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            androidx.compose.material3.TextButton(onClick = onViewReceipt) {
                androidx.compose.material3.Text("View Receipt")
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
