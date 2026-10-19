package com.example.frontend.ui.screens.Registration

import com.example.frontend.ui.screens.Auth.ID_NUMBER_MAX_LENGTH
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Registration state machine (KAN-43): step counter, field limits and the details gate.
 * Robolectric supplies the main looper the view model's coroutines run on.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class RegistrationViewModelTest {

    private val viewModel = RegistrationViewModel()
    private val state get() = viewModel.uiState.value

    private fun send(vararg events: RegistrationUiEvent) = events.forEach(viewModel::onEvent)

    private fun fillDetails() = send(
        RegistrationUiEvent.OnFullNamesChanged("Thandi Nkosi"),
        RegistrationUiEvent.OnIdNumberChanged("9201185082089"),
        RegistrationUiEvent.OnPhoneChanged("0821234567"),
        RegistrationUiEvent.OnProvinceSelected(Province.GAUTENG),
        RegistrationUiEvent.OnStreetChanged("124 Market Street"),
        RegistrationUiEvent.OnCityChanged("Johannesburg"),
        RegistrationUiEvent.OnPostalCodeChanged("2001")
    )

    // --- Steps ------------------------------------------------------------------

    @Test
    fun `next and previous move one step at a time`() {
        assertEquals(1, state.currentStep)
        send(RegistrationUiEvent.OnNextStepClicked, RegistrationUiEvent.OnNextStepClicked)
        assertEquals(3, state.currentStep)
        send(RegistrationUiEvent.OnPreviousStepClicked)
        assertEquals(2, state.currentStep)
    }

    @Test
    fun `steps stay between the first and last step`() {
        send(RegistrationUiEvent.OnPreviousStepClicked)
        assertEquals("cannot go before step 1", 1, state.currentStep)

        repeat(REGISTRATION_STEPS + 3) { send(RegistrationUiEvent.OnNextStepClicked) }
        assertEquals("cannot go past the last step", REGISTRATION_STEPS, state.currentStep)
    }

    @Test
    fun `finishing registration resets everything for the next voter`() {
        fillDetails()
        send(RegistrationUiEvent.OnNextStepClicked, RegistrationUiEvent.OnConsentChanged(true))

        send(RegistrationUiEvent.OnRegistrationComplete)

        assertEquals(RegistrationUiState(), state)
    }

    // --- Details step -----------------------------------------------------------

    @Test
    fun `details can only be submitted once every required field is filled`() {
        assertFalse(state.canSubmitDetails)
        fillDetails()
        assertTrue(state.canSubmitDetails)

        send(RegistrationUiEvent.OnCityChanged(""))
        assertFalse("city is required", state.canSubmitDetails)
    }

    @Test
    fun `full names split into first names and surname`() {
        fillDetails()
        send(RegistrationUiEvent.OnFullNamesChanged("  Thandi  Grace Nkosi "))
        assertEquals("Thandi Grace", state.firstName)
        assertEquals("Nkosi", state.lastName)

        send(RegistrationUiEvent.OnFullNamesChanged("Thandi"))
        assertFalse("a surname is required", state.canSubmitDetails)
    }

    @Test
    fun `email is optional`() {
        fillDetails()
        assertEquals("", state.email)
        assertTrue(state.canSubmitDetails)
    }

    @Test
    fun `a province must be picked because it decides the ballots`() {
        send(
            RegistrationUiEvent.OnFullNamesChanged("Thandi Nkosi"),
            RegistrationUiEvent.OnIdNumberChanged("9201185082089"),
            RegistrationUiEvent.OnPhoneChanged("0821234567"),
            RegistrationUiEvent.OnStreetChanged("124 Market Street"),
            RegistrationUiEvent.OnCityChanged("Johannesburg"),
            RegistrationUiEvent.OnPostalCodeChanged("2001")
        )
        assertNull(state.province)
        assertFalse(state.canSubmitDetails)
    }

    @Test
    fun `too-long ID numbers and postal codes are ignored`() {
        send(RegistrationUiEvent.OnIdNumberChanged("9201185082089"))
        send(RegistrationUiEvent.OnIdNumberChanged("92011850820891")) // 14 digits
        assertEquals(ID_NUMBER_MAX_LENGTH, state.idNumber.length)

        send(RegistrationUiEvent.OnPostalCodeChanged("2001"))
        send(RegistrationUiEvent.OnPostalCodeChanged("20011"))
        assertEquals("2001", state.postalCode)
    }

    @Test
    fun `postal code must be exactly four digits`() {
        fillDetails()
        send(RegistrationUiEvent.OnPostalCodeChanged("200"))
        assertFalse(state.canSubmitDetails)
    }
}
