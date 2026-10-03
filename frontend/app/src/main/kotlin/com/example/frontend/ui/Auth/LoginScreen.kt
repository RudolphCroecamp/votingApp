package com.example.frontend.ui.Auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.frontend.ui.Components.AppTextField
import com.example.frontend.ui.Components.BrandHeader
import com.example.frontend.ui.Components.ButtonVariant
import com.example.frontend.ui.Components.DescriptionLabel
import com.example.frontend.ui.Components.HeadingLabel
import com.example.frontend.ui.Components.SimpleButton
import com.example.frontend.ui.theme.CardBorder
import com.example.frontend.ui.theme.FrontendTheme
import com.example.frontend.ui.theme.Spacing
import com.example.frontend.ui.theme.MutedText
import com.example.frontend.ui.theme.PageBackground

@Composable
fun LoginScreen(
    onSignIn: (idNumber: String, password: String) -> Unit,
    onFaceVerification: () -> Unit,
    onRegister: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = PageBackground
) {
    var idNumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Scaffold(
        modifier = modifier,
        containerColor = backgroundColor
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.screen, vertical = Spacing.section),
            verticalArrangement = Arrangement.spacedBy(Spacing.item),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BrandHeader(
                title = "South Africa\nNational Voting App",
                subtitle = "Secure  ·  Anonymous  ·  Verifiable",
                logoSize = 72.dp
            )

            Spacer(modifier = Modifier.height(Spacing.small))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(Spacing.tight)
            ) {
                HeadingLabel(text = "Sign In", modifier = Modifier.fillMaxWidth())
                DescriptionLabel(
                    text = "Use your South African ID or passport to access your account.",
                    modifier = Modifier.fillMaxWidth(),
                    color = MutedText
                )
            }

            AppTextField(
                value = idNumber,
                onValueChange = { idNumber = it },
                placeholder = "ID Number / Passport Number",
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = Icons.Default.AccountBox
            )

            AppTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = "Password",
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = Icons.Default.Lock,
                isPassword = true
            )

            SimpleButton(
                label = "Sign In",
                onClick = { onSignIn(idNumber, password) },
                modifier = Modifier.fillMaxWidth(),
                variant = ButtonVariant.Tertiary
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.tight),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.item)
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = CardBorder)
                DescriptionLabel(text = "or", color = MutedText)
                HorizontalDivider(modifier = Modifier.weight(1f), color = CardBorder)
            }

            SimpleButton(
                label = "Sign in with Face Verification",
                onClick = onFaceVerification,
                modifier = Modifier.fillMaxWidth(),
                variant = ButtonVariant.Secondary,
                leadingIcon = Icons.Default.CenterFocusStrong
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                DescriptionLabel(
                    text = "Don't have an account?",
                    color = MutedText,
                    textAlign = TextAlign.Center
                )
                TextButton(onClick = onRegister) {
                    Text(
                        text = "Register",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginScreenPreview() {
    FrontendTheme {
        LoginScreen(
            onSignIn = { _, _ -> },
            onFaceVerification = {},
            onRegister = {}
        )
    }
}
