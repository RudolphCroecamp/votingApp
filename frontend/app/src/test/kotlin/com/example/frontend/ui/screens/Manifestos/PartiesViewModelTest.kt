package com.example.frontend.ui.screens.Manifestos

import android.os.Looper
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.frontend.data.Party
import com.example.frontend.data.PartyRepository
import com.example.frontend.data.SampleParties
import com.example.frontend.ui.theme.FrontendTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import java.io.IOException

/** KAN-25: the party list loads through the repository with loading and error states. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PartiesViewModelTest {

    @get:Rule
    val compose = createComposeRule()

    private fun idle() = shadowOf(Looper.getMainLooper()).idle()

    @Test
    fun `parties from the repository end up loaded`() {
        val vm = PartiesViewModel { SampleParties }
        idle()
        assertEquals(PartiesUiState.Loaded(SampleParties), vm.state.value)
        assertEquals("anc", vm.state.value.partyById("anc")?.id)
        assertNull(vm.state.value.partyById("nope"))
    }

    @Test
    fun `a failing repository shows the error state and retry recovers`() {
        var calls = 0
        val flaky = PartyRepository { if (calls++ == 0) throw IOException("offline") else SampleParties }
        val vm = PartiesViewModel(flaky)
        idle()
        assertEquals(PartiesUiState.Error, vm.state.value)

        vm.retry()
        idle()
        assertEquals(PartiesUiState.Loaded(SampleParties), vm.state.value)
        assertEquals(2, calls)
    }

    @Test
    fun `error screen offers try again`() {
        var retried = 0
        compose.setContent { FrontendTheme { PartiesLoadError(onRetry = { retried++ }) } }
        compose.onNodeWithText("Couldn't load the parties. Check your connection and try again.").assertExists()
        compose.onNodeWithText("Try again").performClick()
        assertEquals(1, retried)
    }

    @Test
    fun `opening a party loads it by id`() {
        val fresh = SampleParties.first().copy(description = "From the backend")
        val repo = object : PartyRepository {
            override suspend fun parties() = SampleParties
            override suspend fun party(id: String) = fresh.takeIf { id == fresh.id }
        }
        val vm = PartiesViewModel(repo)
        idle()

        vm.openParty(fresh.id)
        idle()
        assertEquals(PartyDetailUiState.Loaded(fresh.id, fresh), vm.detail.value)
    }

    @Test
    fun `a failed lookup falls back to the loaded list`() {
        val repo = object : PartyRepository {
            override suspend fun parties() = SampleParties
            override suspend fun party(id: String): Party? = throw IOException("offline")
        }
        val vm = PartiesViewModel(repo)
        idle()

        vm.openParty("da")
        idle()
        assertEquals(PartyDetailUiState.Loaded("da", SampleParties[1]), vm.detail.value)

        vm.openParty("nope")
        idle()
        assertEquals(PartyDetailUiState.Error("nope"), vm.detail.value)
    }

    @Test
    fun `an unknown party is reported as not found`() {
        val vm = PartiesViewModel { SampleParties }
        idle()

        vm.openParty("nope")
        idle()
        assertEquals(PartyDetailUiState.NotFound("nope"), vm.detail.value)
    }
}
