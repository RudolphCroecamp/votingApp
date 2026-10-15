package com.example.frontend.navigation

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
    @Serializable
    data object PartySelection : Screen
    @Serializable
    data object Review : Screen
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
