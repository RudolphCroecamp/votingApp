package com.example.frontend.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.frontend.data.Party
import com.example.frontend.data.SampleParties
import com.example.frontend.ui.screens.Auth.LoginScreen
import com.example.frontend.ui.Components.BottomTab
import com.example.frontend.ui.screens.Info.InfoScreen
import com.example.frontend.ui.screens.Registration.RegistrationUiEvent
import com.example.frontend.ui.screens.Registration.RegistrationViewModel
import com.example.frontend.ui.screens.Registration.screens.RegisterDoneScreen
import com.example.frontend.ui.screens.Registration.screens.RegisterFaceScreen
import com.example.frontend.ui.screens.Registration.screens.RegisterIdentityScreen
import com.example.frontend.ui.screens.Registration.screens.RegisterScreen
import com.example.frontend.ui.screens.Vote.VoteConfirmationScreen
import com.example.frontend.ui.screens.Vote.VotePartySelectionScreen
import com.example.frontend.ui.screens.Vote.VoteReceiptScreen
import com.example.frontend.ui.screens.Vote.VoteReviewScreen
import com.example.frontend.ui.screens.Vote.VoteVerifyReceiptScreen
import com.example.frontend.ui.screens.Welcome.WelcomeScreen
import com.example.frontend.ui.screens.home.HomeScreen

private fun routeFor(tab: BottomTab): String = when (tab) {
    BottomTab.Home -> Destinations.HOME
    BottomTab.Vote -> Destinations.PARTY_SELECTION
    BottomTab.Info -> Destinations.INFO
    BottomTab.Settings -> Destinations.SETTINGS
}

private fun tabFor(route: String?): BottomTab = when (route) {
    Destinations.HOME -> BottomTab.Home
    Destinations.INFO -> BottomTab.Info
    Destinations.SETTINGS -> BottomTab.Settings
    else -> BottomTab.Vote
}

@Composable
fun FrontendNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    registrationViewModel: RegistrationViewModel = viewModel()
) {
    var selectedParty by remember { mutableStateOf<Party?>(null) }
    var registeredName by remember { mutableStateOf("Voter") }

    val registrationState by registrationViewModel.uiState.collectAsState()

    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val currentTab = tabFor(currentRoute)

    fun openTab(tab: BottomTab) {
        val route = routeFor(tab)
        if (route == currentRoute) return
        navController.navigate(route) {
            popUpTo(Destinations.HOME) {
                inclusive = false
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    fun logout() {
        navController.navigate(Destinations.LOGIN) {
            popUpTo(navController.graph.id) { inclusive = true }
            launchSingleTop = true
        }
    }

    NavHost(
        navController = navController,
        startDestination = Destinations.WELCOME,
        modifier = modifier
    ) {
        composable(Destinations.WELCOME) {
            WelcomeScreen(onGetStarted = { navController.navigate(Destinations.LOGIN) })
        }

        composable(Destinations.LOGIN) {
            LoginScreen(
                onFaceVerification = { navController.navigate(Destinations.HOME) },
                onRegister = { navController.navigate(Destinations.REGISTER) }
            )
        }

        composable(Destinations.REGISTER) {
            RegisterScreen(
                uiState = registrationState,
                onEvent = registrationViewModel::onEvent,
                onContinue = {
                    registrationViewModel.onEvent(RegistrationUiEvent.OnNextStepClicked)
                    navController.navigate(Destinations.REGISTER_IDENTITY)
                },
                onSignIn = { navController.popBackStack() }
            )
        }

        composable(Destinations.REGISTER_IDENTITY) {
            RegisterIdentityScreen(
                uiState = registrationState,
                onEvent = registrationViewModel::onEvent,
                onBackClick = {
                    registrationViewModel.onEvent(RegistrationUiEvent.OnPreviousStepClicked)
                    navController.popBackStack()
                },
                onContinue = { navController.navigate(Destinations.REGISTER_FACE) }
            )
        }

        composable(Destinations.REGISTER_FACE) {
            RegisterFaceScreen(
                uiState = registrationState,
                onEvent = registrationViewModel::onEvent,
                onBackClick = {
                    registrationViewModel.onEvent(RegistrationUiEvent.OnPreviousStepClicked)
                    navController.popBackStack()
                },
                onContinue = { navController.navigate(Destinations.REGISTER_DONE) }
            )
        }

        composable(Destinations.REGISTER_DONE) {
            RegisterDoneScreen(
                voterName = registrationState.voterName,
                onContinue = {
                    registeredName = registrationState.voterName
                    registrationViewModel.onEvent(RegistrationUiEvent.OnRegistrationComplete)
                    navController.navigate(Destinations.HOME) {
                        popUpTo(Destinations.WELCOME) { inclusive = false }
                    }
                }
            )
        }

        composable(Destinations.HOME) {
            HomeScreen(
                onVoteNow = { navController.navigate(Destinations.PARTY_SELECTION) },
                voterName = registeredName,
                selectedTab = currentTab,
                onTabSelected = { openTab(it) },
                onVotingInformation = { navController.navigate(Destinations.INFO) },
                onPrivacy = { navController.navigate(Destinations.INFO) },
                onHelp = { navController.navigate(Destinations.INFO) },
                onSettings = { navController.navigate(Destinations.SETTINGS) },
                onVerifyVote = { navController.navigate(Destinations.VERIFY_RECEIPT) },
                onLogout = { logout() }
            )
        }

        composable(Destinations.PARTY_SELECTION) {
            VotePartySelectionScreen(
                onBackClick = { navController.popBackStack() },
                onContinue = { party ->
                    selectedParty = party
                    navController.navigate(Destinations.REVIEW)
                },
                selectedTab = currentTab,
                onTabSelected = { openTab(it) }
            )
        }

        composable(Destinations.REVIEW) {
            VoteReviewScreen(
                party = selectedParty ?: SampleParties.first(),
                onChangeSelection = { navController.popBackStack() },
                onConfirmVote = { navController.navigate(Destinations.CONFIRMATION) },
                selectedTab = currentTab,
                onTabSelected = { openTab(it) }
            )
        }

        composable(Destinations.CONFIRMATION) {
            VoteConfirmationScreen(
                onDone = { navController.navigate(Destinations.HOME) },
                onViewReceipt = { navController.navigate(Destinations.RECEIPT) },
                selectedTab = currentTab,
                onTabSelected = { openTab(it) }
            )
        }

        composable(Destinations.RECEIPT) {
            VoteReceiptScreen(
                onBackClick = { navController.popBackStack() },
                selectedTab = currentTab,
                onTabSelected = { openTab(it) }
            )
        }

        composable(Destinations.VERIFY_RECEIPT) {
            VoteVerifyReceiptScreen(
                onBackClick = { navController.popBackStack() },
                selectedTab = currentTab,
                onTabSelected = { openTab(it) }
            )
        }

        composable(Destinations.INFO) {
            InfoScreen(
                onBackClick = { navController.popBackStack() },
                selectedTab = currentTab,
                onTabSelected = { openTab(it) }
            )
        }

        composable(Destinations.SETTINGS) {
            InfoScreen(
                onBackClick = { navController.popBackStack() },
                selectedTab = currentTab,
                onTabSelected = { openTab(it) }
            )
        }
    }
}
