package com.example.frontend.navigation

import com.example.frontend.ui.Components.Ballot
import kotlinx.serialization.Serializable

sealed interface Screen {
    @Serializable
    data object Splash : Screen
    @Serializable
    data object Welcome : Screen
    @Serializable
    data object Home : Screen
    @Serializable
    data object Login : Screen
    @Serializable
    data class LoginFace(val idNumber: String) : Screen
    @Serializable
    data object Register : Screen
    @Serializable
    data object RegisterOtp : Screen
    @Serializable
    data object PrivacyConsent : Screen
    @Serializable
    data object RegisterIdentity : Screen
    @Serializable
    data object RegisterFace : Screen
    @Serializable
    data object RegisterDone : Screen
    /**
     * One ballot of the three-ballot flow. [editing] = opened from Review's
     * "Change", so Continue returns to Review instead of the next ballot.
     */
    @Serializable
    data class PartySelection(
        val ballot: Ballot = Ballot.National,
        val editing: Boolean = false
    ) : Screen
    @Serializable
    data object Review : Screen
    @Serializable
    data object VoteFaceCheck : Screen
    @Serializable
    data object Confirmation : Screen
    @Serializable
    data object Receipt : Screen
    @Serializable
    data object VerifyReceipt : Screen
    @Serializable
    data object Info : Screen
    @Serializable
    data object Settings : Screen
    @Serializable
    data object Profile : Screen
    @Serializable
    data object Manifestos : Screen
    @Serializable
    data class PartyDetail(val partyId: String) : Screen
}
