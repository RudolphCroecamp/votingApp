package com.example.frontend.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Verified
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.frontend.R
import com.example.frontend.ui.theme.FrontendTheme
import com.example.frontend.ui.theme.HeaderChipBackground
import com.example.frontend.ui.theme.Spacing

/**
 * Navy home header: flag accent bar, greeting, verified-voter chip and the
 * language / help affordances on the trailing edge.
 */
@Composable
fun HomeHeader(
    greeting: String,
    modifier: Modifier = Modifier,
    verifiedLabel: String = stringResource(R.string.home_verified_voter),
    language: String = "EN",
    onLanguageClick: () -> Unit = {},
    onHelpClick: () -> Unit = {},
    trailingContent: @Composable () -> Unit = {}
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
    ) {
        Column(Modifier.statusBarsPadding()) {
            FlagAccentBar(height = 5.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = Spacing.screen,
                        end = Spacing.screen,
                        top = Spacing.card,
                        bottom = Spacing.section
                    ),
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(Spacing.small)
                ) {
                    Text(
                        text = greeting,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    VerifiedChip(label = verifiedLabel)
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.small)
                ) {
                    LanguagePill(language = language, onClick = onLanguageClick)
                    Box {
                        HeaderIconButton(onClick = onHelpClick)
                        trailingContent()
                    }
                }
            }
        }
    }
}

@Composable
private fun VerifiedChip(label: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = HeaderChipBackground,
        contentColor = MaterialTheme.colorScheme.onPrimary
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Verified,
                contentDescription = null,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun LanguagePill(language: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(50))
            .clickable(onClick = onClick, role = Role.Button),
        shape = RoundedCornerShape(50),
        color = HeaderChipBackground,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        border = BorderStroke(1.dp, HeaderChipBackground)
    ) {
        Row(
            modifier = Modifier.padding(start = 12.dp, end = 8.dp, top = 7.dp, bottom = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(text = language, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Icon(
                imageVector = Icons.Default.ExpandMore,
                contentDescription = stringResource(R.string.language_change),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun HeaderIconButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
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
            imageVector = Icons.AutoMirrored.Filled.HelpOutline,
            contentDescription = stringResource(R.string.action_help),
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(19.dp)
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun HomeHeaderPreview() {
    FrontendTheme {
        HomeHeader(greeting = "Hi, Thandi")
    }
}
