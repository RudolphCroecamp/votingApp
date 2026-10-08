package com.example.frontend.ui.Components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.frontend.ui.theme.BrandNavy
import com.example.frontend.ui.theme.FrontendTheme
import com.example.frontend.ui.theme.GoldAccent

/** Gold disc holding a small navy icon. Used on every forward-moving call to action. */
@Composable
fun GoldIconBadge(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    size: Dp = 28.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .background(GoldAccent, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = BrandNavy,
            modifier = Modifier.size(size * 0.6f)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun GoldIconBadgePreview() {
    FrontendTheme {
        GoldIconBadge(icon = Icons.AutoMirrored.Filled.ArrowForward)
    }
}
