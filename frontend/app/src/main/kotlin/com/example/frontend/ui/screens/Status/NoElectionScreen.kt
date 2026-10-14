package com.example.frontend.ui.screens.Status

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.HowToVote
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
import com.example.frontend.ui.theme.SurfaceSunken
import com.example.frontend.ui.theme.Spacing

@Composable
fun NoElectionScreen(
    onVotingInformation: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    nextElectionName: String? = null,
    nextElectionDate: String? = null,
    backgroundColor: Color = PageBackground
) {
    val dateKnown = nextElectionDate != null

    Scaffold(
        modifier = modifier,
        containerColor = backgroundColor,
        topBar = { AppTopBar(title = "No election open", onBackClick = onBackClick) }
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
                    imageVector = Icons.Default.HowToVote,
                    contentDescription = null,
                    tint = MutedText,
                    modifier = Modifier.size(44.dp)
                )
            }

            SectionHeading(
                title = "Nothing to vote on right now",
                description = "There is no election open at the moment. Your registration stays " +
                    "active, so you do not need to sign up again.",
                textAlign = TextAlign.Center
            )

            NextElectionCard(
                name = nextElectionName,
                date = nextElectionDate,
                modifier = Modifier.fillMaxWidth()
            )

            StatusLabel(
                message = if (dateKnown) {
                    "You will be able to vote from this app on the day. We will remind you closer " +
                        "to the time."
                } else {
                    "We will tell you as soon as a date is announced."
                },
                type = StatusType.Info,
                modifier = Modifier.fillMaxWidth()
            )

            SimpleButton(
                label = "Voting information",
                onClick = onVotingInformation,
                modifier = Modifier.fillMaxWidth(),
                variant = ButtonVariant.Tertiary,
                trailingIcon = Icons.AutoMirrored.Filled.ArrowForward
            )

            DescriptionLabel(
                text = "Check your voting district, what each ballot decides, and how your vote " +
                    "stays anonymous.",
                modifier = Modifier.fillMaxWidth(),
                color = MutedText,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun NextElectionCard(
    name: String?,
    date: String?,
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
            Icon(
                imageVector = Icons.Default.CalendarMonth,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )

            DescriptionLabel(
                text = if (date == null) "Next election" else "Next election opens",
                color = MutedText,
                textAlign = TextAlign.Center
            )

            Text(
                text = date ?: "Date not announced yet",
                style = if (date == null) {
                    MaterialTheme.typography.titleMedium
                } else {
                    MaterialTheme.typography.headlineSmall
                },
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )

            if (name != null) {
                HeadingLabel(
                    text = name,
                    style = MaterialTheme.typography.titleSmall
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun NoElectionScreenDateKnownPreview() {
    FrontendTheme {
        NoElectionScreen(
            onVotingInformation = {},
            onBackClick = {},
            nextElectionName = "2027 Provincial Election",
            nextElectionDate = "18 March 2027"
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun NoElectionScreenDateUnknownPreview() {
    FrontendTheme {
        NoElectionScreen(
            onVotingInformation = {},
            onBackClick = {}
        )
    }
}
