package com.example.frontend.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.frontend.R
import com.example.frontend.data.SampleParties
import com.example.frontend.ui.components.Ballot
import com.example.frontend.ui.components.BottomTab
import com.example.frontend.ui.screens.Auth.LoginFaceScreen
import com.example.frontend.ui.screens.Auth.LoginFaceViewModel
import com.example.frontend.ui.screens.Auth.LoginScreen
import com.example.frontend.ui.screens.Info.InfoScreen
import com.example.frontend.ui.screens.Info.InfoDetailScreen
import com.example.frontend.ui.screens.Info.InfoTopic
import com.example.frontend.ui.components.HeroState
import com.example.frontend.ui.screens.Manifestos.ManifestosScreen
import com.example.frontend.ui.screens.Manifestos.PartiesLoadError
import com.example.frontend.ui.screens.Manifestos.PartiesUiState
import com.example.frontend.ui.screens.Manifestos.PartiesViewModel
import com.example.frontend.ui.screens.Manifestos.partyById
import com.example.frontend.ui.components.FullScreenLoading
import com.example.frontend.ui.screens.Manifestos.PartyDetailScreen
import com.example.frontend.ui.screens.Profile.ProfileScreen
import com.example.frontend.ui.screens.Settings.SettingsScreen
import com.example.frontend.ui.screens.Registration.RegistrationUiEvent
import com.example.frontend.ui.screens.Registration.RegistrationViewModel
import com.example.frontend.ui.screens.PrivacyConsent.PrivacyConsentScreen
import com.example.frontend.ui.screens.Registration.screens.RegisterDoneScreen
import com.example.frontend.ui.screens.Registration.screens.RegisterFaceScreen
import com.example.frontend.ui.screens.Registration.screens.RegisterIdentityScreen
import com.example.frontend.ui.screens.Registration.screens.RegisterOtpScreen
import com.example.frontend.ui.screens.Registration.screens.RegisterScreen
import com.example.frontend.ui.screens.Splash.SplashScreen
import com.example.frontend.ui.screens.Vote.SampleReceipts
import com.example.frontend.ui.screens.Vote.VoteConfirmationScreen
import com.example.frontend.ui.screens.Vote.VoteFaceCheckScreen
import com.example.frontend.ui.screens.Vote.VoteFaceCheckViewModel
import com.example.frontend.ui.screens.Vote.VoteFlowViewModel
import com.example.frontend.ui.screens.Vote.nextBallotAfter
import com.example.frontend.ui.screens.Vote.resolveParties
import com.example.frontend.ui.screens.Vote.VotePartySelectionScreen
import com.example.frontend.ui.screens.Vote.VoteReceiptScreen
import com.example.frontend.ui.screens.Vote.VoteReviewScreen
import com.example.frontend.ui.screens.Vote.VoteVerifyReceiptScreen
import com.example.frontend.ui.screens.Vote.reviewErrorMessage
import com.example.frontend.ui.screens.Welcome.WelcomeScreen
import com.example.frontend.ui.screens.home.HomeScreen
import com.example.frontend.ui.theme.MotionDurations
import com.example.frontend.ui.theme.StandardEasing
import com.example.frontend.ui.theme.motionDuration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/** Which [Screen] each bottom-bar tab opens. */
private fun screenFor(tab: BottomTab): Screen = when (tab) {
    BottomTab.Home -> Screen.Home
    BottomTab.Vote -> Screen.PartySelection()
    BottomTab.Manifestos -> Screen.Manifestos
    BottomTab.Info -> Screen.Info
    BottomTab.Settings -> Screen.Settings
}

/** Which tab should look selected for the destination currently on screen. */
private fun tabFor(destination: NavDestination?): BottomTab = when {
    destination == null -> BottomTab.Vote
    destination.hasRoute<Screen.Home>() -> BottomTab.Home
    destination.hasRoute<Screen.Manifestos>() -> BottomTab.Manifestos
    destination.hasRoute<Screen.PartyDetail>() -> BottomTab.Manifestos
    destination.hasRoute<Screen.Info>() -> BottomTab.Info
    destination.hasRoute<Screen.Settings>() -> BottomTab.Settings
    else -> BottomTab.Vote
}

@Composable
fun AppNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    registrationViewModel: RegistrationViewModel = viewModel(),
    // Activity-scoped so all three ballots, Review and the face check share it (KAN-17).
    voteFlowViewModel: VoteFlowViewModel = viewModel(),
    // Activity-scoped so the list loads once for Manifestos and party detail (KAN-25).
    partiesViewModel: PartiesViewModel = viewModel()
) {
    // Flow state that outlives a single destination. Anything a later screen
    // needs but cannot be derived from its own route lives here.
    var registeredName by remember { mutableStateOf("Thandi Nkosi") }
    var registeredIdNumber by remember { mutableStateOf("9201185082089") }
    var registeredPhone by remember { mutableStateOf("0821234567") }
    var registeredStreet by remember { mutableStateOf("124 Market Street") }
    var registeredCity by remember { mutableStateOf("Johannesburg") }
    var registeredProvince by remember { mutableStateOf("Gauteng") }
    var registeredPostalCode by remember { mutableStateOf("2001") }

    val registrationState by registrationViewModel.uiState.collectAsState()
    val voteState by voteFlowViewModel.state.collectAsState()

    /** Resume where the voter left off; after voting, show the receipts instead. */
    fun startVoting() {
        val target = when {
            voteState.hasVoted -> Screen.Receipt
            voteState.isComplete -> Screen.Review
            else -> Screen.PartySelection(voteState.firstUnfilledBallot ?: Ballot.National)
        }
        navController.navigate(target)
    }

    val currentDestination = navController.currentBackStackEntryAsState().value?.destination
    val currentTab = tabFor(currentDestination)

    fun openTab(tab: BottomTab) {
        if (tabFor(currentDestination) == tab) return
        if (tab == BottomTab.Vote) {
            startVoting()
            return
        }
        val target = screenFor(tab)
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

    fun openInfo(topic: InfoTopic) = navController.navigate(Screen.InfoDetail(topic))

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
                onFaceVerification = { idNumber -> navController.navigate(Screen.LoginFace(idNumber)) },
                onRegister = { navController.navigate(Screen.Register) }
            )
        }

        composable<Screen.LoginFace> { entry ->
            val route = entry.toRoute<Screen.LoginFace>()
            val faceViewModel: LoginFaceViewModel = viewModel()
            val faceState by faceViewModel.uiState.collectAsState()

            LaunchedEffect(route.idNumber) { faceViewModel.start(route.idNumber) }

            LoginFaceScreen(
                uiState = faceState,
                onEvent = faceViewModel::onEvent,
                onBackClick = { navController.popBackStack() },
                onVerified = {
                    navController.navigate(Screen.Home) {
                        popUpTo<Screen.Login> { inclusive = true }
                    }
                }
            )
        }

        composable<Screen.Register> {
            RegisterScreen(
                uiState = registrationState,
                onEvent = registrationViewModel::onEvent,
                onContinue = {
                    registrationViewModel.onEvent(RegistrationUiEvent.OnNextStepClicked)
                    registrationViewModel.onEvent(RegistrationUiEvent.OnOtpRequested)
                    navController.navigate(Screen.RegisterOtp)
                },
                onSignIn = { navController.popBackStack() },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable<Screen.RegisterOtp> {
            RegisterOtpScreen(
                uiState = registrationState,
                onEvent = registrationViewModel::onEvent,
                onBackClick = {
                    registrationViewModel.onEvent(RegistrationUiEvent.OnPreviousStepClicked)
                    navController.popBackStack()
                },
                onContinue = {
                    registrationViewModel.onEvent(RegistrationUiEvent.OnNextStepClicked)
                    navController.navigate(Screen.PrivacyConsent) {
                        // The number is confirmed, so Back should not land on a dead code screen.
                        popUpTo<Screen.RegisterOtp> { inclusive = true }
                    }
                }
            )
        }

        composable<Screen.PrivacyConsent> {
            PrivacyConsentScreen(
                uiState = registrationState,
                onEvent = registrationViewModel::onEvent,
                onBackClick = {
                    registrationViewModel.onEvent(RegistrationUiEvent.OnPreviousStepClicked)
                    navController.popBackStack()
                },
                onContinue = {
                    registrationViewModel.onEvent(RegistrationUiEvent.OnNextStepClicked)
                    navController.navigate(Screen.RegisterIdentity)
                }
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
                    if (registrationState.fullNames.isNotBlank()) registeredName = registrationState.fullNames
                    if (registrationState.idNumber.isNotBlank()) registeredIdNumber = registrationState.idNumber
                    if (registrationState.phone.isNotBlank()) registeredPhone = registrationState.phone
                    if (registrationState.postalCode.isNotBlank()) registeredPostalCode = registrationState.postalCode
                    registrationViewModel.onEvent(RegistrationUiEvent.OnRegistrationComplete)
                    navController.navigate(Screen.Home) {
                        popUpTo<Screen.Welcome> { inclusive = false }
                    }
                }
            )
        }

        composable<Screen.Home> {
            HomeScreen(
                onVoteNow = { startVoting() },
                voterName = registeredName,
                heroState = if (voteState.hasVoted) HeroState.VoteCast else HeroState.VotingOpen,
                receiptSummary = voteState.receipts.firstOrNull()?.let { first ->
                    stringResource(R.string.home_receipt_summary, first.castAt, voteState.receipts.size)
                } ?: "",
                onViewReceipt = { navController.navigate(Screen.Receipt) },
                selectedTab = currentTab,
                onTabSelected = { openTab(it) },
                onVotingStation = { openInfo(InfoTopic.VotingStation) },
                onVotingInformation = { openInfo(InfoTopic.Rights) },
                onCheckRegistration = { navController.navigate(Screen.Profile) },
                onRemindMe = { navController.navigate(Screen.Info) },
                onReportProblem = { openInfo(InfoTopic.Help) },
                onPrivacy = { openInfo(InfoTopic.Privacy) },
                onHelp = { openInfo(InfoTopic.Help) },
                onSettings = { navController.navigate(Screen.Settings) },
                onVerifyVote = { navController.navigate(Screen.VerifyReceipt) },
                onLogout = { logout() }
            )
        }

        composable<Screen.PartySelection> { entry ->
            val route = entry.toRoute<Screen.PartySelection>()
            val next = nextBallotAfter(route.ballot)
            VotePartySelectionScreen(
                ballot = route.ballot,
                selectedPartyId = voteState.selectionFor(route.ballot),
                onPartySelected = { party -> voteFlowViewModel.select(route.ballot, party.id) },
                continueLabel = when {
                    route.editing -> stringResource(R.string.vote_back_to_review)
                    next != null -> stringResource(R.string.vote_next_ballot, stringResource(next.shortLabelRes))
                    else -> stringResource(R.string.vote_review_my_votes)
                },
                onContinue = {
                    when {
                        route.editing -> navController.popBackStack()
                        next != null -> navController.navigate(Screen.PartySelection(next))
                        else -> navController.navigate(Screen.Review)
                    }
                },
                onBackClick = { navController.popBackStack() },
                selectedTab = currentTab,
                onTabSelected = { openTab(it) }
            )
        }

        composable<Screen.Review> {
            VoteReviewScreen(
                selections = voteState.selections.resolveParties(SampleParties),
                errorMessage = voteState.reviewError?.let {
                    reviewErrorMessage(it, voteState.faceAttemptsLeft)
                },
                confirmEnabled = voteState.isComplete && !voteState.isFaceLocked,
                onChangeBallot = { ballot ->
                    voteFlowViewModel.clearReviewError()
                    navController.navigate(Screen.PartySelection(ballot, editing = true))
                },
                onConfirmVote = {
                    voteFlowViewModel.clearReviewError()
                    navController.navigate(Screen.VoteFaceCheck)
                },
                onBackClick = { navController.popBackStack() },
                selectedTab = currentTab,
                onTabSelected = { openTab(it) }
            )
        }

        composable<Screen.VoteFaceCheck> {
            val faceViewModel: VoteFaceCheckViewModel = viewModel()
            val faceState by faceViewModel.uiState.collectAsState()

            LaunchedEffect(registeredIdNumber) { faceViewModel.start(registeredIdNumber) }

            VoteFaceCheckScreen(
                uiState = faceState,
                onEvent = faceViewModel::onEvent,
                onBackClick = { navController.popBackStack() },
                onVerified = {
                    voteFlowViewModel.onFaceCheckPassed(
                        castAt = LocalDateTime.now().format(RECEIPT_TIME_FORMAT)
                    )
                    navController.navigate(Screen.Confirmation) {
                        // The vote is cast: Back must never re-open the ballots or the scan.
                        popUpTo<Screen.Home> { inclusive = false }
                    }
                },
                onFailed = {
                    voteFlowViewModel.onFaceCheckFailed()
                    navController.popBackStack()
                }
            )
        }

        composable<Screen.Confirmation> {
            VoteConfirmationScreen(
                receipts = voteState.receipts.ifEmpty { SampleReceipts },
                onDone = {
                    navController.navigate(Screen.Home) {
                        popUpTo<Screen.Home> { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onViewReceipt = { navController.navigate(Screen.Receipt) },
                onPrivacy = { navController.navigate(Screen.Info) },
                selectedTab = currentTab,
                onTabSelected = { openTab(it) }
            )
        }

        composable<Screen.Receipt> {
            VoteReceiptScreen(
                receipts = voteState.receipts.ifEmpty { SampleReceipts },
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

        composable<Screen.Manifestos> {
            val partiesState by partiesViewModel.state.collectAsState()
            when (val state = partiesState) {
                PartiesUiState.Loading -> FullScreenLoading(label = stringResource(R.string.parties_loading))
                PartiesUiState.Error -> PartiesLoadError(onRetry = partiesViewModel::retry)
                is PartiesUiState.Loaded -> ManifestosScreen(
                    parties = state.parties,
                    onPartyClick = { party -> navController.navigate(Screen.PartyDetail(party.id)) },
                    selectedTab = currentTab,
                    onTabSelected = { openTab(it) }
                )
            }
        }

        composable<Screen.PartyDetail> { entry ->
            val route = entry.toRoute<Screen.PartyDetail>()
            val partiesState by partiesViewModel.state.collectAsState()
            when (val state = partiesState) {
                PartiesUiState.Loading -> FullScreenLoading(label = stringResource(R.string.parties_loading))
                PartiesUiState.Error -> PartiesLoadError(onRetry = partiesViewModel::retry)
                is PartiesUiState.Loaded -> {
                    val party = state.partyById(route.partyId)
                    if (party == null) {
                        // Unknown id (stale link): leave instead of crashing.
                        LaunchedEffect(route.partyId) { navController.popBackStack() }
                    } else {
                        PartyDetailScreen(party = party, onBackClick = { navController.popBackStack() })
                    }
                }
            }
        }

        composable<Screen.Info> {
            InfoScreen(
                onBackClick = { navController.popBackStack() },
                onVotingInformation = { openInfo(InfoTopic.Rights) },
                onVotingStation = { openInfo(InfoTopic.VotingStation) },
                onPrivacy = { openInfo(InfoTopic.Privacy) },
                onHelp = { openInfo(InfoTopic.Help) },
                selectedTab = currentTab,
                onTabSelected = { openTab(it) }
            )
        }

        composable<Screen.InfoDetail> { entry ->
            InfoDetailScreen(
                topic = entry.toRoute<Screen.InfoDetail>().topic,
                onBackClick = { navController.popBackStack() },
                selectedTab = currentTab,
                onTabSelected = { openTab(it) }
            )
        }

        composable<Screen.Settings> {
            SettingsScreen(
                onBackClick = { navController.popBackStack() },
                onProfileClick = { navController.navigate(Screen.Profile) },
                onCheckRegistrationClick = { navController.navigate(Screen.Profile) },
                onVotingInformationClick = { navController.navigate(Screen.Info) },
                onPrivacyClick = { navController.navigate(Screen.Info) },
                onHelpClick = { navController.navigate(Screen.Info) },
                onReportProblemClick = { navController.navigate(Screen.Info) },
                onLogoutClick = { logout() },
                selectedTab = currentTab,
                onTabSelected = { openTab(it) }
            )
        }

        composable<Screen.Profile> {
            ProfileScreen(
                onBackClick = { navController.popBackStack() },
                initialName = registeredName,
                initialIdNumber = registeredIdNumber,
                initialPhone = registeredPhone,
                initialStreet = registeredStreet,
                initialCity = registeredCity,
                initialProvince = registeredProvince,
                initialPostalCode = registeredPostalCode,
                onSaveProfile = { phoneChanged, newPhone ->
                    if (phoneChanged) registeredPhone = newPhone
                },
                onSaveIdentity = { newName, newIdNumber ->
                    registeredName = newName
                    registeredIdNumber = newIdNumber
                },
                selectedTab = currentTab,
                onTabSelected = { openTab(it) }
            )
        }
    }
}

private val RECEIPT_TIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
