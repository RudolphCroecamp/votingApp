package com.example.frontend.ui.Components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.HowToVote
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import com.example.frontend.ui.theme.FrontendTheme
import com.example.frontend.ui.theme.MutedText

enum class BottomTab(val label: String, val icon: ImageVector) {
    Home("Home", Icons.Default.Home),
    Vote("Vote", Icons.Default.HowToVote),
    Info("Info", Icons.Default.Info),
    Settings("Settings", Icons.Default.Settings)
}

@Composable
fun AppBottomBar(
    selected: BottomTab,
    onSelect: (BottomTab) -> Unit,
    modifier: Modifier = Modifier
) {
    androidx.compose.material3.Surface(
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
                val raised = active && tab == BottomTab.Vote
                val tint = if (active) MaterialTheme.colorScheme.secondary else MutedText
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable { onSelect(tab) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (raised) {
                        Box(
                            modifier = Modifier
                                .offset(y = (-14).dp)
                                .size(52.dp)
                                .shadow(6.dp, CircleShape)
                                .background(MaterialTheme.colorScheme.secondary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(tab.icon, tab.label, tint = Color.White, modifier = Modifier.size(26.dp))
                        }
                        Text(
                            tab.label, color = tint, fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.offset(y = (-12).dp)
                        )
                    } else {
                        Icon(tab.icon, tab.label, tint = tint, modifier = Modifier.size(24.dp))
                        Text(
                            tab.label, color = tint, fontSize = 12.sp,
                            fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
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
