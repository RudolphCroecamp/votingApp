package com.example.frontend.ui.Components

import androidx.annotation.StringRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.HowToVote
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.frontend.R
import com.example.frontend.ui.theme.FrontendTheme
import com.example.frontend.ui.theme.InfoContainer
import com.example.frontend.ui.theme.MotionDurations
import com.example.frontend.ui.theme.MutedText
import com.example.frontend.ui.theme.StandardEasing
import com.example.frontend.ui.theme.motionDuration
import com.example.frontend.ui.theme.pressSpring

enum class BottomTab(@param:StringRes val labelRes: Int, val icon: ImageVector) {
    Home(R.string.tab_home, Icons.Default.Home),
    Vote(R.string.tab_vote, Icons.Default.HowToVote),
    Info(R.string.tab_info, Icons.Default.Info),
    Settings(R.string.tab_settings, Icons.Default.Settings)
}

/**
 * Flat navigation bar: the active tab goes navy on a soft tinted pill, the
 * rest stay muted. No raised action, matching the home design.
 */
@Composable
fun AppBottomBar(
    selected: BottomTab,
    onSelect: (BottomTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(64.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomTab.entries.forEach { tab ->
                val active = tab == selected
                val label = stringResource(tab.labelRes)
                val colorSpec = tween<androidx.compose.ui.graphics.Color>(
                    durationMillis = motionDuration(MotionDurations.medium),
                    easing = StandardEasing
                )
                val tint by animateColorAsState(
                    targetValue = if (active) MaterialTheme.colorScheme.primary else MutedText,
                    animationSpec = colorSpec,
                    label = "tabTint"
                )
                val pill by animateColorAsState(
                    targetValue = if (active) InfoContainer else MaterialTheme.colorScheme.surface,
                    animationSpec = colorSpec,
                    label = "tabPill"
                )
                // A small pop so switching tabs registers without a layout change.
                val iconScale by animateFloatAsState(
                    targetValue = if (active) 1.08f else 1f,
                    animationSpec = pressSpring(),
                    label = "tabIconScale"
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable { onSelect(tab) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .background(color = pill, shape = MaterialTheme.shapes.small)
                            .padding(horizontal = 14.dp, vertical = 3.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = label,
                            tint = tint,
                            modifier = Modifier
                                .size(22.dp)
                                .graphicsLayer {
                                    scaleX = iconScale
                                    scaleY = iconScale
                                }
                        )
                    }
                    Text(
                        text = label,
                        color = tint,
                        style = if (active) {
                            MaterialTheme.typography.labelSmall
                        } else {
                            MaterialTheme.typography.bodySmall
                        }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun AppBottomBarPreview() {
    FrontendTheme {
        AppBottomBar(selected = BottomTab.Home, onSelect = {})
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun AppBottomBarVotePreview() {
    FrontendTheme {
        AppBottomBar(selected = BottomTab.Vote, onSelect = {})
    }
}
