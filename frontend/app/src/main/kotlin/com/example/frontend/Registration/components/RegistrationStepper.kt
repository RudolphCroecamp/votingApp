package com.example.frontend.ui.Registration.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RegistrationStepper(currentStep: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (step in 1..4) {
            val isActive = step <= currentStep
            val bgColor = if (isActive) Color(0xFF0F2D59) else Color(0xFFE2E8F0)
            val textColor = if (isActive) Color.White else Color(0xFF64748B)

            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(bgColor, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = step.toString(),
                    color = textColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (step < 4) {
                Box(
                    modifier = Modifier
                        .width(32.dp)
                        .height(2.dp)
                        .background(if (step < currentStep) Color(0xFF0F2D59) else Color(0xFFCBD5E1))
                )
            }
        }
    }
}