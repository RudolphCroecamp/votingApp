package com.example.frontend.ui.Components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.frontend.R
import com.example.frontend.ui.theme.FrontendTheme
import com.example.frontend.ui.theme.Spacing
import com.example.frontend.ui.theme.SuccessBorder
import com.example.frontend.ui.theme.SuccessContainer
import com.example.frontend.ui.theme.SuccessContent

@Composable
fun VoteNowCard(
    title: String,
    status: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    logo: Painter = painterResource(R.drawable.ic_ballot_logo),
    buttonLabel: String = "Vote Now",
    enabled: Boolean = true,
    containerColor: Color = SuccessContainer,
    contentColor: Color = SuccessContent,
    borderColor: Color = SuccessBorder
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.card),
            verticalArrangement = Arrangement.spacedBy(Spacing.card)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.card)
            ) {
                Image(
                    painter = logo,
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                    contentScale = ContentScale.Fit
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(Spacing.tight)
                ) {
                    HeadingLabel(
                        text = title,
                        color = contentColor,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = contentColor,
                            modifier = Modifier.size(16.dp)
                        )
                        DescriptionLabel(
                            text = status,
                            color = contentColor,
                            style = MaterialTheme.typography.labelLarge,
                            maxLines = 1
                        )
                    }
                }
            }

            SimpleButton(
                label = buttonLabel,
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
                variant = ButtonVariant.Primary,
                trailingIcon = Icons.AutoMirrored.Filled.ArrowForward,
                enabled = enabled
            )
        }
    }
}

@Preview(name = "Vote now card", showBackground = true, widthDp = 360)
@Composable
private fun VoteNowCardPreview() {
    FrontendTheme {
        VoteNowCard(
            title = "2026 National Election",
            status = "Voting is open",
            onClick = {},
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}

@Preview(name = "Vote now card closed", showBackground = true, widthDp = 360)
@Composable
private fun VoteNowCardClosedPreview() {
    FrontendTheme {
        VoteNowCard(
            title = "2026 National Election",
            status = "Voting opens 12 May",
            onClick = {},
            enabled = false,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}
