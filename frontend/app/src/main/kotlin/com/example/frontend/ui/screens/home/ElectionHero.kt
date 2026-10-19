package com.example.frontend.ui.screens.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.example.frontend.R
import com.example.frontend.data.election.Election
import com.example.frontend.data.election.ElectionPhase
import com.example.frontend.data.election.PlannedReminder
import com.example.frontend.data.election.nextRelevant
import com.example.frontend.ui.components.HeroState
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

/** What the home hero says about the election schedule. Null fields keep HomeScreen's defaults. */
data class ElectionHero(
    val state: HeroState,
    val title: String? = null,
    val openingLine: String? = null,
    val closingLine: String? = null,
    val footnote: String? = null
)

@Composable
fun electionHero(
    elections: ElectionsState,
    hasVoted: Boolean,
    nextReminder: PlannedReminder?,
    remindersEnabled: Boolean,
    today: LocalDate = LocalDate.now()
): ElectionHero {
    if (hasVoted) return ElectionHero(HeroState.VoteCast)
    // Offline or still loading: keep the generic copy and the Vote now button.
    if (elections !is ElectionsState.Loaded) return ElectionHero(HeroState.VotingOpen)
    val election = elections.elections.nextRelevant(today)
        ?: return ElectionHero(
            state = HeroState.ComingUp,
            title = stringResource(R.string.home_no_election_title),
            openingLine = stringResource(R.string.home_no_election_body)
        )
    val footnote = reminderFootnote(remindersEnabled, nextReminder)
    return when (election.phase(today)) {
        ElectionPhase.Open -> ElectionHero(
            state = HeroState.VotingOpen,
            title = election.name,
            closingLine = closingLine(election, today)
        )
        else -> ElectionHero(
            state = HeroState.ComingUp,
            title = election.name,
            openingLine = openingLine(election, today),
            footnote = footnote
        )
    }
}

@Composable
private fun closingLine(election: Election, today: LocalDate): String =
    if (election.endDate == today) {
        stringResource(R.string.home_closes_today)
    } else {
        stringResource(R.string.home_open_until, formatElectionDate(election.endDate))
    }

@Composable
private fun openingLine(election: Election, today: LocalDate): String {
    val days = ChronoUnit.DAYS.between(today, election.startDate).toInt()
    val date = formatElectionDate(election.startDate)
    return if (days == 1) {
        stringResource(R.string.home_opens_tomorrow, date)
    } else {
        pluralStringResource(R.plurals.home_opens_in_days, days, days, date)
    }
}

@Composable
private fun reminderFootnote(enabled: Boolean, next: PlannedReminder?): String =
    when {
        !enabled -> stringResource(R.string.home_reminder_off)
        next != null -> stringResource(R.string.reminder_next, formatReminderTime(next.at))
        else -> stringResource(R.string.reminder_nothing_scheduled)
    }

@Composable
@ReadOnlyComposable
private fun locale(): Locale = LocalConfiguration.current.locales[0] ?: Locale.getDefault()

@Composable
@ReadOnlyComposable
fun formatElectionDate(date: LocalDate): String =
    date.format(DateTimeFormatter.ofPattern("EEE d MMMM yyyy", locale()))

@Composable
@ReadOnlyComposable
fun formatReminderTime(at: LocalDateTime): String =
    at.format(DateTimeFormatter.ofPattern("EEE d MMM, HH:mm", locale()))
