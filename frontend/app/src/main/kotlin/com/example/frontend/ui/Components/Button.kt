package com.example.frontend.ui.Components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.frontend.ui.theme.FrontendTheme

enum class ButtonVariant { Primary, Secondary, Tertiary }

@Composable
fun SimpleButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ButtonVariant = ButtonVariant.Primary,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    enabled: Boolean = true
) {
    val content: @Composable RowScope.() -> Unit = {
        if (leadingIcon != null) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                modifier = Modifier.size(ButtonDefaults.IconSize)
            )
            Spacer(Modifier.width(ButtonDefaults.IconSpacing))
        }
        Text(label)
        if (trailingIcon != null) {
            Spacer(Modifier.width(ButtonDefaults.IconSpacing))
            Icon(
                imageVector = trailingIcon,
                contentDescription = null,
                modifier = Modifier.size(ButtonDefaults.IconSize)
            )
        }
    }

    when (variant) {
        ButtonVariant.Primary -> Button(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            shape = MaterialTheme.shapes.small,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary
            ),
            content = content
        )

        ButtonVariant.Secondary -> {
            val accent = MaterialTheme.colorScheme.primary
            OutlinedButton(
                onClick = onClick,
                modifier = modifier,
                enabled = enabled,
                shape = MaterialTheme.shapes.small,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = accent),
                border = BorderStroke(1.5.dp, if (enabled) accent else accent.copy(alpha = 0.12f)),
                content = content
            )
        }

        ButtonVariant.Tertiary -> Button(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            shape = MaterialTheme.shapes.small,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            content = content
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SimpleButtonPreview() {
    FrontendTheme {
        SimpleButton(
            label = "Confirm Vote",
            onClick = {},
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SimpleButtonSecondaryIconPreview() {
    FrontendTheme {
        SimpleButton(
            label = "Change Selection",
            onClick = {},
            modifier = Modifier.fillMaxWidth(),
            variant = ButtonVariant.Secondary,
            leadingIcon = Icons.Default.CheckCircle
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SimpleButtonDisabledPreview() {
    FrontendTheme {
        SimpleButton(
            label = "Confirm Vote",
            onClick = {},
            modifier = Modifier.fillMaxWidth(),
            enabled = false
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SimpleButtonTertiaryPreview() {
    FrontendTheme {
        SimpleButton(
            label = "Confirm Vote",
            onClick = {},
            modifier = Modifier.fillMaxWidth(),
            variant = ButtonVariant.Tertiary
        )
    }
}
