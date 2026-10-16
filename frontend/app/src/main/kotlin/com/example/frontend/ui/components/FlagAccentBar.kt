package com.example.frontend.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.example.frontend.ui.theme.FlagBlack
import com.example.frontend.ui.theme.FlagBlue
import com.example.frontend.ui.theme.FlagGold
import com.example.frontend.ui.theme.FlagGreen
import com.example.frontend.ui.theme.FlagRed
import com.example.frontend.ui.theme.FlagWhite
import com.example.frontend.ui.theme.FrontendTheme

private val FlagColors = listOf(FlagBlack, FlagGold, FlagGreen, FlagWhite, FlagRed, FlagBlue)

@Composable
fun FlagAccentBar(
    modifier: Modifier = Modifier,
    height: Dp = 8.dp
) {
    Row(modifier = modifier.fillMaxWidth().height(height)) {
        FlagColors.forEach { color ->
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(color)
            ) {}
        }
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun FlagAccentBarPreview() {
    FrontendTheme {
        FlagAccentBar()
    }
}
