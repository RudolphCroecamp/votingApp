package com.example.frontend.ui.Components

import androidx.annotation.Keep
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.annotation.StringRes
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.frontend.R
import com.example.frontend.ui.theme.DividerColor
import com.example.frontend.ui.theme.FrontendTheme
import com.example.frontend.ui.theme.MotionDurations
import com.example.frontend.ui.theme.MutedText
import com.example.frontend.ui.theme.Spacing
import com.example.frontend.ui.theme.StandardEasing
import com.example.frontend.ui.theme.motionDuration
import kotlinx.serialization.Serializable

// @Keep: route argument; R8 must not rename it once minify is on (KAN-41).
@Keep
@Serializable
enum class Ballot(@param:StringRes val titleRes: Int, @param:StringRes val shortLabelRes: Int) {
    National(R.string.ballot_national_title, R.string.ballot_national_short),
    Regional(R.string.ballot_regional_title, R.string.ballot_regional_short),
    Provincial(R.string.ballot_provincial_title, R.string.ballot_provincial_short)
}

val BALLOT_ORDER: List<Ballot> = Ballot.entries

private val TrackHeight = 4.dp

@Composable
fun BallotStepIndicator(
    ballot: Ballot,
    modifier: Modifier = Modifier,
    ballots: List<Ballot> = BALLOT_ORDER
) {
    val position = (ballots.indexOf(ballot) + 1).coerceAtLeast(1)
    val total = ballots.size
    val accent = MaterialTheme.colorScheme.primary
    val shortLabel = stringResource(ballot.shortLabelRes)
    val stepDescription = stringResource(R.string.ballot_step_description, position, total, shortLabel)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = stepDescription
            },
        verticalArrangement = Arrangement.spacedBy(Spacing.small)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(Spacing.small),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.ballot_step_count, position, total),
                style = MaterialTheme.typography.labelMedium,
                color = MutedText
            )
            Text(
                text = "·",
                style = MaterialTheme.typography.labelMedium,
                color = MutedText
            )
            Text(
                text = shortLabel,
                style = MaterialTheme.typography.labelLarge,
                color = accent
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.tight)
        ) {
            ballots.forEachIndexed { index, _ ->
                val fill by animateFloatAsState(
                    targetValue = if (index < position) 1f else 0f,
                    animationSpec = tween(
                        motionDuration(MotionDurations.medium),
                        easing = StandardEasing
                    ),
                    label = "ballotSegment$index"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(TrackHeight)
                        .clip(CircleShape)
                        .background(DividerColor)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                scaleX = fill
                                transformOrigin = TransformOrigin(0f, 0.5f)
                            }
                            .background(accent)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun BallotStepIndicatorPreview() {
    FrontendTheme {
        Column(
            modifier = Modifier.padding(Spacing.screen),
            verticalArrangement = Arrangement.spacedBy(Spacing.section)
        ) {
            BALLOT_ORDER.forEach { BallotStepIndicator(ballot = it) }
        }
    }
}
