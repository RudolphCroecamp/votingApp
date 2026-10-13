package com.example.frontend.ui.Components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.frontend.ui.theme.FrontendTheme
import com.example.frontend.ui.theme.MotionDurations
import com.example.frontend.ui.theme.SuccessBorder
import com.example.frontend.ui.theme.motionDuration
import com.example.frontend.ui.theme.motionEnabled

@Composable
fun SuccessCheckmarkBadge(
    modifier: Modifier = Modifier,
    size: Dp = 84.dp,
    circleColor: Color = SuccessBorder,
    checkColor: Color = Color.White
) {
    val animated = motionEnabled
    val tickDuration = motionDuration(MotionDurations.slow)
    // One shot: the disc springs in, then the tick draws itself along its own
    // path. Both run on a single small Canvas, so there is nothing to lay out.
    val pop = remember { Animatable(if (animated) 0.6f else 1f) }
    val draw = remember { Animatable(if (animated) 0f else 1f) }

    LaunchedEffect(animated) {
        if (!animated) return@LaunchedEffect
        pop.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
    }
    LaunchedEffect(animated) {
        if (!animated) return@LaunchedEffect
        draw.animateTo(1f, tween(durationMillis = tickDuration))
    }

    // PathMeasure is allocated once and only re-run when the canvas resizes.
    val measure = remember { PathMeasure() }
    val fullTick = remember { Path() }
    val visibleTick = remember { Path() }

    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer {
                scaleX = pop.value
                scaleY = pop.value
                alpha = pop.value
            }
            .background(color = circleColor, shape = CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size * 0.5f)) {
            val width = this.size.width
            val height = this.size.height

            fullTick.reset()
            fullTick.moveTo(width * 0.04f, height * 0.52f)
            fullTick.lineTo(width * 0.38f, height * 0.86f)
            fullTick.lineTo(width * 0.96f, height * 0.16f)

            val stroke = Stroke(
                width = width * 0.16f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )

            if (draw.value >= 1f) {
                drawPath(path = fullTick, color = checkColor, style = stroke)
                return@Canvas
            }

            measure.setPath(fullTick, false)
            visibleTick.reset()
            measure.getSegment(0f, measure.length * draw.value, visibleTick, true)
            drawPath(path = visibleTick, color = checkColor, style = stroke)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SuccessCheckmarkBadgePreview() {
    FrontendTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SuccessCheckmarkBadge()
            SuccessCheckmarkBadge(size = 48.dp)
            SuccessCheckmarkBadge(size = 24.dp)
        }
    }
}
