package com.example.frontend.ui.screens.Vote

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.frontend.R
import com.example.frontend.ui.Components.AppBottomBar
import com.example.frontend.ui.Components.AppTextField
import com.example.frontend.ui.Components.AppTopBar
import com.example.frontend.ui.Components.Ballot
import com.example.frontend.ui.Components.BottomTab
import com.example.frontend.ui.Components.ButtonVariant
import com.example.frontend.ui.Components.DescriptionLabel
import com.example.frontend.ui.Components.HeadingLabel
import com.example.frontend.ui.Components.StatusLabel
import com.example.frontend.ui.Components.SectionHeading
import com.example.frontend.ui.Components.SimpleButton
import com.example.frontend.ui.Components.StatusType
import com.example.frontend.ui.theme.CardBorder
import com.example.frontend.ui.theme.FrontendTheme
import com.example.frontend.ui.theme.MotionDurations
import com.example.frontend.ui.theme.MutedText
import com.example.frontend.ui.theme.StandardEasing
import com.example.frontend.ui.theme.motionDuration
import com.example.frontend.ui.theme.PageBackground
import com.example.frontend.ui.theme.Spacing
import com.example.frontend.util.getPlainText
import kotlinx.coroutines.launch

private const val RECEIPT_CODE_LENGTH = 19

sealed interface ReceiptLookup {
    data object Idle : ReceiptLookup
    data object NotFound : ReceiptLookup
    data class Found(
        val receiptCode: String,
        val ballot: Ballot,
        val castAt: String,
        val electionName: String
    ) : ReceiptLookup
}

@Composable
fun VoteVerifyReceiptScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    selectedTab: BottomTab = BottomTab.Vote,
    onTabSelected: (BottomTab) -> Unit = {},
    onVerify: (String) -> ReceiptLookup = ::sampleLookup,
    backgroundColor: Color = PageBackground
) {
    var receiptCode by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<ReceiptLookup>(ReceiptLookup.Idle) }
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    val resultSwapMillis = motionDuration(MotionDurations.medium)

    Scaffold(
        modifier = modifier,
        containerColor = backgroundColor,
        topBar = { AppTopBar(title = stringResource(R.string.verify_title), onBackClick = onBackClick) },
        bottomBar = { AppBottomBar(selected = selectedTab, onSelect = onTabSelected) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(Spacing.screen),
            verticalArrangement = Arrangement.spacedBy(Spacing.item)
        ) {
            SectionHeading(
                title = stringResource(R.string.verify_heading),
                description = stringResource(R.string.verify_description)
            )

            AppTextField(
                value = receiptCode,
                onValueChange = {
                    receiptCode = it.uppercase()
                    result = ReceiptLookup.Idle
                },
                placeholder = stringResource(R.string.verify_placeholder),
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = Icons.Default.Lock,
                maxLength = RECEIPT_CODE_LENGTH
            )

            SimpleButton(
                label = stringResource(R.string.verify_button),
                onClick = { result = onVerify(receiptCode.trim()) },
                modifier = Modifier.fillMaxWidth(),
                variant = ButtonVariant.Tertiary,
                leadingIcon = Icons.Default.Search,
                enabled = receiptCode.isNotBlank()
            )

            SimpleButton(
                label = stringResource(R.string.verify_paste),
                onClick = {
                    scope.launch {
                        clipboard.getPlainText()?.let {
                            receiptCode = it.trim().uppercase().take(RECEIPT_CODE_LENGTH)
                            result = ReceiptLookup.Idle
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                variant = ButtonVariant.Secondary,
                leadingIcon = Icons.Default.ContentPaste
            )

            Spacer(modifier = Modifier.height(Spacing.tight))

            // Fading between outcomes makes the lookup feel like it answered,
            // rather than the panel blinking from one state to another.
            AnimatedContent(
                targetState = result,
                transitionSpec = {
                    val spec = tween<Float>(resultSwapMillis, easing = StandardEasing)
                    fadeIn(spec) togetherWith fadeOut(spec)
                },
                label = "receiptLookup"
            ) { lookup ->
                when (lookup) {
                    ReceiptLookup.Idle -> StatusLabel(
                        message = stringResource(R.string.verify_idle_info),
                        type = StatusType.Info,
                        modifier = Modifier.fillMaxWidth()
                    )

                    ReceiptLookup.NotFound -> StatusLabel(
                        message = stringResource(R.string.verify_not_found),
                        type = StatusType.Error,
                        modifier = Modifier.fillMaxWidth()
                    )

                    is ReceiptLookup.Found -> VerifiedReceiptCard(lookup)
                }
            }
        }
    }
}

@Composable
private fun VerifiedReceiptCard(lookup: ReceiptLookup.Found) {
    val shape = MaterialTheme.shapes.large
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .border(width = 1.dp, color = CardBorder, shape = shape)
            .padding(Spacing.card),
        verticalArrangement = Arrangement.spacedBy(Spacing.item)
    ) {
        StatusLabel(
            message = stringResource(R.string.verify_found),
            type = StatusType.Success,
            modifier = Modifier.fillMaxWidth()
        )
        HorizontalDivider(color = CardBorder)
        VerifiedDetailRow(Icons.Default.Lock, stringResource(R.string.receipt_field_id), lookup.receiptCode)
        VerifiedDetailRow(
            Icons.Default.DateRange,
            stringResource(R.string.receipt_field_election),
            lookup.electionName
        )
        VerifiedDetailRow(
            Icons.Default.DateRange,
            stringResource(R.string.receipt_field_ballot),
            stringResource(lookup.ballot.titleRes)
        )
        VerifiedDetailRow(Icons.Default.DateRange, stringResource(R.string.receipt_field_recorded), lookup.castAt)
    }
}

@Composable
private fun VerifiedDetailRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.item)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            color = MutedText
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

fun sampleLookup(code: String): ReceiptLookup =
    if (code.equals("AB7F-92K4-6ZP1-3QX8", ignoreCase = true)) {
        ReceiptLookup.Found(
            receiptCode = code.uppercase(),
            ballot = Ballot.National,
            castAt = "2026-05-27 09:42",
            electionName = "2026 National Election"
        )
    } else {
        ReceiptLookup.NotFound
    }

@Preview(showBackground = true)
@Composable
private fun VoteVerifyReceiptScreenPreview() {
    FrontendTheme {
        VoteVerifyReceiptScreen(onBackClick = {})
    }
}
