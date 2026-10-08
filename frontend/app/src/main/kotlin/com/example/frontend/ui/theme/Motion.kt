package com.example.frontend.ui.theme

import android.provider.Settings
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode

/**
 * One place every duration and curve in the app comes from.
 *
 * Everything here is deliberately short and runs on properties the render
 * thread can handle on its own (alpha, scale, translation, colour), so nothing
 * re-lays-out mid-animation. On a device with animations turned down or off —
 * which is common on low-end hardware and battery savers — [LocalMotionScale]
 * collapses to 0 and every helper below snaps straight to its end state.
 */
object MotionDurations {
    /** Press feedback and other sub-perceptual responses. */
    const val fast = 110

    /** The default: entrances, colour changes, content swaps. */
    const val medium = 220

    /** Reserved for the one or two larger moments, such as the success badge. */
    const val slow = 420

    /** Gap between items in a staggered list entrance. */
    const val stagger = 45

    /** Items past this index skip the stagger, so long lists never feel slow. */
    const val maxStaggeredItems = 6
}

/** Decelerating curve for things entering the screen. */
val EnterEasing: Easing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)

/** Symmetric curve for state changes already on screen. */
val StandardEasing: Easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)

/** Low-bounce spring used for press feedback; cheaper to tune than a tween. */
fun <T> pressSpring(): SpringSpec<T> = spring(
    dampingRatio = Spring.DampingRatioNoBouncy,
    stiffness = Spring.StiffnessMediumLow
)

/**
 * The system animator duration scale: 1 normally, 0 when the user (or a
 * battery saver) has switched animations off, and 0.5 on "fast" settings.
 */
val LocalMotionScale = compositionLocalOf { 1f }

/** True when animations should actually play. */
val motionEnabled: Boolean
    @Composable
    @ReadOnlyComposable
    get() = LocalMotionScale.current > 0f

/** Scales a duration by the system setting, collapsing to 0 when motion is off. */
@Composable
@ReadOnlyComposable
fun motionDuration(base: Int): Int = (base * LocalMotionScale.current).toInt()

/** Reads the animator duration scale once per context. Previews always animate. */
@Composable
fun rememberSystemMotionScale(): Float {
    if (LocalInspectionMode.current) return 1f
    val resolver = LocalContext.current.contentResolver
    return remember(resolver) {
        runCatching {
            Settings.Global.getFloat(resolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f)
        }.getOrDefault(1f).coerceIn(0f, 2f)
    }
}
