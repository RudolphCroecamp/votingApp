package com.example.frontend.ui.Welcome

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.frontend.ui.Components.BrandHeader
import com.example.frontend.ui.Components.ButtonVariant
import com.example.frontend.ui.Components.DescriptionLabel
import com.example.frontend.ui.Components.SimpleButton
import com.example.frontend.ui.theme.FlagBlack
import com.example.frontend.ui.theme.FlagBlue
import com.example.frontend.ui.theme.FlagGold
import com.example.frontend.ui.theme.FlagGreen
import com.example.frontend.ui.theme.FlagRed
import com.example.frontend.ui.theme.FlagWhite
import com.example.frontend.ui.theme.FrontendTheme
import com.example.frontend.ui.theme.PageBackground
import com.example.frontend.ui.theme.Spacing
import kotlin.math.PI
import kotlin.math.hypot
import kotlin.math.sin

private const val WAVE_STEPS = 64

@Composable
fun WelcomeScreen(
    onGetStarted: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = PageBackground
) {
    Scaffold(
        modifier = modifier,
        containerColor = backgroundColor
    ) { innerPadding ->
        Box(Modifier.fillMaxSize()) {
            WelcomeDecor(Modifier.fillMaxSize(), backgroundColor = backgroundColor)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = innerPadding.calculateTopPadding() + Spacing.section)
                    .padding(horizontal = Spacing.screen),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.weight(1f))

                BrandHeader(
                    title = "South Africa\nNational Voting App",
                    logoSize = 120.dp
                )

                Spacer(modifier = Modifier.height(Spacing.small))

                DescriptionLabel(
                    text = "Your vote. Your voice.\nA stronger South Africa.",
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(Spacing.section * 2))

                SimpleButton(
                    label = "Get Started",
                    onClick = onGetStarted,
                    modifier = Modifier.fillMaxWidth(),
                    variant = ButtonVariant.Tertiary
                )

                Spacer(
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = innerPadding.calculateBottomPadding() + Spacing.section)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun WelcomeScreenPreview() {
    FrontendTheme {
        WelcomeScreen(onGetStarted = {})
    }
}

private fun DrawScope.drawSouthAfricanFlag(
    topLeft: Offset,
    flagWidth: Float,
    cornerRadius: Float = 0f,
    waveDepth: Float = 0f,
    waveCount: Float = 1.5f,
    wavePhase: Float = 0f,
    foldStrength: Float = 0.5f,
    fadeInto: Color = Color.Unspecified
) {
    val w = flagWidth
    val h = w * 2f / 3f

    val whiteBand = h * 0.05f
    val greenBand = h * 0.15f
    val goldBand = h * 0.05f
    val pallBand = greenBand + whiteBand * 2f

    val junctionX = h * 0.9f
    val armLength = hypot(junctionX, h / 2f)
    val ux = junctionX / armLength
    val uy = (h / 2f) / armLength

    val pallCentre = pallBand / 2f
    val goldEdge = pallBand
    val blackEdge = pallBand + goldBand

    fun apexX(d: Float) = (ux * h / 2f - d) / uy
    fun hoistY(d: Float) = d / ux

    val overshoot = h

    fun arm(mirrored: Boolean): Path {
        fun y(value: Float) = if (mirrored) h - value else value
        val startX = -uy * pallCentre - ux * overshoot
        val startY = ux * pallCentre - uy * overshoot
        return Path().apply {
            moveTo(startX, y(startY))
            lineTo(apexX(pallCentre), y(h / 2f))
            lineTo(w + overshoot, y(h / 2f))
        }
    }

    fun triangle(d: Float) = Path().apply {
        moveTo(0f, hoistY(d))
        lineTo(apexX(d), h / 2f)
        lineTo(0f, h - hoistY(d))
        close()
    }

    val amplitude = h * waveDepth / 2f
    val baseline = h - amplitude

    fun waveY(t: Float) = baseline + amplitude * sin(t * waveCount * 2f * PI.toFloat() + wavePhase)

    fun bottomEdge(dy: Float) = Path().apply {
        moveTo(topLeft.x, topLeft.y + waveY(0f) + dy)
        for (i in 1..WAVE_STEPS) {
            val t = i / WAVE_STEPS.toFloat()
            lineTo(topLeft.x + w * t, topLeft.y + waveY(t) + dy)
        }
    }

    val clip = Path().apply {
        if (amplitude <= 0f) {
            addRoundRect(
                RoundRect(
                    Rect(topLeft, Size(w, h)),
                    CornerRadius(cornerRadius)
                )
            )
        } else {
            moveTo(topLeft.x, topLeft.y)
            lineTo(topLeft.x + w, topLeft.y)
            for (i in WAVE_STEPS downTo 0) {
                val t = i / WAVE_STEPS.toFloat()
                lineTo(topLeft.x + w * t, topLeft.y + waveY(t))
            }
            close()
        }
    }

    if (amplitude > 0f) {
        for (i in 1..3) {
            drawPath(
                bottomEdge(h * 0.004f * i),
                FlagBlack.copy(alpha = 0.05f / i),
                style = Stroke(width = h * 0.012f * i, cap = StrokeCap.Round)
            )
        }
    }

    clipPath(clip) {
        translate(topLeft.x, topLeft.y) {
            drawRect(FlagRed, Offset.Zero, Size(w, h / 2f))
            drawRect(FlagBlue, Offset(0f, h / 2f), Size(w, h / 2f))

            listOf(false, true).forEach { mirrored ->
                drawPath(
                    arm(mirrored),
                    FlagWhite,
                    style = Stroke(pallBand, cap = StrokeCap.Butt, join = StrokeJoin.Miter)
                )
            }
            listOf(false, true).forEach { mirrored ->
                drawPath(
                    arm(mirrored),
                    FlagGreen,
                    style = Stroke(greenBand, cap = StrokeCap.Butt, join = StrokeJoin.Miter)
                )
            }

            drawPath(triangle(goldEdge), FlagGold)
            drawPath(triangle(blackEdge), FlagBlack)

            if (amplitude > 0f && foldStrength > 0f) {
                val sliceWidth = w / WAVE_STEPS
                for (i in 0 until WAVE_STEPS) {
                    val t = (i + 0.5f) / WAVE_STEPS
                    val swing = sin(t * waveCount * 2f * PI.toFloat() + wavePhase)
                    val shade = if (swing < 0f) {
                        FlagBlack.copy(alpha = 0.14f * foldStrength * -swing)
                    } else {
                        FlagWhite.copy(alpha = 0.10f * foldStrength * swing)
                    }
                    drawRect(
                        brush = Brush.verticalGradient(
                            0f to Color.Transparent,
                            1f to shade,
                            startY = h * 0.25f,
                            endY = h
                        ),
                        topLeft = Offset(i * sliceWidth, 0f),
                        size = Size(sliceWidth + 1f, h)
                    )
                }
            }

            if (fadeInto.isSpecified) {
                drawRect(
                    brush = Brush.verticalGradient(
                        0.45f to Color.Transparent,
                        1f to fadeInto.copy(alpha = 0.55f),
                        startY = 0f,
                        endY = h
                    ),
                    topLeft = Offset.Zero,
                    size = Size(w, h)
                )
            }
        }
    }
}

@Composable
private fun WelcomeDecor(
    modifier: Modifier = Modifier,
    backgroundColor: Color = PageBackground
) {
    val transition = rememberInfiniteTransition(label = "flagWave")
    val wavePhase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 7000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wavePhase"
    )

    Canvas(modifier) {
        val w = size.width
        val h = size.height

        drawSouthAfricanFlag(
            topLeft = Offset.Zero,
            flagWidth = w,
            waveDepth = 0.08f,
            waveCount = 1.5f,
            wavePhase = wavePhase,
            foldStrength = 0.6f,
            fadeInto = backgroundColor
        )

        val ground = h * 0.012f
        val base = h - ground
        val sky = Color(0xFFC9D3E3).copy(alpha = 0.6f)
        val u = w / 20f
        val blocks = listOf(
            0f to 0.06f,
            1.5f to 0.04f,
            3f to 0.08f,
            4.2f to 0.05f,
            6.5f to 0.09f,
            9f to 0.05f,
            10.3f to 0.1f,
            12.5f to 0.06f,
            14.5f to 0.13f,
            15.2f to 0.07f,
            17f to 0.05f,
            18.5f to 0.04f
        )
        blocks.forEach { (x, bh) ->
            drawRect(sky, Offset(x * u, base - h * bh), Size(1.4f * u, h * bh))
        }
        drawCircle(sky, 1.4f * u, Offset(7.8f * u, base - h * 0.1f))
        drawRect(sky, Offset(7.3f * u, base - h * 0.1f), Size(1f * u, h * 0.1f))
        drawRect(sky, Offset(17.4f * u, base - h * 0.17f), Size(0.35f * u, h * 0.17f))
        drawCircle(sky, 0.7f * u, Offset(17.57f * u, base - h * 0.17f))
        drawRect(sky, Offset(0f, base), Size(w, ground))
    }
}
