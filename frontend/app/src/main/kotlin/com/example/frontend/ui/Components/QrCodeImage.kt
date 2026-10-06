package com.example.frontend.ui.Components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.frontend.ui.theme.FrontendTheme
import kotlin.random.Random

private const val QR_MODULES = 25
private const val FINDER_SIZE = 7

@Composable
fun QrCodeImage(
    content: String,
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
    color: Color = MaterialTheme.colorScheme.primary
) {
    val modules = remember(content) { buildModules(content) }

    Canvas(modifier = modifier.size(size)) {
        val cell = this.size.width / QR_MODULES
        for (row in 0 until QR_MODULES) {
            for (column in 0 until QR_MODULES) {
                if (modules[row][column]) {
                    drawRoundRect(
                        color = color,
                        topLeft = Offset(column * cell, row * cell),
                        size = Size(cell, cell),
                        cornerRadius = CornerRadius(cell * 0.2f)
                    )
                }
            }
        }
    }
}

private fun buildModules(content: String): Array<BooleanArray> {
    val random = Random(content.hashCode())
    val modules = Array(QR_MODULES) { BooleanArray(QR_MODULES) { random.nextBoolean() } }

    fun finderAt(top: Int, left: Int) {
        for (row in -1..FINDER_SIZE) {
            for (column in -1..FINDER_SIZE) {
                val r = top + row
                val c = left + column
                if (r !in 0 until QR_MODULES || c !in 0 until QR_MODULES) continue
                val inside = row in 0 until FINDER_SIZE && column in 0 until FINDER_SIZE
                val ring = row == 0 || row == FINDER_SIZE - 1 || column == 0 || column == FINDER_SIZE - 1
                val core = row in 2..4 && column in 2..4
                modules[r][c] = inside && (ring || core)
            }
        }
    }

    finderAt(0, 0)
    finderAt(0, QR_MODULES - FINDER_SIZE)
    finderAt(QR_MODULES - FINDER_SIZE, 0)
    return modules
}

@Preview(showBackground = true)
@Composable
private fun QrCodeImagePreview() {
    FrontendTheme {
        QrCodeImage(content = "AB7F-92K4-6ZP1-3QX8")
    }
}
