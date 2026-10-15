package com.example.frontend.ui.screens.Registration.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.frontend.R
import com.example.frontend.ui.screens.Registration.REGISTRATION_STEPS
import com.example.frontend.ui.theme.DividerColor
import com.example.frontend.ui.theme.FrontendTheme
import com.example.frontend.ui.theme.MotionDurations
import com.example.frontend.ui.theme.MutedText
import com.example.frontend.ui.theme.StandardEasing
import com.example.frontend.ui.theme.motionDuration
import com.example.frontend.ui.theme.pressSpring
import com.example.frontend.ui.theme.SurfaceSunken

@Composable
fun RegistrationStepper(
    currentStep: Int,
    modifier: Modifier = Modifier,
    totalSteps: Int = REGISTRATION_STEPS
) {
    val accent = MaterialTheme.colorScheme.primary
    val stepDescription = stringResource(R.string.register_step_of, currentStep, totalSteps)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = stepDescription },
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (step in 1..totalSteps) {
            val isActive = step <= currentStep
            val isCurrent = step == currentStep
            val stepColor by animateColorAsState(
                targetValue = if (isActive) accent else SurfaceSunken,
                animationSpec = tween(
                    motionDuration(MotionDurations.medium),
                    easing = StandardEasing
                ),
                label = "stepColor$step"
            )
            // Only the step you are on grows, so the eye lands on it.
            val stepScale by animateFloatAsState(
                targetValue = if (isCurrent) 1.12f else 1f,
                animationSpec = pressSpring(),
                label = "stepScale$step"
            )

            Box(
                modifier = Modifier
                    .size(28.dp)
                    .graphicsLayer {
                        scaleX = stepScale
                        scaleY = stepScale
                    }
                    .background(stepColor, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = step.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isActive) MaterialTheme.colorScheme.onPrimary else MutedText
                )
            }

            if (step < totalSteps) {
                val connectorFill by animateFloatAsState(
                    targetValue = if (step < currentStep) 1f else 0f,
                    animationSpec = tween(
                        motionDuration(MotionDurations.medium),
                        easing = StandardEasing
                    ),
                    label = "connector$step"
                )
                Box(
                    modifier = Modifier
                        .width(32.dp)
                        .height(2.dp)
                        .background(DividerColor)
                ) {
                    // Scales a solid bar from the left rather than animating a
                    // width, which would re-measure the row on every frame.
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                scaleX = connectorFill
                                transformOrigin = TransformOrigin(0f, 0.5f)
                            }
                            .background(accent)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 320)
@Composable
private fun RegistrationStepperPreview() {
    FrontendTheme {
        RegistrationStepper(currentStep = 2)
    }
}
