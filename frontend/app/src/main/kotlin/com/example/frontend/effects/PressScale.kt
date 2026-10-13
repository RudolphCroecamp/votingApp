package com.example.frontend.effects

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import com.example.frontend.ui.theme.motionEnabled
import com.example.frontend.ui.theme.pressSpring

/**
 * Shrinks a surface very slightly while it is held down.
 *
 * The scale is read inside [graphicsLayer]'s lambda, so the animation runs in
 * the draw phase only: no recomposition and no re-layout per frame, which is
 * what keeps this cheap on weak GPUs. Returns the receiver untouched when the
 * device has animations switched off.
 */
@Composable
fun Modifier.pressScale(
    interactionSource: InteractionSource,
    pressedScale: Float = 0.975f
): Modifier {
    if (!motionEnabled) return this
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) pressedScale else 1f,
        animationSpec = pressSpring(),
        label = "pressScale"
    )
    return this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}
