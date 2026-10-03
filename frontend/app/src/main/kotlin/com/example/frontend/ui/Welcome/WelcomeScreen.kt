package com.example.frontend.ui.Welcome

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import com.example.frontend.ui.theme.*
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.frontend.ui.Components.BrandHeader
import com.example.frontend.ui.Components.ButtonVariant
import com.example.frontend.ui.Components.DescriptionLabel
import com.example.frontend.ui.Components.SimpleButton
import com.example.frontend.ui.theme.FrontendTheme
import com.example.frontend.ui.theme.Spacing
import com.example.frontend.ui.theme.PageBackground

@Composable
fun WelcomeScreen(
    onGetStarted: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = PageBackground
) {
    Scaffold(
        modifier = modifier,
        containerColor = backgroundColor,
    ) { innerPadding ->
        Box(Modifier.fillMaxSize()) {
        WelcomeDecor(Modifier.fillMaxSize())
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = Spacing.screen, vertical = Spacing.section),
            verticalArrangement = Arrangement.spacedBy(Spacing.section),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(1f))

            BrandHeader(
                title = "South Africa\nNational Voting App",
                logoSize = 120.dp
            )
            DescriptionLabel(
                text = "Your vote. Your voice.\nA stronger South Africa.",
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.weight(1f))

            SimpleButton(
                label = "Get Started",
                onClick = onGetStarted,
                modifier = Modifier.fillMaxWidth(),
                variant = ButtonVariant.Tertiary
            )
        }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun WelcomeScreenPreview() {
    FrontendTheme {
        WelcomeScreen(onGetStarted = {})
    }
}

private fun Path.poly(vararg p: Pair<Float, Float>) {
    moveTo(p[0].first, p[0].second)
    for (i in 1 until p.size) lineTo(p[i].first, p[i].second)
    close()
}

/** Decorative South African flag in the top-left corner and a city skyline silhouette near the bottom. */
@Composable
private fun WelcomeDecor(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val fh = h * 0.13f
        fun fill(c: Color, vararg p: Pair<Float, Float>) =
            drawPath(Path().apply { poly(*p) }, c)
        // flag, sheared strips
        fill(FlagRed, 0f to 0f, w * 0.75f to 0f, w * 0.75f to fh * 0.25f, w * 0.38f to fh * 0.55f, 0f to fh * 0.55f)
        fill(FlagBlue, 0f to fh * 0.8f, w * 0.38f to fh * 0.8f, w * 0.7f to fh * 1.05f, w * 0.45f to fh * 1.25f, 0f to fh * 1.25f)
        fill(FlagWhite, 0f to fh * 0.3f, w * 0.4f to fh * 0.3f, w * 0.78f to fh * 0.05f, w * 0.78f to fh * 0.2f, w * 0.42f to fh * 0.58f, 0f to fh * 0.58f)
                fill(FlagGreen, 0f to fh * 0.22f, w * 0.36f to fh * 0.22f, w * 0.72f to 0f, w * 0.85f to 0f, w * 0.4f to fh * 0.4f, w * 0.4f to fh * 0.8f, w * 0.85f to fh * 1.1f, w * 0.72f to fh * 1.1f, w * 0.36f to fh * 0.8f, 0f to fh * 0.8f)
        fill(FlagGold, 0f to fh * 0.3f, w * 0.34f to fh * 0.3f, w * 0.34f to fh * 0.72f, 0f to fh * 0.72f)
        fill(FlagBlack, 0f to fh * 0.2f, w * 0.3f to fh * 0.5f, 0f to fh * 0.8f)
        // skyline
        val base = h * 0.84f
        val sky = Color(0xFFC9D3E3)
        val u = w / 20f
        val blocks = listOf(
            0f to 0.06f, 1.5f to 0.04f, 3f to 0.08f, 4.2f to 0.05f, 6.5f to 0.09f,
            9f to 0.05f, 10.3f to 0.1f, 12.5f to 0.06f, 14.5f to 0.13f, 15.2f to 0.07f, 17f to 0.05f, 18.5f to 0.04f
        )
        blocks.forEach { (x, bh) ->
            drawRect(sky, Offset(x * u, base - h * bh), Size(1.4f * u, h * bh))
        }
        // dome, tower, spire
        drawCircle(sky, 1.4f * u, Offset(7.8f * u, base - h * 0.1f))
        drawRect(sky, Offset(7.3f * u, base - h * 0.1f), Size(1f * u, h * 0.1f))
        drawRect(sky, Offset(17.4f * u, base - h * 0.17f), Size(0.35f * u, h * 0.17f))
        drawCircle(sky, 0.7f * u, Offset(17.57f * u, base - h * 0.17f))
        drawRect(sky, Offset(0f, base), Size(w, h * 0.012f))
    }
}
