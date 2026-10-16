package com.example.frontend.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.frontend.ui.theme.FrontendTheme
import com.example.frontend.ui.theme.Spacing

private val SpinnerSize = 24.dp

@Composable
fun IndeterminateCircularIndicator(
    modifier: Modifier = Modifier,
    size: Dp = SpinnerSize,
    color: Color = MaterialTheme.colorScheme.primary
) {
    CircularProgressIndicator(
        modifier = modifier.size(size),
        color = color,
        trackColor = MaterialTheme.colorScheme.surfaceVariant
    )
}

@Composable
fun IndeterminateLinearIndicator(
    modifier: Modifier = Modifier,
    label: String? = null,
    color: Color = MaterialTheme.colorScheme.primary
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.small),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LinearProgressIndicator(
            modifier = Modifier.fillMaxWidth(),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
        if (label != null) {
            DescriptionLabel(text = label, textAlign = TextAlign.Center)
        }
    }
}

/**
 * Full-screen loading state (KAN-6): dims whatever is underneath and swallows taps,
 * so a busy screen stays visible but can't be double-submitted.
 */
@Composable
fun FullScreenLoading(
    modifier: Modifier = Modifier,
    label: String? = null,
    scrimColor: Color = MaterialTheme.colorScheme.background.copy(alpha = 0.85f)
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(scrimColor)
            // Consume every touch so nothing behind the overlay can be pressed.
            .pointerInput(Unit) { awaitPointerEventScope { while (true) awaitPointerEvent() } }
            .semantics { contentDescription = label ?: "Loading" },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(Spacing.screen),
            verticalArrangement = Arrangement.spacedBy(Spacing.item),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            IndeterminateCircularIndicator(size = 48.dp)
            if (label != null) {
                DescriptionLabel(text = label, textAlign = TextAlign.Center)
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 480)
@Composable
private fun FullScreenLoadingPreview() {
    FrontendTheme { FullScreenLoading(label = "Submitting your vote…") }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun LoadingIndicatorPreview() {
    FrontendTheme {
        Column(
            modifier = Modifier.padding(Spacing.screen),
            verticalArrangement = Arrangement.spacedBy(Spacing.section),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            IndeterminateCircularIndicator()
            IndeterminateLinearIndicator(label = "Verifying your details…")
        }
    }
}
