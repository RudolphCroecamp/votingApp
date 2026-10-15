package com.example.frontend.ui.screens.Auth

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.example.frontend.R
import com.example.frontend.effects.entrance
import com.example.frontend.effects.rememberEntranceTrigger
import androidx.compose.ui.unit.dp
import com.example.frontend.ui.Components.AppTextField
import com.example.frontend.ui.Components.BrandHeader
import com.example.frontend.ui.Components.ButtonVariant
import com.example.frontend.ui.Components.DescriptionLabel
import com.example.frontend.ui.Components.HeadingLabel
import com.example.frontend.ui.Components.StatusLabel
import com.example.frontend.ui.Components.SectionHeading
import com.example.frontend.ui.Components.SimpleButton
import com.example.frontend.ui.Components.StatusType
import com.example.frontend.ui.theme.FrontendTheme
import com.example.frontend.ui.theme.Spacing
import com.example.frontend.ui.theme.MutedText
import com.example.frontend.ui.theme.PageBackground

@Composable
fun LoginScreen(
    onFaceVerification: (idNumber: String) -> Unit,
    onRegister: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = PageBackground
) {
    val entered by rememberEntranceTrigger()
    var idNumber by remember { mutableStateOf("") }

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
                title = stringResource(R.string.brand_title),
                subtitle = stringResource(R.string.brand_tagline),
                logoSize = 72.dp,
                modifier = Modifier.entrance(entered, index = 0)
            )

            Spacer(modifier = Modifier.height(Spacing.small))

            SectionHeading(
                modifier = Modifier.entrance(entered, index = 1),
                title = stringResource(R.string.login_title),
                description = stringResource(R.string.login_description)
            )

            AppTextField(
                value = idNumber,
                onValueChange = { idNumber = it },
                placeholder = stringResource(R.string.field_id_or_passport),
                modifier = Modifier
                    .fillMaxWidth()
                    .entrance(entered, index = 2),
                leadingIcon = Icons.Default.AccountBox,
                maxLength = ID_NUMBER_MAX_LENGTH
            )

            SimpleButton(
                label = stringResource(R.string.login_with_face),
                onClick = { onFaceVerification(idNumber) },
                modifier = Modifier
                    .fillMaxWidth()
                    .entrance(entered, index = 3),
                variant = ButtonVariant.Tertiary,
                leadingIcon = Icons.Default.CenterFocusStrong,
                enabled = idNumber.isNotBlank()
            )

            StatusLabel(
                message = stringResource(R.string.login_face_info),
                type = StatusType.Info,
                modifier = Modifier
                    .fillMaxWidth()
                    .entrance(entered, index = 4)
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                DescriptionLabel(
                    text = stringResource(R.string.login_no_account),
                    color = MutedText,
                    textAlign = TextAlign.Center
                )
                TextButton(onClick = onRegister) {
                    Text(
                        text = stringResource(R.string.login_register),
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
            onFaceVerification = {},
            onRegister = {}
        )
    }
}
