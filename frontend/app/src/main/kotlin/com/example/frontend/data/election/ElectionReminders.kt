package com.example.frontend.data.election

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.annotation.StringRes
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.frontend.MainActivity
import com.example.frontend.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.concurrent.TimeUnit

private const val TAG = "ElectionReminders"

/** How long before voting opens a reminder fires. Advance reminders come at 09:00, the day-of one at 07:00. */
enum class ReminderInterval(val daysBefore: Long, @param:StringRes val labelRes: Int) {
    MONTH(30, R.string.reminder_interval_month),
    TWO_WEEKS(14, R.string.reminder_interval_two_weeks),
    WEEK(7, R.string.reminder_interval_week),
    THREE_DAYS(3, R.string.reminder_interval_three_days),
    DAY(1, R.string.reminder_interval_day),
    MORNING_OF(0, R.string.reminder_interval_morning_of);

    fun fireTime(startDate: LocalDate): LocalDateTime =
        startDate.minusDays(daysBefore).atTime(if (daysBefore == 0L) DAY_OF_TIME else ADVANCE_TIME)

    companion object {
        val ADVANCE_TIME: LocalTime = LocalTime.of(9, 0)
        val DAY_OF_TIME: LocalTime = LocalTime.of(7, 0)
        val defaults: Set<ReminderInterval> = setOf(WEEK, DAY, MORNING_OF)
    }
}

data class ReminderSettings(
    val enabled: Boolean = false,
    val intervals: Set<ReminderInterval> = ReminderInterval.defaults
)

data class PlannedReminder(val election: Election, val interval: ReminderInterval, val at: LocalDateTime)

/** Every reminder still ahead of [now], soonest first. Ended elections and past times are dropped. */
fun planReminders(
    elections: List<Election>,
    settings: ReminderSettings,
    now: LocalDateTime
): List<PlannedReminder> {
    if (!settings.enabled) return emptyList()
    return elections
        .filter { it.phase(now.toLocalDate()) != ElectionPhase.Ended }
        .flatMap { election -> settings.intervals.map { PlannedReminder(election, it, it.fireTime(election.startDate)) } }
        .filter { it.at.isAfter(now) }
        .sortedBy { it.at }
}

interface ReminderStore {
    val settings: StateFlow<ReminderSettings>

    fun save(settings: ReminderSettings)
}

class PrefsReminderStore(context: Context) : ReminderStore {

    private val prefs = context.getSharedPreferences("election_reminders", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(read())
    override val settings: StateFlow<ReminderSettings> = _settings.asStateFlow()

    override fun save(settings: ReminderSettings) {
        prefs.edit {
            putBoolean(KEY_ENABLED, settings.enabled)
            putStringSet(KEY_INTERVALS, settings.intervals.map { it.name }.toSet())
        }
        _settings.value = settings
    }

    private fun read(): ReminderSettings {
        val names = prefs.getStringSet(KEY_INTERVALS, null)
        return ReminderSettings(
            enabled = prefs.getBoolean(KEY_ENABLED, false),
            intervals = names?.mapNotNull { name -> ReminderInterval.entries.firstOrNull { it.name == name } }?.toSet()
                ?: ReminderInterval.defaults
        )
    }

    private companion object {
        const val KEY_ENABLED = "enabled"
        const val KEY_INTERVALS = "intervals"
    }
}

interface ReminderScheduler {
    /** Replaces everything scheduled before with [reminders]. */
    fun schedule(reminders: List<PlannedReminder>, now: LocalDateTime)

    fun cancelAll()
}

/** WorkManager keeps the schedule across app restarts and reboots; delivery can drift a little under Doze. */
class WorkManagerReminderScheduler(private val context: Context) : ReminderScheduler {

    private val workManager: WorkManager? by lazy {
        try {
            WorkManager.getInstance(context)
        } catch (e: IllegalStateException) {
            Log.w(TAG, "WorkManager unavailable", e)
            null
        }
    }

    override fun schedule(reminders: List<PlannedReminder>, now: LocalDateTime) {
        val manager = workManager ?: return
        manager.cancelAllWorkByTag(WORK_TAG)
        reminders.forEach { reminder ->
            val request = OneTimeWorkRequestBuilder<ElectionReminderWorker>()
                .setInitialDelay(Duration.between(now, reminder.at).toMillis(), TimeUnit.MILLISECONDS)
                .setInputData(
                    workDataOf(
                        ElectionReminderWorker.KEY_NAME to reminder.election.name,
                        ElectionReminderWorker.KEY_START to reminder.election.startDate.toString(),
                        ElectionReminderWorker.KEY_DAYS to reminder.interval.daysBefore
                    )
                )
                .addTag(WORK_TAG)
                .build()
            manager.enqueueUniqueWork(
                "reminder-${reminder.election.id}-${reminder.interval.name}",
                ExistingWorkPolicy.REPLACE,
                request
            )
        }
    }

    override fun cancelAll() {
        workManager?.cancelAllWorkByTag(WORK_TAG)
    }

    private companion object {
        const val WORK_TAG = "election-reminder"
    }
}

const val REMINDER_CHANNEL_ID = "election_reminders"

fun createReminderChannel(context: Context) {
    val channel = NotificationChannel(
        REMINDER_CHANNEL_ID,
        context.getString(R.string.reminder_channel_name),
        NotificationManager.IMPORTANCE_DEFAULT
    ).apply { description = context.getString(R.string.reminder_channel_description) }
    context.getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
}

fun canPostNotifications(context: Context): Boolean =
    (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
        PackageManager.PERMISSION_GRANTED) &&
        NotificationManagerCompat.from(context).areNotificationsEnabled()

class ElectionReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val name = inputData.getString(KEY_NAME).orEmpty()
        val start = inputData.getString(KEY_START)?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
            ?: return Result.success()
        val days = inputData.getLong(KEY_DAYS, 0L)
        if (!canPostNotifications(applicationContext)) return Result.success()

        val date = start.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
        val body = if (days == 0L) {
            applicationContext.getString(R.string.reminder_body_today)
        } else {
            applicationContext.resources.getQuantityString(R.plurals.reminder_body_days, days.toInt(), days.toInt(), date)
        }
        val openApp = PendingIntent.getActivity(
            applicationContext,
            0,
            Intent(applicationContext, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notification = NotificationCompat.Builder(applicationContext, REMINDER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_ballot)
            .setContentTitle(name.ifBlank { applicationContext.getString(R.string.reminder_title_fallback) })
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentIntent(openApp)
            .setAutoCancel(true)
            .build()
        try {
            NotificationManagerCompat.from(applicationContext).notify("$name-$start-$days".hashCode(), notification)
        } catch (e: SecurityException) {
            Log.w(TAG, "Notification permission revoked", e)
        }
        return Result.success()
    }

    companion object {
        const val KEY_NAME = "name"
        const val KEY_START = "start"
        const val KEY_DAYS = "days"
    }
}
