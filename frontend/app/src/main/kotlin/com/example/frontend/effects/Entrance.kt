package com.example.frontend.effects

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import com.example.frontend.ui.theme.EnterEasing
import com.example.frontend.ui.theme.MotionDurations
import com.example.frontend.ui.theme.motionDuration
import com.example.frontend.ui.theme.motionEnabled

/**
 * Flips to true on the first frame a screen is composed, which is what drives
 * the entrance animations below.
 */
@Composable
fun rememberEntranceTrigger(): State<Boolean> {
    val shown = remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { shown.value = true }
    return shown
}

/**
 * Fades and lifts content into place, offset by [index] so a list arrives as a
 * short cascade rather than all at once.
 *
 * Like [pressScale] this only ever touches alpha and translation inside the
 * draw phase, so a long list costs no extra layout work. The stagger is capped
 * at [MotionDurations.maxStaggeredItems] so the last row of a long list is not
 * left waiting.
 */
@Composable
fun Modifier.entrance(
    visible: Boolean,
    index: Int = 0,
    liftDp: Float = 14f
): Modifier {
    if (!motionEnabled) return this
    val delay = minOf(index, MotionDurations.maxStaggeredItems) * MotionDurations.stagger
    val progress by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(
            durationMillis = motionDuration(MotionDurations.medium),
            delayMillis = motionDuration(delay),
            easing = EnterEasing
        ),
        label = "entrance$index"
    )
    return this.graphicsLayer {
        alpha = progress
        translationY = (1f - progress) * liftDp * density
    }
}
