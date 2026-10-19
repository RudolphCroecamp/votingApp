package com.example.frontend.data

import android.os.Looper
import com.example.frontend.data.address.GeoPoint
import com.example.frontend.data.election.Election
import com.example.frontend.data.election.ElectionException
import com.example.frontend.data.election.ElectionPhase
import com.example.frontend.data.election.PlannedReminder
import com.example.frontend.data.election.ReminderInterval
import com.example.frontend.data.election.ReminderScheduler
import com.example.frontend.data.election.ReminderSettings
import com.example.frontend.data.election.ReminderStore
import com.example.frontend.data.election.nextRelevant
import com.example.frontend.data.election.planReminders
import com.example.frontend.data.station.StationKind
import com.example.frontend.data.station.VotingStation
import com.example.frontend.data.station.distanceMeters
import com.example.frontend.data.station.rank
import com.example.frontend.ui.screens.Station.registeredAddressLine
import com.example.frontend.ui.screens.home.ElectionViewModel
import com.example.frontend.ui.screens.home.ElectionsState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import java.time.LocalDate
import java.time.LocalDateTime

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ElectionRemindersTest {

    private val now = LocalDateTime.of(2026, 10, 19, 12, 0)
    private val today = now.toLocalDate()

    private fun election(id: String, start: LocalDate, end: LocalDate = start) =
        Election(id, "Election $id", "National", start, end, isActive = false)

    @Test
    fun `phase follows the admin dates inclusively`() {
        val e = election("a", LocalDate.of(2026, 11, 4), LocalDate.of(2026, 11, 5))
        assertEquals(ElectionPhase.Upcoming, e.phase(LocalDate.of(2026, 11, 3)))
        assertEquals(ElectionPhase.Open, e.phase(LocalDate.of(2026, 11, 4)))
        assertEquals(ElectionPhase.Open, e.phase(LocalDate.of(2026, 11, 5)))
        assertEquals(ElectionPhase.Ended, e.phase(LocalDate.of(2026, 11, 6)))
    }

    @Test
    fun `an open election beats a sooner upcoming one`() {
        val open = election("open", today.minusDays(1), today.plusDays(1))
        val soon = election("soon", today.plusDays(2))
        val later = election("later", today.plusDays(30))
        assertEquals("open", listOf(later, soon, open).nextRelevant(today)?.id)
        assertEquals("soon", listOf(later, soon).nextRelevant(today)?.id)
        assertNull(listOf(election("old", today.minusDays(5))).nextRelevant(today))
    }

    @Test
    fun `reminders fire at the chosen lead times and skip the past`() {
        val start = today.plusDays(5)
        val plan = planReminders(
            listOf(election("a", start)),
            ReminderSettings(enabled = true, intervals = setOf(ReminderInterval.WEEK, ReminderInterval.DAY, ReminderInterval.MORNING_OF)),
            now
        )
        // A week before is already in the past; the day before and the morning of remain.
        assertEquals(listOf(ReminderInterval.DAY, ReminderInterval.MORNING_OF), plan.map { it.interval })
        assertEquals(start.minusDays(1).atTime(9, 0), plan[0].at)
        assertEquals(start.atTime(7, 0), plan[1].at)
    }

    @Test
    fun `disabled reminders plan nothing`() {
        val plan = planReminders(listOf(election("a", today.plusDays(10))), ReminderSettings(enabled = false), now)
        assertTrue(plan.isEmpty())
    }

    @Test
    fun `view model schedules after loading and cancels when switched off`() {
        val store = MemoryReminderStore()
        val scheduler = RecordingScheduler()
        val vm = ElectionViewModel(
            repository = { listOf(election("a", today.plusDays(10))) },
            store = store,
            scheduler = scheduler,
            now = { now }
        )
        vm.setRemindersEnabled(true)
        vm.refresh()
        idle()

        assertTrue(vm.state.value is ElectionsState.Loaded)
        assertEquals(ReminderInterval.defaults.size, scheduler.scheduled.size)
        assertEquals(ReminderInterval.WEEK, vm.nextReminder.value?.interval)

        vm.toggleInterval(ReminderInterval.WEEK)
        assertEquals(ReminderInterval.DAY, vm.nextReminder.value?.interval)

        vm.setRemindersEnabled(false)
        assertTrue(scheduler.cancelled)
        assertNull(vm.nextReminder.value)
    }

    @Test
    fun `an offline refresh keeps the existing schedule`() {
        val scheduler = RecordingScheduler()
        val vm = ElectionViewModel(
            repository = { throw ElectionException("offline") },
            store = MemoryReminderStore(ReminderSettings(enabled = true)),
            scheduler = scheduler,
            now = { now }
        )
        vm.refresh()
        idle()

        assertEquals(ElectionsState.Unavailable, vm.state.value)
        assertTrue(scheduler.scheduled.isEmpty())
        assertTrue(!scheduler.cancelled)
    }

    @Test
    fun `tagged stations rank ahead of closer guesses`() {
        val home = GeoPoint(-33.96, 25.60)
        val school = VotingStation("School", GeoPoint(-33.961, 25.601), StationKind.Likely, 150.0)
        val tagged = VotingStation("Hall", GeoPoint(-33.97, 25.61), StationKind.Official, 1_400.0)
        assertEquals(listOf("Hall", "School"), rank(listOf(school, tagged)).map { it.name })
        assertTrue(distanceMeters(home, tagged.location) in 1_300.0..1_600.0)
    }

    @Test
    fun `address line needs a street or city`() {
        assertEquals("", registeredAddressLine("", "", "6070", "Eastern Cape"))
        assertEquals(
            "12 Main Road, Gqeberha, 6070, Eastern Cape, South Africa",
            registeredAddressLine("12 Main Road", "Gqeberha", "6070", "Eastern Cape")
        )
    }

    private fun idle() = shadowOf(Looper.getMainLooper()).idle()

    private class MemoryReminderStore(initial: ReminderSettings = ReminderSettings()) : ReminderStore {
        private val _settings = MutableStateFlow(initial)
        override val settings: StateFlow<ReminderSettings> = _settings
        override fun save(settings: ReminderSettings) {
            _settings.value = settings
        }
    }

    private class RecordingScheduler : ReminderScheduler {
        var scheduled: List<PlannedReminder> = emptyList()
        var cancelled = false
        override fun schedule(reminders: List<PlannedReminder>, now: LocalDateTime) {
            scheduled = reminders
        }
        override fun cancelAll() {
            cancelled = true
            scheduled = emptyList()
        }
    }
}
