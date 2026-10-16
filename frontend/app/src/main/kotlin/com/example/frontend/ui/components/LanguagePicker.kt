package com.example.frontend.ui.components

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.os.LocaleListCompat
import com.example.frontend.R
import com.example.frontend.ui.theme.FrontendTheme
import com.example.frontend.ui.theme.Spacing

/**
 * Languages the app ships translations for (res/values, values-af, values-zu, values-xh).
 * [nativeName] is deliberately not translated: people look for their own language by
 * the name they know it by.
 */
enum class AppLanguage(val tag: String, val nativeName: String, val shortCode: String) {
    English("en", "English", "EN"),
    Afrikaans("af", "Afrikaans", "AF"),
    Zulu("zu", "isiZulu", "ZU"),
    Xhosa("xh", "isiXhosa", "XH");

    companion object {
        /** Language for a BCP-47 tag such as "zu-ZA"; anything unsupported falls back to English. */
        fun fromTag(tag: String?): AppLanguage {
            val language = tag?.substringBefore('-')?.substringBefore('_')?.lowercase()
            return entries.firstOrNull { it.tag == language } ?: English
        }
    }
}

/** The language the UI is currently drawn in. Recomposes when the locale changes. */
@Composable
fun currentAppLanguage(): AppLanguage {
    val locales = LocalConfiguration.current.locales
    return AppLanguage.fromTag(if (locales.isEmpty) null else locales[0].toLanguageTag())
}

/**
 * Switches the whole app to [language]. AppCompat recreates the activity, so every
 * stringResource re-reads in the new language, and remembers the choice across restarts.
 */
fun applyAppLanguage(language: AppLanguage) {
    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(language.tag))
}

@Composable
fun LanguagePickerDialog(
    current: AppLanguage,
    onSelect: (AppLanguage) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.settings_language_dialog_title),
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier.selectableGroup(),
                verticalArrangement = Arrangement.spacedBy(Spacing.tight)
            ) {
                AppLanguage.entries.forEach { language ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(role = Role.RadioButton) { onSelect(language) }
                            .padding(vertical = Spacing.small, horizontal = Spacing.tight),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Spacing.small)
                    ) {
                        // The row handles the click so the whole line is one touch target.
                        RadioButton(selected = language == current, onClick = null)
                        Text(
                            text = language.nativeName,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    )
}

/**
 * Picker wired to the real app locale: selecting a language applies it and closes.
 * Picking the language already in use just closes the dialog.
 */
@Composable
fun AppLanguagePickerDialog(onDismiss: () -> Unit) {
    val current = currentAppLanguage()
    LanguagePickerDialog(
        current = current,
        onSelect = { language ->
            onDismiss()
            if (language != current) applyAppLanguage(language)
        },
        onDismiss = onDismiss
    )
}

@Preview(showBackground = true)
@Composable
private fun LanguagePickerDialogPreview() {
    FrontendTheme {
        LanguagePickerDialog(current = AppLanguage.Zulu, onSelect = {}, onDismiss = {})
    }
}
