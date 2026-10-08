package com.example.frontend.ui.Components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.frontend.effects.pressScale
import com.example.frontend.ui.theme.FrontendTheme

/**
 * Primary = green (go), Tertiary = navy (neutral forward), Secondary = navy outline.
 * Trailing icons render inside a gold disc, matching the home hero call to action.
 */
enum class ButtonVariant { Primary, Secondary, Tertiary }

private val ButtonPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
private val ButtonMinHeight = 50.dp

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
    val content: @Composable () -> Unit = {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = if (trailingIcon != null) {
                Arrangement.SpaceBetween
            } else {
                Arrangement.Center
            }
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (leadingIcon != null) {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        modifier = Modifier.size(ButtonDefaults.IconSize)
                    )
                    Spacer(Modifier.width(ButtonDefaults.IconSpacing))
                }
                Text(text = label, style = MaterialTheme.typography.labelLarge)
            }
            if (trailingIcon != null) {
                GoldIconBadge(icon = trailingIcon)
            }
        }
    }

    val shape = MaterialTheme.shapes.medium
    val interactionSource = remember { MutableInteractionSource() }
    val buttonModifier = modifier
        .heightIn(min = ButtonMinHeight)
        .pressScale(interactionSource)

    when (variant) {
        ButtonVariant.Primary -> Button(
            onClick = onClick,
            modifier = buttonModifier,
            enabled = enabled,
            shape = shape,
            interactionSource = interactionSource,
            contentPadding = ButtonPadding,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = Color.White
            )
        ) { content() }

        ButtonVariant.Secondary -> {
            val accent = MaterialTheme.colorScheme.primary
            OutlinedButton(
                onClick = onClick,
                modifier = buttonModifier,
                enabled = enabled,
                shape = shape,
                interactionSource = interactionSource,
                contentPadding = ButtonPadding,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = accent),
                border = BorderStroke(1.5.dp, if (enabled) accent else accent.copy(alpha = 0.12f))
            ) { content() }
        }

        ButtonVariant.Tertiary -> Button(
            onClick = onClick,
            modifier = buttonModifier,
            enabled = enabled,
            shape = shape,
            interactionSource = interactionSource,
            contentPadding = ButtonPadding,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) { content() }
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun SimpleButtonPreview() {
    FrontendTheme {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SimpleButton("Vote now", {}, Modifier.fillMaxWidth(), trailingIcon = Icons.AutoMirrored.Filled.ArrowForward)
            SimpleButton("Confirm vote", {}, Modifier.fillMaxWidth())
            SimpleButton(
                "Change selection", {}, Modifier.fillMaxWidth(),
                variant = ButtonVariant.Secondary,
                leadingIcon = Icons.Default.CheckCircle
            )
            SimpleButton("Continue", {}, Modifier.fillMaxWidth(), variant = ButtonVariant.Tertiary)
            SimpleButton("Disabled", {}, Modifier.fillMaxWidth(), enabled = false)
        }
    }
}
