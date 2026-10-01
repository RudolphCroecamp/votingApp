package com.example.frontend.ui.Components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.frontend.ui.theme.BrandGreen

@Composable
fun SimpleButton(
    //this defines the variables that will later be used to accept arguments
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = BrandGreen,
    contentColor: Color = Color.White,
    enabled: Boolean = true,
    border: BorderStroke? = null
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        border = border,
        shape = RoundedCornerShape(5.dp)
    ) {
        Text(label)
    }
}

@Preview(showBackground = true)
@Composable
private fun SimpleButtonPreview() {
    SimpleButton(
        label = "Confirm Vote",
        onClick = {},
        modifier = Modifier.fillMaxWidth()
    )
}
