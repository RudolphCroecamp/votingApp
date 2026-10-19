package com.example.frontend.ui.screens.Registration

import android.os.Looper
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.example.frontend.data.address.AddressSuggestion
import com.example.frontend.ui.screens.Registration.screens.RegisterScreen
import com.example.frontend.ui.theme.FrontendTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import java.time.Duration

/** Step 1 says why it can't continue and fills the address from a suggestion. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class RegistrationDetailsFeedbackTest {

    @get:Rule
    val compose = createComposeRule()

    private val gqeberha = AddressSuggestion(
        label = "Main Road, Walmer, Gqeberha, 6070, South Africa",
        street = "Main Road",
        city = "Gqeberha",
        postalCode = "6070",
        province = "Eastern Cape"
    )

    private val queries = mutableListOf<String>()
    private val viewModel = RegistrationViewModel(addressSuggester = { queries += it; listOf(gqeberha) })
    private val state get() = viewModel.uiState.value

    private fun waitForSuggestions() = shadowOf(Looper.getMainLooper()).idleFor(Duration.ofSeconds(1))

    @Test
    fun `an empty form lists every required field`() {
        assertEquals(
            listOf(
                DetailsIssue.NAME_MISSING, DetailsIssue.ID_MISSING, DetailsIssue.PHONE_MISSING,
                DetailsIssue.PROVINCE_MISSING, DetailsIssue.STREET_MISSING, DetailsIssue.CITY_MISSING,
                DetailsIssue.POSTAL_INVALID
            ),
            state.detailsIssues
        )
    }

    @Test
    fun `a single name asks for the surname`() {
        viewModel.onEvent(RegistrationUiEvent.OnFullNamesChanged("Thandi"))
        assertTrue(DetailsIssue.SURNAME_MISSING in state.detailsIssues)
    }

    @Test
    fun `short ids, odd cellphones and bad emails are flagged`() {
        viewModel.onEvent(RegistrationUiEvent.OnIdNumberChanged("92011850"))
        viewModel.onEvent(RegistrationUiEvent.OnPhoneChanged("08212"))
        viewModel.onEvent(RegistrationUiEvent.OnEmailChanged("thandi@"))
        assertTrue(state.detailsIssues.containsAll(
            listOf(DetailsIssue.ID_LENGTH, DetailsIssue.PHONE_INVALID, DetailsIssue.EMAIL_INVALID)
        ))

        viewModel.onEvent(RegistrationUiEvent.OnIdNumberChanged("A12345678"))
        viewModel.onEvent(RegistrationUiEvent.OnPhoneChanged("+27 82 123 4567"))
        viewModel.onEvent(RegistrationUiEvent.OnEmailChanged(""))
        assertTrue(state.detailsIssues.none {
            it in listOf(DetailsIssue.ID_LENGTH, DetailsIssue.PHONE_INVALID, DetailsIssue.EMAIL_INVALID)
        })
    }

    @Test
    fun `typing a street suggests addresses once and picking one fills the address`() {
        viewModel.onEvent(RegistrationUiEvent.OnStreetChanged("12 Ma"))
        viewModel.onEvent(RegistrationUiEvent.OnStreetChanged("12 Main"))
        waitForSuggestions()

        assertEquals(listOf("12 Main"), queries)
        assertEquals(listOf(gqeberha), state.addressSuggestions)

        viewModel.onEvent(RegistrationUiEvent.OnAddressSuggestionPicked(gqeberha))
        assertEquals("12 Main Road", state.street)
        assertEquals("Gqeberha", state.city)
        assertEquals("6070", state.postalCode)
        assertEquals(Province.EASTERN_CAPE, state.province)
        assertTrue(state.addressSuggestions.isEmpty())
    }

    @Test
    fun `short queries do not hit the geocoder`() {
        viewModel.onEvent(RegistrationUiEvent.OnStreetChanged("12"))
        waitForSuggestions()
        assertTrue(queries.isEmpty())
    }

    @Test
    fun `continue on an incomplete form explains what is missing`() {
        var continued = false
        compose.setContent {
            val uiState by viewModel.uiState.collectAsState()
            FrontendTheme {
                RegisterScreen(
                    uiState = uiState,
                    onEvent = viewModel::onEvent,
                    onContinue = { continued = true },
                    onSignIn = {},
                    onBackClick = {}
                )
            }
        }
        viewModel.onEvent(RegistrationUiEvent.OnFullNamesChanged("Thandi"))
        compose.onNodeWithText("Continue").performScrollTo().performClick()

        assertFalse(continued)
        assertTrue(state.showDetailsIssues)
        compose.onNodeWithText("Eish, not so fast!", substring = true).performScrollTo().assertExists()
        compose.onAllNodesWithText("Add your surname too", substring = true).assertCountEquals(2)
    }

    @Test
    fun `surname prefixes stay with the surname`() {
        fun split(name: String) = RegistrationUiState(fullNames = name).let { it.firstName to it.lastName }
        assertEquals("Adriaan" to "van Wyk", split("Adriaan van Wyk"))
        assertEquals("Jan Hendrik" to "van der Merwe", split("Jan Hendrik van der Merwe"))
        assertEquals("Thandi" to "Nkosi", split("Thandi Nkosi"))
        assertEquals("Van" to "Morrison", split("Van Morrison"))
        assertEquals("Thandi" to "", split("Thandi"))
    }

    @Test
    fun `starting a new registration clears the last voter's details and photos`() {
        val photo = java.io.File.createTempFile("id_front", ".jpg")
        viewModel.onEvent(RegistrationUiEvent.OnFullNamesChanged("Adriaan van Wyk"))
        viewModel.onEvent(RegistrationUiEvent.OnIdNumberChanged("0412305800081"))
        viewModel.onEvent(RegistrationUiEvent.OnConsentChanged(true))
        viewModel.onEvent(RegistrationUiEvent.OnDocumentCaptured(DocumentSide.FRONT, photo.absolutePath))
        viewModel.onEvent(RegistrationUiEvent.OnNextStepClicked)

        viewModel.onEvent(RegistrationUiEvent.OnRegistrationStarted)

        assertEquals(RegistrationUiState(), state)
        assertFalse(photo.exists())
    }
}
