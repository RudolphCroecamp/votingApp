package com.example.frontend.ui.screens.Support

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.frontend.BuildConfig
import com.example.frontend.R
import com.example.frontend.ui.components.AppBottomBar
import com.example.frontend.ui.components.AppTextField
import com.example.frontend.ui.components.AppTopBar
import com.example.frontend.ui.components.BottomTab
import com.example.frontend.ui.components.ButtonVariant
import com.example.frontend.ui.components.DescriptionLabel
import com.example.frontend.ui.components.SectionHeading
import com.example.frontend.ui.components.SimpleButton
import com.example.frontend.ui.components.StatusLabel
import com.example.frontend.ui.components.StatusType
import com.example.frontend.ui.theme.CardBorder
import com.example.frontend.ui.theme.FrontendTheme
import com.example.frontend.ui.theme.MutedText
import com.example.frontend.ui.theme.PageBackground
import com.example.frontend.ui.theme.Spacing

const val REPORT_MESSAGE_MIN_LENGTH = 10
const val REPORT_MESSAGE_MAX_LENGTH = 1000

private val EMAIL_PATTERN = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")

enum class ProblemCategory(@param:StringRes val labelRes: Int, val subjectTag: String) {
    APP(R.string.report_category_app, "App issue"),
    VOTING(R.string.report_category_voting, "Voting concern"),
    ACCOUNT(R.string.report_category_account, "Account & login"),
    OTHER(R.string.report_category_other, "Other")
}

data class ProblemReport(val subject: String, val body: String)

enum class ReportStatus { Idle, MessageTooShort, EmailInvalid, Opened, NoEmailApp }

fun buildProblemReport(
    category: ProblemCategory,
    message: String,
    replyEmail: String,
    deviceInfo: String
): ProblemReport = ProblemReport(
    subject = "[VotingApp] ${category.subjectTag}",
    body = buildString {
        appendLine(message.trim())
        appendLine()
        appendLine("---")
        if (replyEmail.isNotBlank()) appendLine("Reply to: ${replyEmail.trim()}")
        append(deviceInfo)
    }
)

fun validateReport(message: String, replyEmail: String): ReportStatus = when {
    message.trim().length < REPORT_MESSAGE_MIN_LENGTH -> ReportStatus.MessageTooShort
    replyEmail.isNotBlank() && !EMAIL_PATTERN.matches(replyEmail.trim()) -> ReportStatus.EmailInvalid
    else -> ReportStatus.Idle
}

fun deviceInfo(): String =
    "App ${BuildConfig.VERSION_NAME} · Android ${Build.VERSION.RELEASE} · ${Build.MANUFACTURER} ${Build.MODEL}"

private fun openEmailApp(context: Context, report: ProblemReport): Boolean {
    val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:")).apply {
        putExtra(Intent.EXTRA_EMAIL, arrayOf(BuildConfig.SUPPORT_EMAIL))
        putExtra(Intent.EXTRA_SUBJECT, report.subject)
        putExtra(Intent.EXTRA_TEXT, report.body)
    }
    return try {
        context.startActivity(intent)
        true
    } catch (e: ActivityNotFoundException) {
        false
    }
}

@Composable
fun ReportProblemScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    selectedTab: BottomTab = BottomTab.Settings,
    onTabSelected: (BottomTab) -> Unit = {},
    onSend: (ProblemReport) -> Boolean = { true },
    backgroundColor: Color = PageBackground
) {
    var category by rememberSaveable { mutableStateOf(ProblemCategory.APP) }
    var message by rememberSaveable { mutableStateOf("") }
    var replyEmail by rememberSaveable { mutableStateOf("") }
    var status by rememberSaveable { mutableStateOf(ReportStatus.Idle) }

    Scaffold(
        modifier = modifier,
        containerColor = backgroundColor,
        topBar = {
            AppTopBar(
                title = stringResource(R.string.report_title),
                subtitle = stringResource(R.string.report_subtitle),
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
            verticalArrangement = Arrangement.spacedBy(Spacing.item)
        ) {
            SectionHeading(
                title = stringResource(R.string.report_heading),
                description = stringResource(R.string.report_description)
            )

            DescriptionLabel(
                text = stringResource(R.string.report_category_label),
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.labelLarge
            )

            ProblemCategory.entries.chunked(2).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.item)
                ) {
                    row.forEach { option ->
                        CategoryChip(
                            label = stringResource(option.labelRes),
                            isSelected = option == category,
                            onClick = { category = option },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            AppTextField(
                value = message,
                onValueChange = {
                    message = it
                    if (status != ReportStatus.Opened) status = ReportStatus.Idle
                },
                placeholder = stringResource(R.string.report_message_placeholder),
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = Icons.Default.Edit,
                maxLength = REPORT_MESSAGE_MAX_LENGTH,
                minLines = 5,
                errorText = if (status == ReportStatus.MessageTooShort) {
                    stringResource(R.string.report_message_too_short)
                } else {
                    null
                }
            )

            DescriptionLabel(
                text = stringResource(R.string.report_counter, message.length, REPORT_MESSAGE_MAX_LENGTH),
                modifier = Modifier.fillMaxWidth(),
                color = MutedText,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.End
            )

            AppTextField(
                value = replyEmail,
                onValueChange = {
                    replyEmail = it
                    if (status != ReportStatus.Opened) status = ReportStatus.Idle
                },
                placeholder = stringResource(R.string.report_email_placeholder),
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = Icons.Default.Email,
                keyboardType = KeyboardType.Email,
                errorText = if (status == ReportStatus.EmailInvalid) {
                    stringResource(R.string.issue_email_invalid)
                } else {
                    null
                }
            )

            StatusLabel(
                message = stringResource(R.string.report_device_note),
                type = StatusType.Info,
                modifier = Modifier.fillMaxWidth()
            )

            when (status) {
                ReportStatus.Opened -> StatusLabel(
                    message = stringResource(R.string.report_opened),
                    type = StatusType.Success,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { liveRegion = LiveRegionMode.Polite }
                )
                ReportStatus.NoEmailApp -> StatusLabel(
                    message = stringResource(R.string.report_no_email_app, BuildConfig.SUPPORT_EMAIL),
                    type = StatusType.Warning,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { liveRegion = LiveRegionMode.Polite }
                )
                else -> Unit
            }

            SimpleButton(
                label = stringResource(R.string.report_send),
                onClick = {
                    val problem = validateReport(message, replyEmail)
                    status = if (problem != ReportStatus.Idle) {
                        problem
                    } else if (onSend(buildProblemReport(category, message, replyEmail, deviceInfo()))) {
                        ReportStatus.Opened
                    } else {
                        ReportStatus.NoEmailApp
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                variant = ButtonVariant.Tertiary,
                leadingIcon = Icons.AutoMirrored.Filled.Send
            )
        }
    }
}

@Composable
fun rememberEmailSender(): (ProblemReport) -> Boolean {
    val context = LocalContext.current
    return { report -> openEmailApp(context, report) }
}

@Composable
private fun CategoryChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accent = MaterialTheme.colorScheme.primary

    Surface(
        modifier = modifier.clickable(onClick = onClick, role = Role.RadioButton),
        shape = MaterialTheme.shapes.small,
        color = if (isSelected) accent.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface,
        border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, if (isSelected) accent else CardBorder)
    ) {
        Text(
            text = label,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = Spacing.item, horizontal = Spacing.tight),
            style = MaterialTheme.typography.labelLarge,
            color = if (isSelected) accent else MutedText,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ReportProblemScreenPreview() {
    FrontendTheme {
        ReportProblemScreen(onBackClick = {})
    }
}
