package com.example.frontend.ui.Components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.frontend.ui.theme.CardBorder
import com.example.frontend.ui.theme.FrontendTheme
import com.example.frontend.ui.theme.Spacing
import com.example.frontend.ui.theme.MutedText

/** Boxed receipt code with an optional copy action. */
@Composable
fun ReceiptCodeBox(
    receiptCode: String,
    modifier: Modifier = Modifier,
    label: String = "Your voting receipt:",
    onCopyClick: (() -> Unit)? = null
) {
    val shape = MaterialTheme.shapes.medium
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Color(0xFFF7FAFD))
            .border(width = 1.dp, color = CardBorder, shape = shape)
            .padding(horizontal = Spacing.card, vertical = Spacing.item),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.item)
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(Spacing.tight)
        ) {
            DescriptionLabel(text = label, color = MutedText)
            HeadingLabel(
                text = receiptCode,
                style = MaterialTheme.typography.titleLarge
            )
        }
        if (onCopyClick != null) {
            IconButton(onClick = onCopyClick) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy receipt code",
                    tint = MutedText
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun ReceiptCodeBoxPreview() {
    FrontendTheme {
        ReceiptCodeBox(
            receiptCode = "AB7F-92K4-6ZP1-3QX8",
            modifier = Modifier.padding(16.dp),
            onCopyClick = {}
        )
    }
}
