package com.example.frontend.ui.screens.Status

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.frontend.ui.Components.AppTopBar
import com.example.frontend.ui.Components.ButtonVariant
import com.example.frontend.ui.Components.DescriptionLabel
import com.example.frontend.ui.Components.HeadingLabel
import com.example.frontend.ui.Components.SectionHeading
import com.example.frontend.ui.Components.SimpleButton
import com.example.frontend.ui.Components.StatusLabel
import com.example.frontend.ui.Components.StatusType
import com.example.frontend.ui.theme.CardBorder
import com.example.frontend.ui.theme.FrontendTheme
import com.example.frontend.ui.theme.MutedText
import com.example.frontend.ui.theme.PageBackground
import com.example.frontend.ui.theme.Spacing
import com.example.frontend.ui.theme.SurfaceSunken

@Composable
fun ElectionClosedScreen(
    onViewResults: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    electionName: String? = null,
    closedOn: String? = null,
    resultsPublished: Boolean = false,
    resultsExpected: String? = null,
    backgroundColor: Color = PageBackground
) {
    Scaffold(
        modifier = modifier,
        containerColor = backgroundColor,
        topBar = { AppTopBar(title = "Voting has closed", onBackClick = onBackClick) }
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
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .background(SurfaceSunken, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.EventBusy,
                    contentDescription = null,
                    tint = MutedText,
                    modifier = Modifier.size(44.dp)
                )
            }

            SectionHeading(
                title = "This ballot is closed",
                description = "Voting has ended, so no more ballots can be cast. " +
                    "Any vote you already sent still counts.",
                textAlign = TextAlign.Center
            )

            ClosedElectionCard(
                electionName = electionName,
                closedOn = closedOn,
                modifier = Modifier.fillMaxWidth()
            )

            StatusLabel(
                message = if (resultsPublished) {
                    "The results are out. You can see the count for every ballot."
                } else {
                    resultsExpected?.let { "Results are expected $it." }
                        ?: "Results are being counted. They appear here once the Electoral " +
                        "Commission publishes them."
                },
                type = if (resultsPublished) StatusType.Success else StatusType.Info,
                modifier = Modifier.fillMaxWidth()
            )

            SimpleButton(
                label = if (resultsPublished) "View results" else "Results not published yet",
                onClick = onViewResults,
                modifier = Modifier.fillMaxWidth(),
                variant = ButtonVariant.Tertiary,
                leadingIcon = Icons.Default.BarChart,
                trailingIcon = if (resultsPublished) Icons.AutoMirrored.Filled.ArrowForward else null,
                enabled = resultsPublished
            )

            DescriptionLabel(
                text = "If you voted, keep your receipt code. You can still check that your " +
                    "ballot was counted after the results are published.",
                modifier = Modifier.fillMaxWidth(),
                color = MutedText,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ClosedElectionCard(
    electionName: String?,
    closedOn: String?,
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
                .padding(horizontal = Spacing.card, vertical = Spacing.section),
            verticalArrangement = Arrangement.spacedBy(Spacing.small),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (electionName != null) {
                HeadingLabel(
                    text = electionName,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            DescriptionLabel(
                text = if (closedOn == null) "Voting is closed" else "Voting closed on",
                color = MutedText,
                textAlign = TextAlign.Center
            )

            Text(
                text = closedOn ?: "No more votes are being accepted",
                style = if (closedOn == null) {
                    MaterialTheme.typography.titleSmall
                } else {
                    MaterialTheme.typography.headlineSmall
                },
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ElectionClosedScreenResultsOutPreview() {
    FrontendTheme {
        ElectionClosedScreen(
            onViewResults = {},
            onBackClick = {},
            electionName = "2026 Local Government Election",
            closedOn = "21 May 2026",
            resultsPublished = true
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ElectionClosedScreenCountingPreview() {
    FrontendTheme {
        ElectionClosedScreen(
            onViewResults = {},
            onBackClick = {},
            electionName = "2026 Local Government Election",
            closedOn = "21 May 2026",
            resultsExpected = "within 48 hours"
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ElectionClosedScreenMinimalPreview() {
    FrontendTheme {
        ElectionClosedScreen(
            onViewResults = {},
            onBackClick = {}
        )
    }
}
