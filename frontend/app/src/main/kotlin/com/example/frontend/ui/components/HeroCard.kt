package com.example.frontend.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HowToVote
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.frontend.R
import com.example.frontend.ui.theme.BrandNavy
import com.example.frontend.ui.theme.FrontendTheme
import com.example.frontend.ui.theme.GoldAccent
import com.example.frontend.ui.theme.GoldContainer
import com.example.frontend.ui.theme.GoldContent
import com.example.frontend.ui.theme.MutedText
import com.example.frontend.ui.theme.Spacing
import com.example.frontend.ui.theme.SuccessContainer
import com.example.frontend.ui.theme.SuccessContent

/** Which of the three home hero states to render. */
enum class HeroState { VotingOpen, VoteCast, ComingUp }

/**
 * The single hero card that carries the home screen. It swaps status chip,
 * copy, decoration and call to action for each [HeroState].
 */
@Composable
fun HeroCard(
    state: HeroState,
    title: String,
    subtitle: String,
    actionLabel: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
    footnote: String? = null
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.card),
            verticalArrangement = Arrangement.spacedBy(Spacing.item)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(Spacing.small)
                ) {
                    HeroStatusChip(state)
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        color = BrandNavy
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        ),
                        color = MutedText
                    )
                }
                HeroDecoration(state)
            }

            HeroAction(state = state, label = actionLabel, onClick = onAction)

            if (footnote != null) {
                Text(
                    text = footnote,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    ),
                    color = MutedText
                )
            }
        }
    }
}

@Composable
private fun HeroStatusChip(state: HeroState, modifier: Modifier = Modifier) {
    val (label, container, content) = when (state) {
        HeroState.VotingOpen -> Triple(stringResource(R.string.hero_status_voting_open), SuccessContainer, SuccessContent)
        HeroState.VoteCast -> Triple(stringResource(R.string.hero_status_vote_cast), GoldContainer, GoldContent)
        HeroState.ComingUp -> Triple(stringResource(R.string.hero_status_coming_up), GoldContainer, GoldContent)
    }
    Surface(modifier = modifier, shape = CircleShape, color = container, contentColor = content) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            when (state) {
                HeroState.VotingOpen -> Box(
                    Modifier.size(7.dp).background(SuccessContent, CircleShape)
                )

                HeroState.VoteCast -> Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(13.dp)
                )

                HeroState.ComingUp -> Box(
                    Modifier.size(7.dp).background(GoldAccent, CircleShape)
                )
            }
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.6.sp
            )
        }
    }
}

@Composable
private fun HeroDecoration(state: HeroState, modifier: Modifier = Modifier) {
    when (state) {
        HeroState.VotingOpen -> Icon(
            imageVector = Icons.Default.HowToVote,
            contentDescription = null,
            tint = BrandNavy,
            modifier = modifier.size(40.dp)
        )

        HeroState.VoteCast -> Box(
            modifier = modifier
                .size(40.dp)
                .background(SuccessContent, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(26.dp)
            )
        }

        HeroState.ComingUp -> Icon(
            imageVector = Icons.Default.Schedule,
            contentDescription = null,
            tint = BrandNavy,
            modifier = modifier.size(40.dp)
        )
    }
}

@Composable
private fun HeroAction(
    state: HeroState,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val trailingIcon = when (state) {
        HeroState.ComingUp -> Icons.Default.NotificationsActive
        else -> Icons.AutoMirrored.Filled.ArrowForward
    }
    val content: @Composable () -> Unit = {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, style = MaterialTheme.typography.labelLarge)
            GoldIconBadge(icon = trailingIcon)
        }
    }

    if (state == HeroState.VotingOpen) {
        Button(
            onClick = onClick,
            modifier = modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = Color.White
            )
        ) { content() }
    } else {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandNavy),
            border = BorderStroke(1.5.dp, BrandNavy)
        ) { content() }
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun HeroCardPreview() {
    FrontendTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            HeroCard(
                state = HeroState.VotingOpen,
                title = "2026 Local Government Election",
                subtitle = "Closes today at 21:00. Make your mark!",
                actionLabel = "Vote now",
                onAction = {}
            )
            HeroCard(
                state = HeroState.VoteCast,
                title = "You made your mark, Thandi!",
                subtitle = "Recorded at 10:42 · Receipt ID 7F3A-92KD",
                actionLabel = "View vote receipt",
                onAction = {},
                footnote = "Your receipt proves you voted. It never shows who for."
            )
            HeroCard(
                state = HeroState.ComingUp,
                title = "2026 Local Government Election",
                subtitle = "Voting opens in 2 days · 07:00. Get ready!",
                actionLabel = "Remind me",
                onAction = {}
            )
        }
    }
}
