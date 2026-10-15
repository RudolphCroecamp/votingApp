package com.example.frontend.ui.screens.Settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.frontend.R
import com.example.frontend.effects.entrance
import com.example.frontend.effects.pressScale
import com.example.frontend.effects.rememberEntranceTrigger
import com.example.frontend.ui.Components.AppBottomBar
import com.example.frontend.ui.Components.AppTopBar
import com.example.frontend.ui.Components.BottomTab
import com.example.frontend.ui.Components.SectionHeading
import com.example.frontend.ui.theme.BrandNavy
import com.example.frontend.ui.theme.DividerColor
import com.example.frontend.ui.theme.ErrorContainer
import com.example.frontend.ui.theme.ErrorContent
import com.example.frontend.ui.theme.FrontendTheme
import com.example.frontend.ui.theme.InfoContainer
import com.example.frontend.ui.theme.InfoContent
import com.example.frontend.ui.theme.MutedText
import com.example.frontend.ui.theme.PageBackground
import com.example.frontend.ui.theme.Spacing
import com.example.frontend.ui.theme.SuccessContainer
import com.example.frontend.ui.theme.SuccessContent
import com.example.frontend.ui.theme.WarningContainer
import com.example.frontend.ui.theme.WarningContent

/** Supported languages for the language picker dialog. */
private val SupportedLanguages = listOf(
    "English (ZA)",
    "isiZulu",
    "isiXhosa",
    "Afrikaans",
    "Sepedi",
    "Setswana",
    "Sesotho"
)

@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    onProfileClick: () -> Unit = {},
    onCheckRegistrationClick: () -> Unit = {},
    onVotingInformationClick: () -> Unit = {},
    onPrivacyClick: () -> Unit = {},
    onHelpClick: () -> Unit = {},
    onReportProblemClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    selectedTab: BottomTab = BottomTab.Settings,
    onTabSelected: (BottomTab) -> Unit = {},
    backgroundColor: Color = PageBackground
) {
    val entered by rememberEntranceTrigger()

    // Preferences local states
    var selectedLanguage by rememberSaveable { mutableStateOf("English (ZA)") }
    var notificationsEnabled by rememberSaveable { mutableStateOf(true) }
    var faceVerificationEnabled by rememberSaveable { mutableStateOf(true) }

    // Dialog visibilities
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        containerColor = backgroundColor,
        topBar = {
            AppTopBar(
                title = stringResource(R.string.tab_settings),
                subtitle = stringResource(R.string.settings_subtitle),
                onBackClick = onBackClick
            )
        },
        bottomBar = { AppBottomBar(selected = selectedTab, onSelect = onTabSelected) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.screen, vertical = Spacing.card),
            verticalArrangement = Arrangement.spacedBy(Spacing.section)
        ) {
            // Section 1: Account & Registration
            Column(
                modifier = Modifier.entrance(entered, index = 0),
                verticalArrangement = Arrangement.spacedBy(Spacing.small)
            ) {
                SectionHeading(
                    title = stringResource(R.string.settings_account_title),
                    description = stringResource(R.string.settings_account_description)
                )
                SettingActionRow(
                    icon = Icons.Default.Person,
                    title = stringResource(R.string.settings_profile_title),
                    description = stringResource(R.string.settings_profile_description),
                    onClick = onProfileClick,
                    iconTint = InfoContent,
                    iconBackground = InfoContainer
                )
                SettingActionRow(
                    icon = Icons.Default.AssignmentTurnedIn,
                    title = stringResource(R.string.settings_registration_title),
                    description = stringResource(R.string.settings_registration_description),
                    onClick = onCheckRegistrationClick,
                    iconTint = SuccessContent,
                    iconBackground = SuccessContainer
                )
            }

            // Section 2: App Preferences
            Column(
                modifier = Modifier.entrance(entered, index = 1),
                verticalArrangement = Arrangement.spacedBy(Spacing.small)
            ) {
                SectionHeading(
                    title = stringResource(R.string.settings_preferences_title),
                    description = stringResource(R.string.settings_preferences_description)
                )
                SettingValueRow(
                    icon = Icons.Default.Language,
                    title = stringResource(R.string.settings_language_title),
                    value = selectedLanguage,
                    description = stringResource(R.string.settings_language_description),
                    onClick = { showLanguageDialog = true },
                    iconTint = WarningContent,
                    iconBackground = WarningContainer
                )
                SettingToggleRow(
                    icon = Icons.Default.Notifications,
                    title = stringResource(R.string.settings_reminders_title),
                    description = stringResource(R.string.settings_reminders_description),
                    checked = notificationsEnabled,
                    onCheckedChange = { notificationsEnabled = it },
                    iconTint = InfoContent,
                    iconBackground = InfoContainer
                )
            }

            // Section 3: Security & Privacy
            Column(
                modifier = Modifier.entrance(entered, index = 2),
                verticalArrangement = Arrangement.spacedBy(Spacing.small)
            ) {
                SectionHeading(
                    title = stringResource(R.string.settings_security_title),
                    description = stringResource(R.string.settings_security_description)
                )
                SettingToggleRow(
                    icon = Icons.Default.Face,
                    title = stringResource(R.string.settings_face_title),
                    description = stringResource(R.string.settings_face_description),
                    checked = faceVerificationEnabled,
                    onCheckedChange = { faceVerificationEnabled = it },
                    iconTint = SuccessContent,
                    iconBackground = SuccessContainer
                )
                SettingActionRow(
                    icon = Icons.Default.Shield,
                    title = stringResource(R.string.settings_privacy_title),
                    description = stringResource(R.string.settings_privacy_description),
                    onClick = onPrivacyClick,
                    iconTint = SuccessContent,
                    iconBackground = SuccessContainer
                )
            }

            // Section 4: Help & Information
            Column(
                modifier = Modifier.entrance(entered, index = 3),
                verticalArrangement = Arrangement.spacedBy(Spacing.small)
            ) {
                SectionHeading(
                    title = stringResource(R.string.settings_help_section_title),
                    description = stringResource(R.string.settings_help_section_description)
                )
                SettingActionRow(
                    icon = Icons.AutoMirrored.Filled.HelpOutline,
                    title = stringResource(R.string.menu_help_support),
                    description = stringResource(R.string.settings_help_description),
                    onClick = onHelpClick,
                    iconTint = WarningContent,
                    iconBackground = WarningContainer
                )
                SettingActionRow(
                    icon = Icons.AutoMirrored.Filled.MenuBook,
                    title = stringResource(R.string.settings_election_info_title),
                    description = stringResource(R.string.settings_election_info_description),
                    onClick = onVotingInformationClick,
                    iconTint = WarningContent,
                    iconBackground = WarningContainer
                )
                SettingActionRow(
                    icon = Icons.Default.ReportProblem,
                    title = stringResource(R.string.settings_report_title),
                    description = stringResource(R.string.settings_report_description),
                    onClick = onReportProblemClick,
                    iconTint = ErrorContent,
                    iconBackground = ErrorContainer
                )
            }

            // Section 5: Log Out
            Column(
                modifier = Modifier.entrance(entered, index = 4),
                verticalArrangement = Arrangement.spacedBy(Spacing.small)
            ) {
                SettingActionRow(
                    icon = Icons.AutoMirrored.Filled.Logout,
                    title = stringResource(R.string.menu_log_out),
                    description = stringResource(R.string.settings_logout_description),
                    onClick = { showLogoutDialog = true },
                    iconTint = ErrorContent,
                    iconBackground = ErrorContainer
                )
            }

            // Footer / App Info
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .entrance(entered, index = 5),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Spacing.tight)
            ) {
                HorizontalDivider(color = DividerColor)
                Spacer(modifier = Modifier.height(Spacing.tight))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MutedText,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = stringResource(R.string.settings_footer_iec),
                        style = MaterialTheme.typography.labelMedium,
                        color = MutedText
                    )
                }
                Text(
                    text = stringResource(R.string.settings_footer_version),
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedText,
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    // Language Picker Dialog
    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.settings_language_dialog_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = BrandNavy
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.tight)) {
                    SupportedLanguages.forEach { language ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedLanguage = language
                                    showLanguageDialog = false
                                }
                                .padding(vertical = Spacing.small, horizontal = Spacing.tight),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(Spacing.small)
                        ) {
                            RadioButton(
                                selected = (language == selectedLanguage),
                                onClick = {
                                    selectedLanguage = language
                                    showLanguageDialog = false
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = BrandNavy)
                            )
                            Text(
                                text = language,
                                style = MaterialTheme.typography.bodyMedium,
                                color = BrandNavy
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text(stringResource(R.string.action_cancel), color = BrandNavy)
                }
            }
        )
    }

    // Logout Confirmation Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = null,
                    tint = ErrorContent,
                    modifier = Modifier.size(28.dp)
                )
            },
            title = {
                Text(
                    text = stringResource(R.string.settings_logout_confirm_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = BrandNavy
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.settings_logout_confirm_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MutedText
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        onLogoutClick()
                    }
                ) {
                    Text(stringResource(R.string.menu_log_out), color = ErrorContent, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(stringResource(R.string.action_cancel), color = BrandNavy)
                }
            }
        )
    }
}

/** Tappable preference row leading to a screen or dialog action. */
@Composable
private fun SettingActionRow(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconTint: Color = InfoContent,
    iconBackground: Color = InfoContainer
) {
    val interactionSource = remember { MutableInteractionSource() }

    Card(
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = modifier
            .fillMaxWidth()
            .pressScale(interactionSource),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.item, vertical = Spacing.item),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.item)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(iconBackground, RoundedCornerShape(9.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(19.dp)
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = BrandNavy
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedText
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MutedText,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/** Preference row displaying a selected value (e.g. active language). */
@Composable
private fun SettingValueRow(
    icon: ImageVector,
    title: String,
    value: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconTint: Color = InfoContent,
    iconBackground: Color = InfoContainer
) {
    val interactionSource = remember { MutableInteractionSource() }

    Card(
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = modifier
            .fillMaxWidth()
            .pressScale(interactionSource),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.item, vertical = Spacing.item),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.item)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(iconBackground, RoundedCornerShape(9.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(19.dp)
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = BrandNavy
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedText
                )
            }
            Surface(
                color = InfoContainer,
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.labelMedium,
                    color = BrandNavy,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MutedText,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/** Toggle preference row with a switch control. */
@Composable
private fun SettingToggleRow(
    icon: ImageVector,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    iconTint: Color = InfoContent,
    iconBackground: Color = InfoContainer
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.item, vertical = Spacing.item),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.item)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(iconBackground, RoundedCornerShape(9.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(19.dp)
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = BrandNavy
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedText
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = BrandNavy
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    FrontendTheme {
        SettingsScreen(onBackClick = {})
    }
}
