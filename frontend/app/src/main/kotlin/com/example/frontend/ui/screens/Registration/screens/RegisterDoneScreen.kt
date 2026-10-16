package com.example.frontend.ui.screens.Registration.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HowToVote
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.frontend.R
import com.example.frontend.ui.components.ButtonVariant
import com.example.frontend.ui.components.DescriptionLabel
import com.example.frontend.ui.components.HeadingLabel
import com.example.frontend.ui.components.StatusLabel
import com.example.frontend.ui.components.SectionHeading
import com.example.frontend.ui.components.SimpleButton
import com.example.frontend.ui.components.StatusType
import com.example.frontend.ui.components.SuccessCheckmarkBadge
import com.example.frontend.ui.theme.CardBorder
import com.example.frontend.ui.theme.FrontendTheme
import com.example.frontend.ui.theme.MutedText
import com.example.frontend.ui.theme.PageBackground
import com.example.frontend.ui.theme.Spacing
@Composable
fun RegisterDoneScreen(
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
    voterName: String = "",
    backgroundColor: Color = PageBackground
) {
    Scaffold(
        modifier = modifier,
        containerColor = backgroundColor
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.screen, vertical = Spacing.section),
            verticalArrangement = Arrangement.spacedBy(Spacing.item),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            RegisterStepHeader(
                step = 6,
                heading = stringResource(R.string.register_done_heading),
                description = stringResource(R.string.register_done_description)
            )

            Spacer(modifier = Modifier.height(Spacing.section))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, CardBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.card, vertical = Spacing.section),
                    verticalArrangement = Arrangement.spacedBy(Spacing.item),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    SuccessCheckmarkBadge()

                    SectionHeading(
                        title = stringResource(
                            R.string.register_done_welcome,
                            voterName.ifBlank { stringResource(R.string.voter_fallback_name) }
                        ),
                        description = stringResource(R.string.register_done_sign_in_hint),
                        titleStyle = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.section))

            RegistrationFact(
                icon = Icons.Default.Lock,
                title = stringResource(R.string.register_done_privacy_title),
                description = stringResource(R.string.register_done_privacy_description)
            )
            RegistrationFact(
                icon = Icons.Default.HowToVote,
                title = stringResource(R.string.register_done_one_vote_title),
                description = stringResource(R.string.register_done_one_vote_description)
            )

            StatusLabel(
                message = stringResource(R.string.register_done_keep_id),
                type = StatusType.Info,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(Spacing.section))

            SimpleButton(
                label = stringResource(R.string.register_done_continue),
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth(),
                variant = ButtonVariant.Tertiary
            )

            Spacer(modifier = Modifier.height(Spacing.section))
        }
    }
}

@Composable
private fun RegistrationFact(
    icon: ImageVector,
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.item),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.tight)) {
            HeadingLabel(text = title, style = MaterialTheme.typography.titleSmall)
            DescriptionLabel(
                text = description,
                color = MutedText,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RegisterDoneScreenPreview() {
    FrontendTheme {
        RegisterDoneScreen(onContinue = {}, voterName = "Adriaan van Wyk")
    }
}
