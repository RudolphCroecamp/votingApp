package com.example.frontend.ui.screens.PrivacyConsent

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.frontend.effects.entrance
import com.example.frontend.effects.rememberEntranceTrigger
import androidx.compose.runtime.getValue
import com.example.frontend.ui.Components.AppTopBar
import com.example.frontend.ui.Components.ButtonVariant
import com.example.frontend.ui.Components.DescriptionLabel
import com.example.frontend.ui.Components.HeadingLabel
import com.example.frontend.ui.Components.SimpleButton
import com.example.frontend.ui.Components.StatusLabel
import com.example.frontend.ui.Components.StatusType
import com.example.frontend.ui.screens.Registration.REGISTRATION_STEPS
import com.example.frontend.ui.screens.Registration.RegistrationUiEvent
import com.example.frontend.ui.screens.Registration.RegistrationUiState
import com.example.frontend.ui.screens.Registration.screens.RegisterStepHeader
import com.example.frontend.ui.theme.CardBorder
import com.example.frontend.ui.theme.FrontendTheme
import com.example.frontend.ui.theme.LocalMotionScale
import com.example.frontend.ui.theme.MutedText
import com.example.frontend.ui.theme.PageBackground
import com.example.frontend.ui.theme.Spacing

const val PRIVACY_CONSENT_STEP = 3

private const val CONSENT_STATEMENT =
    "I have read the above and I agree to the Electoral Commission collecting and " +
        "using my personal information for voter registration and voting."

data class ConsentDataItem(
    val icon: ImageVector,
    val label: String,
    val purpose: String,
    val retention: String
)

val CONSENT_DATA_ITEMS: List<ConsentDataItem> = listOf(
    ConsentDataItem(
        icon = Icons.Default.AccountBox,
        label = "Your ID number",
        purpose = "To check you are on the voters' roll and that you only vote once.",
        retention = "Kept while you stay registered to vote."
    ),
    ConsentDataItem(
        icon = Icons.Default.PhotoCamera,
        label = "A photo of your ID or passport",
        purpose = "To confirm the ID number you gave us belongs to a real document.",
        retention = "Deleted once your registration is approved, within 30 days."
    ),
    ConsentDataItem(
        icon = Icons.Default.Face,
        label = "A scan of your face",
        purpose = "To match your face to your ID photo, and to sign you in instead of a password.",
        retention = "Deleted once your registration is approved, within 30 days."
    ),
    ConsentDataItem(
        icon = Icons.Default.Home,
        label = "Your home address",
        purpose = "To place you in the right voting district, which decides your ballots.",
        retention = "Kept while you stay registered to vote."
    ),
    ConsentDataItem(
        icon = Icons.Default.Phone,
        label = "Your cellphone number",
        purpose = "To send the one-time code that confirms it is you signing in.",
        retention = "Kept while you stay registered to vote."
    )
)

@Composable
fun PrivacyConsentScreen(
    uiState: RegistrationUiState,
    onEvent: (RegistrationUiEvent) -> Unit,
    onContinue: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    dataItems: List<ConsentDataItem> = CONSENT_DATA_ITEMS,
    backgroundColor: Color = PageBackground
) {
    val entered by rememberEntranceTrigger()

    Scaffold(
        modifier = modifier,
        containerColor = backgroundColor,
        topBar = {
            AppTopBar(
                title = "Privacy and consent",
                subtitle = "Step $PRIVACY_CONSENT_STEP of $REGISTRATION_STEPS",
                onBackClick = onBackClick
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.screen, vertical = Spacing.card),
            verticalArrangement = Arrangement.spacedBy(Spacing.item)
        ) {
            RegisterStepHeader(
                step = PRIVACY_CONSENT_STEP,
                heading = "What we collect and why",
                description = "South Africa's privacy law (POPIA) says we must tell you this " +
                    "and get your agreement before we collect anything.",
                modifier = Modifier.entrance(entered, index = 0)
            )

            StatusLabel(
                message = "No photos are taken and no face is scanned until you agree below.",
                type = StatusType.Info,
                modifier = Modifier
                    .fillMaxWidth()
                    .entrance(entered, index = 1)
            )

            dataItems.forEachIndexed { index, item ->
                ConsentDataRow(
                    item = item,
                    modifier = Modifier
                        .fillMaxWidth()
                        .entrance(entered, index = index + 2)
                )
            }

            DescriptionLabel(
                text = "We never sell your information and we never share who you voted for. " +
                    "Your ballot is stored separately from your identity, so no one can link " +
                    "the two. You may withdraw your agreement or ask us to delete your data at " +
                    "any time in Settings, though you cannot vote without it.",
                modifier = Modifier
                    .fillMaxWidth()
                    .entrance(entered, index = dataItems.size + 2),
                color = MutedText
            )

            ConsentCheckbox(
                checked = uiState.hasConsented,
                onCheckedChange = { onEvent(RegistrationUiEvent.OnConsentChanged(it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .entrance(entered, index = dataItems.size + 3)
            )

            SimpleButton(
                label = "Continue",
                onClick = onContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .entrance(entered, index = dataItems.size + 4),
                variant = ButtonVariant.Tertiary,
                trailingIcon = Icons.AutoMirrored.Filled.ArrowForward,
                enabled = uiState.canSubmitConsent
            )

            if (!uiState.hasConsented) {
                DescriptionLabel(
                    text = "Tick the box above to continue.",
                    modifier = Modifier.fillMaxWidth(),
                    color = MutedText,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun ConsentDataRow(
    item: ConsentDataItem,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Row(
            modifier = Modifier.padding(Spacing.card),
            horizontalArrangement = Arrangement.spacedBy(Spacing.item)
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.tight)) {
                HeadingLabel(
                    text = item.label,
                    style = MaterialTheme.typography.titleSmall
                )
                DescriptionLabel(text = item.purpose)
                DescriptionLabel(
                    text = "How long we keep it: ${item.retention}",
                    color = MutedText,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun ConsentCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.toggleable(
            value = checked,
            onValueChange = onCheckedChange,
            role = Role.Checkbox
        ),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            if (checked) 1.5.dp else 1.dp,
            if (checked) MaterialTheme.colorScheme.primary else CardBorder
        )
    ) {
        Row(
            modifier = Modifier.padding(Spacing.item),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.small)
        ) {
            Checkbox(checked = checked, onCheckedChange = null)
            Text(
                text = CONSENT_STATEMENT,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun PrivacyConsentPreview(
    hasConsented: Boolean,
    darkTheme: Boolean = false
) {
    FrontendTheme(darkTheme = darkTheme) {
        CompositionLocalProvider(LocalMotionScale provides 0f) {
            PrivacyConsentScreen(
                uiState = RegistrationUiState(hasConsented = hasConsented),
                onEvent = {},
                onContinue = {},
                onBackClick = {}
            )
        }
    }
}

@Preview(name = "1 - Not agreed", showBackground = true, showSystemUi = true, device = Devices.PIXEL_7)
@Composable
private fun PrivacyConsentScreenPreview() {
    PrivacyConsentPreview(hasConsented = false)
}

@Preview(name = "2 - Agreed", showBackground = true, showSystemUi = true, device = Devices.PIXEL_7)
@Composable
private fun PrivacyConsentScreenAgreedPreview() {
    PrivacyConsentPreview(hasConsented = true)
}

@Preview(
    name = "3 - Dark",
    showBackground = true,
    showSystemUi = true,
    device = Devices.PIXEL_7,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun PrivacyConsentScreenDarkPreview() {
    PrivacyConsentPreview(hasConsented = true, darkTheme = true)
}

@Preview(name = "4 - Full page", showBackground = true, widthDp = 412, heightDp = 1500)
@Composable
private fun PrivacyConsentScreenFullPreview() {
    PrivacyConsentPreview(hasConsented = false)
}

@Preview(name = "5 - Large font", showBackground = true, widthDp = 412, heightDp = 1500, fontScale = 1.5f)
@Composable
private fun PrivacyConsentScreenLargeFontPreview() {
    PrivacyConsentPreview(hasConsented = false)
}
