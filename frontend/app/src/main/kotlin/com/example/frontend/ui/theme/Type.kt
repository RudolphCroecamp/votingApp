package com.example.frontend.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Single place the app's typeface is chosen. Swap this for a bundled
 * `FontFamily(Font(R.font.<name>))` to move the whole app onto a brand font.
 */
val AppFontFamily = FontFamily.SansSerif

private fun appTextStyle(
    size: Int,
    lineHeight: Int,
    weight: FontWeight,
    letterSpacing: Double = 0.0
) = TextStyle(
    fontFamily = AppFontFamily,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    letterSpacing = letterSpacing.sp
)

/**
 * Scale taken from the home screen: bold navy headings, compact muted body copy
 * and small all-caps labels for status chips.
 */
val Typography = Typography(
    headlineLarge = appTextStyle(30, 36, FontWeight.Bold, -0.4),
    headlineMedium = appTextStyle(28, 34, FontWeight.Bold, -0.4),
    headlineSmall = appTextStyle(25, 31, FontWeight.Bold, -0.3),
    titleLarge = appTextStyle(20, 26, FontWeight.Bold, -0.2),
    titleMedium = appTextStyle(17, 23, FontWeight.SemiBold),
    titleSmall = appTextStyle(14, 19, FontWeight.SemiBold),
    bodyLarge = appTextStyle(16, 23, FontWeight.Normal, 0.1),
    bodyMedium = appTextStyle(14, 20, FontWeight.Normal, 0.1),
    bodySmall = appTextStyle(12, 16, FontWeight.Normal, 0.1),
    labelLarge = appTextStyle(15, 20, FontWeight.SemiBold, 0.1),
    labelMedium = appTextStyle(13, 17, FontWeight.SemiBold, 0.1),
    labelSmall = appTextStyle(11, 14, FontWeight.Bold, 0.6)
)
