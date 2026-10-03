package com.example.frontend.ui.Vote

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.frontend.ui.Components.AppTopBar
import com.example.frontend.ui.Components.HeadingLabel
import com.example.frontend.ui.Components.IconLabel
import com.example.frontend.ui.Components.QrCodeImage
import com.example.frontend.ui.Components.StatusType
import com.example.frontend.ui.theme.FrontendTheme
import com.example.frontend.ui.theme.Spacing

@Composable
fun VoteReceiptScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    receiptCode: String = "AB7F-92K4-6ZP1-3QX8",
    ballotTitle: String = "National Assembly",
    castAt: String = "2026-05-27 09:42",
    electionName: String = "2026 National Election",
    status: String = "Valid",
    backgroundColor: Color = Color(0xFFEEF1F6)
) {
    Scaffold(
        modifier = modifier,
        containerColor = backgroundColor,
        topBar = { AppTopBar(title = "Voting Receipt", onBackClick = onBackClick) }
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
            ReceiptDetailsCard(
                receiptCode = receiptCode,
                ballotTitle = ballotTitle,
                castAt = castAt,
                electionName = electionName,
                status = status
            )
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
    val shape = MaterialTheme.shapes.medium
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Color.White)
            .border(width = 1.dp, color = Color(0xFFE1E5EC), shape = shape)
            .padding(Spacing.card),
        verticalArrangement = Arrangement.spacedBy(Spacing.item),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        QrCodeImage(content = receiptCode)
        HeadingLabel(
            text = "Voting Receipt",
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.titleMedium,
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
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = castAt,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        HorizontalDivider(color = Color(0xFFD9D9D9))
        ReceiptDetailRow(icon = Icons.Default.DateRange, label = "Election", value = electionName)
        ReceiptDetailRow(icon = Icons.Default.Lock, label = "Receipt ID", value = receiptCode)
        ReceiptDetailRow(
            icon = Icons.Default.CheckCircle,
            label = "Status",
            value = status,
            valueColor = StatusType.Success.contentColor
        )
        IconLabel(
            message = "This receipt can be used to verify that your vote was recorded, but it does not reveal who you voted for.",
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
            color = Color(0xFF6B7280)
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
