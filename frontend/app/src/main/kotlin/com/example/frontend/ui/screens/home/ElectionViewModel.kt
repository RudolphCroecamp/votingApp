package com.example.frontend.ui.screens.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.frontend.data.AppContainer
import com.example.frontend.data.election.Election
import com.example.frontend.data.election.ElectionException
import com.example.frontend.data.election.ElectionRepository
import com.example.frontend.data.election.PlannedReminder
import com.example.frontend.data.election.ReminderInterval
import com.example.frontend.data.election.ReminderScheduler
import com.example.frontend.data.election.ReminderSettings
import com.example.frontend.data.election.ReminderStore
import com.example.frontend.data.election.planReminders
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDateTime

private const val TAG = "Elections"
private val REFRESH_AFTER: Duration = Duration.ofMinutes(5)

sealed interface ElectionsState {
    data object Loading : ElectionsState
    data class Loaded(val elections: List<Election>) : ElectionsState
    /** Offline or signed out: the home screen keeps its generic copy. */
    data object Unavailable : ElectionsState
}

/**
 * The election schedule the admin set, plus the voter's reminder choices.
 * Activity-scoped so Home and Settings read the same list and settings.
 */
class ElectionViewModel(
    private val repository: ElectionRepository = AppContainer.electionRepository,
    private val store: ReminderStore = AppContainer.reminderStore,
    private val scheduler: ReminderScheduler = AppContainer.reminderScheduler,
    private val now: () -> LocalDateTime = LocalDateTime::now
) : ViewModel() {

    private val _state = MutableStateFlow<ElectionsState>(ElectionsState.Loading)
    val state: StateFlow<ElectionsState> = _state.asStateFlow()

    val settings: StateFlow<ReminderSettings> = store.settings

    private val _nextReminder = MutableStateFlow<PlannedReminder?>(null)
    val nextReminder: StateFlow<PlannedReminder?> = _nextReminder.asStateFlow()

    private var loadJob: Job? = null
    private var loadedAt: LocalDateTime? = null

    /** Home calls this on every visit; a list fetched in the last few minutes is reused. */
    fun refresh(force: Boolean = false) {
        if (loadJob?.isActive == true) return
        val fresh = loadedAt?.let { Duration.between(it, now()) < REFRESH_AFTER } == true
        if (!force && fresh && _state.value is ElectionsState.Loaded) return
        loadJob = viewModelScope.launch {
            try {
                _state.value = ElectionsState.Loaded(repository.upcoming())
                loadedAt = now()
                reschedule()
            } catch (e: CancellationException) {
                throw e
            } catch (e: ElectionException) {
                Log.w(TAG, "Election list unavailable", e)
                // Keep whatever was scheduled last time rather than wiping it while offline.
                if (_state.value !is ElectionsState.Loaded) _state.value = ElectionsState.Unavailable
            }
        }
    }

    fun setRemindersEnabled(enabled: Boolean) = update { it.copy(enabled = enabled) }

    fun toggleInterval(interval: ReminderInterval) = update { current ->
        val intervals = if (interval in current.intervals) current.intervals - interval else current.intervals + interval
        current.copy(intervals = intervals)
    }

    /** Signing out: forget the schedule and stop pending notifications. */
    fun clear() {
        loadJob?.cancel()
        loadedAt = null
        _state.value = ElectionsState.Loading
        _nextReminder.value = null
        scheduler.cancelAll()
    }

    private fun update(change: (ReminderSettings) -> ReminderSettings) {
        store.save(change(store.settings.value))
        reschedule()
    }

    private fun reschedule() {
        val elections = (_state.value as? ElectionsState.Loaded)?.elections
        val settings = store.settings.value
        if (!settings.enabled) {
            _nextReminder.value = null
            scheduler.cancelAll()
            return
        }
        // Without a fresh list, keep the existing schedule: it was built from the last good one.
        if (elections == null) return
        val at = now()
        val plan = planReminders(elections, settings, at)
        _nextReminder.value = plan.firstOrNull()
        scheduler.schedule(plan, at)
    }
}
