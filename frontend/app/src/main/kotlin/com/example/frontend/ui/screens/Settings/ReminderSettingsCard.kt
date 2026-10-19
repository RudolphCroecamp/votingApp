package com.example.frontend.ui.screens.Settings

import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.example.frontend.R
import com.example.frontend.data.election.ReminderInterval
import com.example.frontend.data.election.canPostNotifications
import com.example.frontend.ui.components.StatusLabel
import com.example.frontend.ui.components.StatusType
import com.example.frontend.ui.theme.BrandNavy
import com.example.frontend.ui.theme.DividerColor
import com.example.frontend.ui.theme.InfoContainer
import com.example.frontend.ui.theme.InfoContent
import com.example.frontend.ui.theme.MotionDurations
import com.example.frontend.ui.theme.MutedText
import com.example.frontend.ui.theme.Spacing
import com.example.frontend.ui.theme.StandardEasing
import com.example.frontend.ui.theme.motionDuration

/**
 * Election reminders: an on/off switch, then which lead times to be reminded at.
 * Turning it on asks for the notification permission first (Android 13+).
 */
@Composable
fun ReminderSettingsCard(
    enabled: Boolean,
    intervals: Set<ReminderInterval>,
    nextReminder: String?,
    onEnabledChange: (Boolean) -> Unit,
    onIntervalToggle: (ReminderInterval) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val inPreview = LocalInspectionMode.current
    var permissionDenied by remember { mutableStateOf(false) }
    // Re-read on resume: the voter may have just come back from the system settings page.
    var notificationsAllowed by remember { mutableStateOf(inPreview || canPostNotifications(context)) }
    LifecycleResumeEffect(Unit) {
        notificationsAllowed = inPreview || canPostNotifications(context)
        onPauseOrDispose { }
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        notificationsAllowed = canPostNotifications(context)
        permissionDenied = !granted
        if (granted) onEnabledChange(true)
    }

    fun onSwitch(on: Boolean) {
        if (on && !notificationsAllowed && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            permissionDenied = false
            onEnabledChange(on)
        }
    }

    val expandMillis = motionDuration(MotionDurations.medium)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
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
                        .background(InfoContainer, RoundedCornerShape(9.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        tint = InfoContent,
                        modifier = Modifier.size(19.dp)
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = stringResource(R.string.settings_reminders_title),
                        style = MaterialTheme.typography.titleSmall,
                        color = BrandNavy
                    )
                    Text(
                        text = stringResource(R.string.settings_reminders_description),
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedText
                    )
                }
                Switch(
                    checked = enabled,
                    onCheckedChange = ::onSwitch,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = BrandNavy
                    )
                )
            }

            if (permissionDenied && !enabled) {
                PermissionHint(onOpenSettings = {
                    context.startActivity(
                        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                            .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                })
            }

            AnimatedVisibility(
                visible = enabled,
                enter = fadeIn(tween(expandMillis, easing = StandardEasing)) +
                    expandVertically(tween(expandMillis, easing = StandardEasing)),
                exit = fadeOut(tween(expandMillis, easing = StandardEasing)) +
                    shrinkVertically(tween(expandMillis, easing = StandardEasing))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = Spacing.item, end = Spacing.item, bottom = Spacing.item),
                    verticalArrangement = Arrangement.spacedBy(Spacing.small)
                ) {
                    HorizontalDivider(color = DividerColor)
                    Text(
                        text = stringResource(R.string.reminder_intervals_title),
                        style = MaterialTheme.typography.labelLarge,
                        color = BrandNavy
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(Spacing.small),
                        verticalArrangement = Arrangement.spacedBy(Spacing.tight)
                    ) {
                        ReminderInterval.entries.forEach { interval ->
                            val selected = interval in intervals
                            FilterChip(
                                selected = selected,
                                onClick = { onIntervalToggle(interval) },
                                label = { Text(stringResource(interval.labelRes)) },
                                leadingIcon = if (selected) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                } else {
                                    null
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = InfoContainer,
                                    selectedLabelColor = BrandNavy,
                                    selectedLeadingIconColor = BrandNavy
                                )
                            )
                        }
                    }
                    Text(
                        text = when {
                            intervals.isEmpty() -> stringResource(R.string.reminder_none_selected)
                            nextReminder != null -> stringResource(R.string.reminder_next, nextReminder)
                            else -> stringResource(R.string.reminder_nothing_scheduled)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedText
                    )
                    if (!notificationsAllowed) {
                        StatusLabel(
                            message = stringResource(R.string.reminder_notifications_blocked),
                            type = StatusType.Warning,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PermissionHint(onOpenSettings: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = Spacing.item, end = Spacing.item, bottom = Spacing.small)
    ) {
        StatusLabel(
            message = stringResource(R.string.reminder_permission_denied),
            type = StatusType.Warning,
            modifier = Modifier.fillMaxWidth()
        )
        TextButton(onClick = onOpenSettings) {
            Text(stringResource(R.string.reminder_open_settings), color = BrandNavy)
        }
    }
}
