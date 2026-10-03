package com.example.frontend.ui.Components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.frontend.ui.theme.FrontendTheme
import com.example.frontend.ui.theme.SuccessBorder

/** Filled circle with a drawn check mark, so it stays sharp at any size. */
@Composable
fun SuccessCheckmarkBadge(
    modifier: Modifier = Modifier,
    size: Dp = 84.dp,
    circleColor: Color = SuccessBorder,
    checkColor: Color = Color.White
) {
    Box(
        modifier = modifier
            .size(size)
            .background(color = circleColor, shape = CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size * 0.5f)) {
            val width = this.size.width
            val height = this.size.height
            val path = Path().apply {
                moveTo(width * 0.04f, height * 0.52f)
                lineTo(width * 0.38f, height * 0.86f)
                lineTo(width * 0.96f, height * 0.16f)
            }
            drawPath(
                path = path,
                color = checkColor,
                style = Stroke(
                    width = width * 0.16f,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SuccessCheckmarkBadgePreview() {
    FrontendTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SuccessCheckmarkBadge()
            SuccessCheckmarkBadge(size = 48.dp)
            SuccessCheckmarkBadge(size = 24.dp)
        }
    }
}
