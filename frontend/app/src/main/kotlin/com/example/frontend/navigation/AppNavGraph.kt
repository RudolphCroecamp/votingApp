package com.example.frontend.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.frontend.data.Party
import com.example.frontend.data.SampleParties
import com.example.frontend.ui.Components.BottomTab
import com.example.frontend.ui.screens.Auth.LoginScreen
import com.example.frontend.ui.screens.Info.InfoScreen
import com.example.frontend.ui.screens.Registration.RegistrationUiEvent
import com.example.frontend.ui.screens.Registration.RegistrationViewModel
import com.example.frontend.ui.screens.Registration.screens.RegisterDoneScreen
import com.example.frontend.ui.screens.Registration.screens.RegisterFaceScreen
import com.example.frontend.ui.screens.Registration.screens.RegisterIdentityScreen
import com.example.frontend.ui.screens.Registration.screens.RegisterScreen
import com.example.frontend.ui.screens.Splash.SplashScreen
import com.example.frontend.ui.screens.Vote.VoteConfirmationScreen
import com.example.frontend.ui.screens.Vote.VotePartySelectionScreen
import com.example.frontend.ui.screens.Vote.VoteReceiptScreen
import com.example.frontend.ui.screens.Vote.VoteReviewScreen
import com.example.frontend.ui.screens.Vote.VoteVerifyReceiptScreen
import com.example.frontend.ui.screens.Welcome.WelcomeScreen
import com.example.frontend.ui.screens.home.HomeScreen
import com.example.frontend.ui.theme.MotionDurations
import com.example.frontend.ui.theme.StandardEasing
import com.example.frontend.ui.theme.motionDuration

/** Which [Screen] each bottom-bar tab opens. */
private fun screenFor(tab: BottomTab): Screen = when (tab) {
    BottomTab.Home -> Screen.Home
    BottomTab.Vote -> Screen.PartySelection
    BottomTab.Info -> Screen.Info
    BottomTab.Settings -> Screen.Settings
}

/** Which tab should look selected for the destination currently on screen. */
private fun tabFor(destination: NavDestination?): BottomTab = when {
    destination == null -> BottomTab.Vote
    destination.hasRoute<Screen.Home>() -> BottomTab.Home
    destination.hasRoute<Screen.Info>() -> BottomTab.Info
    destination.hasRoute<Screen.Settings>() -> BottomTab.Settings
    else -> BottomTab.Vote
}

@Composable
fun AppNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    registrationViewModel: RegistrationViewModel = viewModel()
) {
    // Flow state that outlives a single destination. Anything a later screen
    // needs but cannot be derived from its own route lives here.
    var selectedParty by remember { mutableStateOf<Party?>(null) }
    var registeredName by remember { mutableStateOf("Voter") }

    val registrationState by registrationViewModel.uiState.collectAsState()

    val currentDestination = navController.currentBackStackEntryAsState().value?.destination
    val currentTab = tabFor(currentDestination)

    fun openTab(tab: BottomTab) {
        val target = screenFor(tab)
        if (tabFor(currentDestination) == tab) return
        navController.navigate(target) {
            // Keep one entry per tab instead of stacking them up.
            popUpTo<Screen.Home> {
                inclusive = false
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    fun logout() {
        navController.navigate(Screen.Login) {
            popUpTo(navController.graph.id) { inclusive = true }
            launchSingleTop = true
        }
    }

    // Screens cross-fade with a short horizontal drift. The slide is a fraction
    // of the width rather than the full screen, so each frame composites a
    // mostly-opaque layer: far cheaper than a full-width slide on weak GPUs.
    val transitionMillis = motionDuration(MotionDurations.medium)
    val slideFraction = 12

    NavHost(
        navController = navController,
        startDestination = Screen.Splash,
        modifier = modifier,
        enterTransition = {
            fadeIn(tween(transitionMillis, easing = StandardEasing)) +
                slideInHorizontally(tween(transitionMillis, easing = StandardEasing)) {
                    it / slideFraction
                }
        },
        exitTransition = {
            fadeOut(tween(transitionMillis, easing = StandardEasing)) +
                slideOutHorizontally(tween(transitionMillis, easing = StandardEasing)) {
                    -it / slideFraction
                }
        },
        popEnterTransition = {
            fadeIn(tween(transitionMillis, easing = StandardEasing)) +
                slideInHorizontally(tween(transitionMillis, easing = StandardEasing)) {
                    -it / slideFraction
                }
        },
        popExitTransition = {
            fadeOut(tween(transitionMillis, easing = StandardEasing)) +
                slideOutHorizontally(tween(transitionMillis, easing = StandardEasing)) {
                    it / slideFraction
                }
        }
    ) {
        composable<Screen.Splash> {
            SplashScreen(
                onFinished = {
                    navController.navigate(Screen.Welcome) {
                        // Drop the splash so Back never returns to it.
                        popUpTo<Screen.Splash> { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable<Screen.Welcome> {
            WelcomeScreen(onGetStarted = { navController.navigate(Screen.Login) })
        }

        composable<Screen.Login> {
            LoginScreen(
                onFaceVerification = { navController.navigate(Screen.Home) },
                onRegister = { navController.navigate(Screen.Register) }
            )
        }

        composable<Screen.Register> {
            RegisterScreen(
                uiState = registrationState,
                onEvent = registrationViewModel::onEvent,
                onContinue = {
                    registrationViewModel.onEvent(RegistrationUiEvent.OnNextStepClicked)
                    navController.navigate(Screen.RegisterIdentity)
                },
                onSignIn = { navController.popBackStack() },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable<Screen.RegisterIdentity> {
            RegisterIdentityScreen(
                uiState = registrationState,
                onEvent = registrationViewModel::onEvent,
                onBackClick = {
                    registrationViewModel.onEvent(RegistrationUiEvent.OnPreviousStepClicked)
                    navController.popBackStack()
                },
                onContinue = { navController.navigate(Screen.RegisterFace) }
            )
        }

        composable<Screen.RegisterFace> {
            RegisterFaceScreen(
                uiState = registrationState,
                onEvent = registrationViewModel::onEvent,
                onBackClick = {
                    registrationViewModel.onEvent(RegistrationUiEvent.OnPreviousStepClicked)
                    navController.popBackStack()
                },
                onContinue = { navController.navigate(Screen.RegisterDone) }
            )
        }

        composable<Screen.RegisterDone> {
            RegisterDoneScreen(
                voterName = registrationState.voterName,
                onContinue = {
                    registeredName = registrationState.voterName
                    registrationViewModel.onEvent(RegistrationUiEvent.OnRegistrationComplete)
                    navController.navigate(Screen.Home) {
                        popUpTo<Screen.Welcome> { inclusive = false }
                    }
                }
            )
        }

        composable<Screen.Home> {
            HomeScreen(
                onVoteNow = { navController.navigate(Screen.PartySelection) },
                voterName = registeredName,
                selectedTab = currentTab,
                onTabSelected = { openTab(it) },
                onVotingStation = { navController.navigate(Screen.Info) },
                onVotingInformation = { navController.navigate(Screen.Info) },
                onCheckRegistration = { navController.navigate(Screen.Info) },
                onRemindMe = { navController.navigate(Screen.Info) },
                onReportProblem = { navController.navigate(Screen.Info) },
                onPrivacy = { navController.navigate(Screen.Info) },
                onHelp = { navController.navigate(Screen.Info) },
                onSettings = { navController.navigate(Screen.Settings) },
                onVerifyVote = { navController.navigate(Screen.VerifyReceipt) },
                onLogout = { logout() }
            )
        }

        composable<Screen.PartySelection> {
            VotePartySelectionScreen(
                onBackClick = { navController.popBackStack() },
                onContinue = { party ->
                    selectedParty = party
                    navController.navigate(Screen.Review)
                },
                selectedTab = currentTab,
                onTabSelected = { openTab(it) }
            )
        }

        composable<Screen.Review> {
            VoteReviewScreen(
                party = selectedParty ?: SampleParties.first(),
                onChangeSelection = { navController.popBackStack() },
                onConfirmVote = { navController.navigate(Screen.Confirmation) },
                selectedTab = currentTab,
                onTabSelected = { openTab(it) }
            )
        }

        composable<Screen.Confirmation> {
            VoteConfirmationScreen(
                onDone = { navController.navigate(Screen.Home) },
                onViewReceipt = { navController.navigate(Screen.Receipt) },
                onPrivacy = { navController.navigate(Screen.Info) },
                selectedTab = currentTab,
                onTabSelected = { openTab(it) }
            )
        }

        composable<Screen.Receipt> {
            VoteReceiptScreen(
                onBackClick = { navController.popBackStack() },
                selectedTab = currentTab,
                onTabSelected = { openTab(it) }
            )
        }

        composable<Screen.VerifyReceipt> {
            VoteVerifyReceiptScreen(
                onBackClick = { navController.popBackStack() },
                selectedTab = currentTab,
                onTabSelected = { openTab(it) }
            )
        }

        composable<Screen.Info> {
            InfoScreen(
                onBackClick = { navController.popBackStack() },
                selectedTab = currentTab,
                onTabSelected = { openTab(it) }
            )
        }

        composable<Screen.Settings> {
            InfoScreen(
                onBackClick = { navController.popBackStack() },
                selectedTab = currentTab,
                onTabSelected = { openTab(it) }
            )
        }
    }
}
