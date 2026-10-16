package com.example.frontend.ui.screens.Vote

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.frontend.R
import com.example.frontend.ui.components.AppBottomBar
import com.example.frontend.ui.components.BottomTab
import com.example.frontend.ui.components.AppTopBar
import com.example.frontend.ui.components.HeadingLabel
import com.example.frontend.ui.components.StatusLabel
import com.example.frontend.ui.components.QrCodeImage
import com.example.frontend.ui.components.StatusType
import com.example.frontend.ui.theme.CardBorder
import com.example.frontend.ui.theme.DividerColor
import com.example.frontend.ui.theme.FrontendTheme
import com.example.frontend.ui.theme.MutedText
import com.example.frontend.ui.theme.PageBackground
import com.example.frontend.ui.theme.Spacing

@Composable
fun VoteReceiptScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    receipts: List<BallotReceipt> = SampleReceipts,
    electionName: String = stringResource(R.string.election_name_sample),
    status: String = stringResource(R.string.receipt_status_valid),
    selectedTab: BottomTab = BottomTab.Vote,
    onTabSelected: (BottomTab) -> Unit = {},
    backgroundColor: Color = PageBackground
) {
    Scaffold(
        modifier = modifier,
        containerColor = backgroundColor,
        topBar = { AppTopBar(
                title = stringResource(
                    if (receipts.size > 1) R.string.receipt_title_plural else R.string.receipt_title
                ),
                onBackClick = onBackClick
            ) },
        bottomBar = { AppBottomBar(selected = selectedTab, onSelect = onTabSelected) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(Spacing.screen),
            verticalArrangement = Arrangement.spacedBy(Spacing.item),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            receipts.forEach { receipt ->
                ReceiptDetailsCard(
                    receiptCode = receipt.code,
                    ballotTitle = stringResource(receipt.ballot.titleRes),
                    castAt = receipt.castAt,
                    electionName = electionName,
                    status = status
                )
            }
        }
    }
}

@Composable
private fun ReceiptDetailsCard(
    receiptCode: String,
    ballotTitle: String,
    castAt: String,
    electionName: String,
    status: String
) {
    val shape = MaterialTheme.shapes.large
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .border(width = 1.dp, color = CardBorder, shape = shape)
            .padding(Spacing.card),
        verticalArrangement = Arrangement.spacedBy(Spacing.item),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        QrCodeImage(content = receiptCode)
        HeadingLabel(
            text = stringResource(R.string.receipt_title),
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
        HeadingLabel(
            text = receiptCode,
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.labelLarge,
            textAlign = TextAlign.Center
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = ballotTitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = castAt,
                style = MaterialTheme.typography.bodySmall,
                color = MutedText
            )
        }
        HorizontalDivider(color = DividerColor)
        ReceiptDetailRow(
            icon = Icons.Default.DateRange,
            label = stringResource(R.string.receipt_field_election),
            value = electionName
        )
        ReceiptDetailRow(
            icon = Icons.Default.Lock,
            label = stringResource(R.string.receipt_field_id),
            value = receiptCode
        )
        ReceiptDetailRow(
            icon = Icons.Default.CheckCircle,
            label = stringResource(R.string.receipt_field_status),
            value = status,
            valueColor = StatusType.Success.contentColor
        )
        StatusLabel(
            message = stringResource(R.string.receipt_explainer),
            type = StatusType.Info,
            icon = Icons.Default.Lock,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun ReceiptDetailRow(
    icon: ImageVector,
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.primary
) {
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
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold),
            color = valueColor
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun VoteReceiptScreenPreview() {
    FrontendTheme() {
        VoteReceiptScreen(onBackClick = {})
    }
}
