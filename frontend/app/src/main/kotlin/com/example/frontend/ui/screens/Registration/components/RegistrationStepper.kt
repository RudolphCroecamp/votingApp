package com.example.frontend.ui.screens.Registration.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.frontend.ui.screens.Registration.REGISTRATION_STEPS
import com.example.frontend.ui.theme.CardBorder
import com.example.frontend.ui.theme.FrontendTheme
import com.example.frontend.ui.theme.MutedText
import com.example.frontend.ui.theme.SurfaceGrey

@Composable
fun RegistrationStepper(
    currentStep: Int,
    modifier: Modifier = Modifier,
    totalSteps: Int = REGISTRATION_STEPS
) {
    val accent = MaterialTheme.colorScheme.primary

    Row(
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = "Step $currentStep of $totalSteps" },
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (step in 1..totalSteps) {
            val isActive = step <= currentStep

            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(if (isActive) accent else SurfaceGrey, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = step.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isActive) MaterialTheme.colorScheme.onPrimary else MutedText
                )
            }

            if (step < totalSteps) {
                Box(
                    modifier = Modifier
                        .width(32.dp)
                        .height(2.dp)
                        .background(if (step < currentStep) accent else CardBorder)
                )
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
