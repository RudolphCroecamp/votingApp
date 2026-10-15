package com.example.frontend.ui.Components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.frontend.R
import com.example.frontend.ui.theme.FrontendTheme
import com.example.frontend.ui.theme.HeaderChipBackground
import com.example.frontend.ui.theme.Spacing

/**
 * Navy top bar sharing the home header's language: flag accent strip, bold
 * title, optional supporting line and circular translucent controls.
 */
@Composable
fun AppTopBar(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onBackClick: (() -> Unit)? = null,
    backContentDescription: String = stringResource(R.string.action_back),
    actions: @Composable RowScope.() -> Unit = {}
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
    ) {
        Column(Modifier.statusBarsPadding()) {
            FlagAccentBar(height = 5.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = Spacing.item,
                        vertical = Spacing.item
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.small)
            ) {
                if (onBackClick != null) {
                    HeaderCircleButton(
                        onClick = onBackClick,
                        contentDescription = backContentDescription
                    )
                } else {
                    Spacer(Modifier.width(Spacing.small))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f)
                        )
                    }
                }
                actions()
            }
        }
    }
}

@Composable
private fun HeaderCircleButton(
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    Box(
        // The disc stays 34dp visually; the tappable area is padded out to the
        // 48dp accessibility minimum.
        modifier = modifier
            .minimumInteractiveComponentSize()
            .size(34.dp)
            .clip(CircleShape)
            .background(HeaderChipBackground)
            .clickable(onClick = onClick, role = Role.Button),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(19.dp)
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun AppTopBarPreview() {
    FrontendTheme {
        AppTopBar(title = "Review your vote", onBackClick = {})
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun AppTopBarSubtitlePreview() {
    FrontendTheme {
        AppTopBar(
            title = "National Assembly",
            subtitle = "Step 2 of 3",
            onBackClick = {}
        )
    }
}
