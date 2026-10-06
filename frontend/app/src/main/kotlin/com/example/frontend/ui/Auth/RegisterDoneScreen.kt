package com.example.frontend.ui.Auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.example.frontend.ui.Components.ButtonVariant
import com.example.frontend.ui.Components.DescriptionLabel
import com.example.frontend.ui.Components.HeadingLabel
import com.example.frontend.ui.Components.IconLabel
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
                .padding(horizontal = Spacing.screen, vertical = Spacing.section),
            verticalArrangement = Arrangement.spacedBy(Spacing.item),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(1f))

            RegisterStepHeader(
                step = 3,
                heading = "You're Registered",
                description = "Your details and face scan have been submitted for verification."
            )

            Spacer(modifier = Modifier.height(Spacing.section))

            SuccessCheckmarkBadge()

            Spacer(modifier = Modifier.height(Spacing.small))

            HeadingLabel(
                text = "Welcome, $voterName",
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )
            DescriptionLabel(
                text = "You can now sign in with your ID number and a face scan.",
                modifier = Modifier.fillMaxWidth(),
                color = MutedText,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.weight(1f))

            IconLabel(
                message = "Keep your ID number handy. You will need it every time you sign in.",
                type = StatusType.Info,
                modifier = Modifier.fillMaxWidth()
            )

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

@Preview(showBackground = true)
@Composable
private fun RegisterDoneScreenPreview() {
    FrontendTheme {
        RegisterDoneScreen(onContinue = {})
    }
}
