package com.example.frontend.ui.screens.Registration.screens

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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.frontend.ui.Components.ButtonVariant
import com.example.frontend.ui.Components.DescriptionLabel
import com.example.frontend.ui.Components.HeadingLabel
import com.example.frontend.ui.Components.StatusLabel
import com.example.frontend.ui.Components.SectionHeading
import com.example.frontend.ui.Components.SimpleButton
import com.example.frontend.ui.Components.StatusType
import com.example.frontend.ui.Components.SuccessCheckmarkBadge
import com.example.frontend.ui.theme.FrontendTheme
import com.example.frontend.ui.theme.MutedText
import com.example.frontend.ui.theme.PageBackground
import com.example.frontend.ui.theme.Spacing
@Composable
fun RegisterDoneScreen(
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
    voterName: String = "Voter",
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
                step = 4,
                heading = "You're Registered",
                description = "Your details, ID document and face scan have been submitted for verification."
            )

            Spacer(modifier = Modifier.height(Spacing.section))

            //#TODO: put the checkmark badge, heading and description in a box

            SuccessCheckmarkBadge()

            Spacer(modifier = Modifier.height(Spacing.small))

            SectionHeading(
                title = "Welcome, $voterName",
                description = "You can now sign in with your ID number and a face scan.",
                titleStyle = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(Spacing.section))

            RegistrationFact(
                icon = Icons.Default.Lock,
                title = "Your privacy is protected",
                description = "Your identity is secure and your vote stays anonymous."
            )
            RegistrationFact(
                icon = Icons.Default.HowToVote,
                title = "One person. One vote.",
                description = "You can only vote once in each election."
            )

            StatusLabel(
                message = "Keep your ID number handy. You will need it every time you sign in.",
                type = StatusType.Info,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(Spacing.section))

            SimpleButton(
                label = "Continue to App",
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
