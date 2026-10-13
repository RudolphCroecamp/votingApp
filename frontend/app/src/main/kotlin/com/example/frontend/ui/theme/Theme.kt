package com.example.frontend.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = BrandNavy,
    onPrimary = Color.White,
    primaryContainer = InfoContainer,
    onPrimaryContainer = BrandNavy,
    secondary = BrandGreen,
    onSecondary = Color.White,
    secondaryContainer = SuccessContainer,
    onSecondaryContainer = SuccessContent,
    tertiary = GoldAccent,
    onTertiary = BrandNavy,
    tertiaryContainer = GoldContainer,
    onTertiaryContainer = GoldContent,
    background = PageBackground,
    onBackground = BrandNavy,
    surface = Color.White,
    onSurface = BrandNavy,
    surfaceVariant = SurfaceGrey,
    onSurfaceVariant = MutedText,
    outline = CardBorder,
    outlineVariant = DividerColor,
    error = ErrorBorder,
    onError = Color.White,
    errorContainer = ErrorContainer,
    onErrorContainer = ErrorContent
)

private val DarkColorScheme = darkColorScheme(
    primary = NavyTint,
    onPrimary = BrandNavyDeep,
    primaryContainer = BrandNavy,
    onPrimaryContainer = Color.White,
    secondary = GreenTint,
    onSecondary = BrandNavyDeep,
    secondaryContainer = BrandGreen,
    onSecondaryContainer = Color.White,
    tertiary = GoldAccent,
    onTertiary = BrandNavyDeep,
    background = DarkPageBackground,
    onBackground = Color.White,
    surface = DarkSurface,
    onSurface = Color.White,
    surfaceVariant = DarkSurface,
    onSurfaceVariant = DarkMutedText,
    outline = DarkCardBorder,
    outlineVariant = DarkCardBorder,
    error = ErrorBorder,
    onError = Color.White
)

@Composable
fun FrontendTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    CompositionLocalProvider(LocalMotionScale provides rememberSystemMotionScale()) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = AppShapes,
            content = content
        )
    }
}
