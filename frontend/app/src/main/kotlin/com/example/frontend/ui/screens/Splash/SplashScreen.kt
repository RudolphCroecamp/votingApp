package com.example.frontend.ui.screens.Splash

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.frontend.R
import com.example.frontend.ui.components.DescriptionLabel
import com.example.frontend.ui.components.FlagAccentBar
import com.example.frontend.ui.components.HeadingLabel
import com.example.frontend.ui.theme.FrontendTheme
import com.example.frontend.ui.theme.MutedText
import com.example.frontend.ui.theme.PageBackground
import com.example.frontend.ui.theme.Spacing
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

/** How long each element takes to fade/scale in. */
private const val LOGO_REVEAL_MS = 400
private const val TITLE_REVEAL_MS = 400
private const val COMMITMENT_REVEAL_MS = 400
private const val LOCATION_REVEAL_MS = 400
private const val FADE_OUT_MS = 400

/** How long each stage stays on screen before the next one starts. */
private const val LOGO_HOLD_MS = 800
private const val TITLE_HOLD_MS = 1200
private const val COMMITMENT_HOLD_MS = 1000
private const val LOCATION_HOLD_MS = 1000

/** Stage indices; an element is visible once [SplashScreen]'s counter reaches its index. */
private const val STAGE_LOGO = 1
private const val STAGE_TITLE = 2
private const val STAGE_COMMITMENT = 3
private const val STAGE_LOCATION = 4

/**
 * Pre-setup splash sequence for the Voter's App: reveals the brand asset, then the
 * mission copy, then fades out and calls [onFinished] so the host can navigate on.
 *
 * The whole sequence runs for roughly 4.5 seconds. It is driven by a [LaunchedEffect],
 * so it is cancelled automatically if the user leaves the screen first.
 */
@Composable
fun SplashScreen(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
    locationText: String = stringResource(R.string.sa_voting_location_text),
    backgroundColor: Color = PageBackground
) {
    var stage by remember { mutableIntStateOf(0) }
    var exiting by remember { mutableStateOf(false) }

    // Keep the latest callback so the long-running effect never calls a stale lambda.
    val finish by rememberUpdatedState(onFinished)

    val screenAlpha by animateFloatAsState(
        targetValue = if (exiting) 0f else 1f,
        animationSpec = tween(FADE_OUT_MS),
        label = "screenAlpha"
    )

    LaunchedEffect(Unit) {
        // 1. INITIAL LOAD: reveal the brand asset while auth/voter modules warm up.
        stage = STAGE_LOGO
        delay(LOGO_HOLD_MS.milliseconds)

        // 2. THEMATIC SHIFT: fade in the main message.
        stage = STAGE_TITLE
        delay(TITLE_HOLD_MS.milliseconds)

        // 3. VALUE MESSAGE: reveal the mission point.
        stage = STAGE_COMMITMENT
        delay(COMMITMENT_HOLD_MS.milliseconds)

        // 4. TRANSITION: the final empowering moment.
        stage = STAGE_LOCATION
        delay(LOCATION_HOLD_MS.milliseconds)

        // 5. FINISH: fade out, then hand control back to the navigation host.
        exiting = true
        delay(FADE_OUT_MS.milliseconds)
        finish()
    }

    val logoAlpha by stageAlpha(STAGE_LOGO, stage, LOGO_REVEAL_MS, from = 0.2f)
    val logoScale by stageScale(STAGE_LOGO, stage, LOGO_REVEAL_MS, from = 0.85f)
    val titleAlpha by stageAlpha(STAGE_TITLE, stage, TITLE_REVEAL_MS)
    val commitmentAlpha by stageAlpha(STAGE_COMMITMENT, stage, COMMITMENT_REVEAL_MS)
    val locationAlpha by stageAlpha(STAGE_LOCATION, stage, LOCATION_REVEAL_MS)
    val locationScale by stageScale(STAGE_LOCATION, stage, LOCATION_REVEAL_MS, from = 0.9f)

    Scaffold(
        modifier = modifier.alpha(screenAlpha),
        containerColor = backgroundColor
    ) { innerPadding ->
        Box(Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = Spacing.screen),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.weight(1f))

                Image(
                    painter = painterResource(R.drawable.ic_ballot_logo),
                    contentDescription = null,
                    modifier = Modifier
                        .size(132.dp)
                        .alpha(logoAlpha)
                        .scale(logoScale)
                )

                Spacer(Modifier.height(Spacing.section))

                HeadingLabel(
                    text = stringResource(R.string.splash_title),
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(titleAlpha),
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(Spacing.card))

                DescriptionLabel(
                    text = stringResource(R.string.splash_subtitle),
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(commitmentAlpha),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.weight(1f))

                DescriptionLabel(
                    text = locationText,
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(locationAlpha)
                        .scale(locationScale),
                    color = MutedText,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(Spacing.section))
            }

            FlagAccentBar(Modifier.align(Alignment.BottomCenter))
        }
    }
}

@Composable
private fun stageAlpha(index: Int, stage: Int, durationMillis: Int, from: Float = 0f) =
    animateFloatAsState(
        targetValue = if (stage >= index) 1f else from,
        animationSpec = tween(durationMillis),
        label = "alpha$index"
    )

@Composable
private fun stageScale(index: Int, stage: Int, durationMillis: Int, from: Float) =
    animateFloatAsState(
        targetValue = if (stage >= index) 1f else from,
        animationSpec = tween(durationMillis),
        label = "scale$index"
    )

@Preview(showBackground = true)
@Composable
private fun SplashScreenPreview() {
    FrontendTheme {
        SplashScreen(onFinished = {})
    }
}
