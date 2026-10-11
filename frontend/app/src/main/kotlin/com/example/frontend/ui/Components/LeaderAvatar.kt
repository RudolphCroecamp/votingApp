package com.example.frontend.ui.Components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.frontend.ui.theme.CardBorder
import com.example.frontend.ui.theme.FrontendTheme
import com.example.frontend.ui.theme.SurfaceSunken

/**
 * Round leader portrait (page 3: crop + clip + border).
 * Falls back to initials while we have no leader photos in res/.
 */
@Composable
fun LeaderAvatar(
    name: String,
    modifier: Modifier = Modifier,
    @DrawableRes photo: Int? = null,
    size: Dp = 40.dp
) {
    val circle = modifier
        .size(size)
        .clip(CircleShape)
        .border(BorderStroke(1.dp, CardBorder), CircleShape)

    if (photo != null) {
        Image(
            painter = painterResource(photo),
            contentDescription = "Photo of $name",
            contentScale = ContentScale.Crop, // fill the circle, trim the edges
            modifier = circle
        )
    } else {
        Box(
            modifier = circle.background(SurfaceSunken),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initialsOf(name),
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
                fontSize = (size.value * 0.36f).sp
            )
        }
    }
}

private fun initialsOf(name: String): String =
    name.split(" ", "-")
        .filter { it.isNotBlank() }
        .let { parts -> listOfNotNull(parts.firstOrNull(), parts.drop(1).lastOrNull()) }
        .joinToString("") { it.first().uppercase() }
        .ifEmpty { "?" }

@Preview(showBackground = true)
@Composable
private fun LeaderAvatarPreview() {
    FrontendTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            LeaderAvatar(name = "Cyril Ramaphosa")
            LeaderAvatar(name = "Geordin Hill-Lewis", size = 72.dp)
        }
    }
}
