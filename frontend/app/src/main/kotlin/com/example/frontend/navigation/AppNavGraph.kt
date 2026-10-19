package com.example.frontend.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
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
import com.example.frontend.data.AppContainer
import com.example.frontend.data.SampleParties
import com.example.frontend.data.blockchain.ChainStatus
import com.example.frontend.ui.components.Ballot
import com.example.frontend.ui.components.BottomTab
import com.example.frontend.ui.screens.Auth.LoginFaceScreen
import com.example.frontend.ui.screens.Auth.LoginFaceViewModel
import com.example.frontend.ui.screens.Auth.LoginScreen
import com.example.frontend.ui.screens.Info.InfoScreen
import com.example.frontend.ui.screens.Info.ElectionGuideScreen
import com.example.frontend.ui.screens.Info.HelpAction
import com.example.frontend.ui.screens.Info.HelpFaqScreen
import com.example.frontend.ui.screens.Info.InfoDetailScreen
import com.example.frontend.ui.screens.Info.InfoTopic
import com.example.frontend.ui.screens.Manifestos.ManifestosScreen
import com.example.frontend.ui.screens.Manifestos.PartiesLoadError
import com.example.frontend.ui.screens.Manifestos.PartiesUiState
import com.example.frontend.ui.screens.Manifestos.PartiesViewModel
import com.example.frontend.ui.screens.Manifestos.PartyDetailUiState
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
import com.example.frontend.ui.screens.Registration.screens.RegisterScreen
import com.example.frontend.ui.screens.Splash.SessionState
import com.example.frontend.ui.screens.Splash.SessionViewModel
import com.example.frontend.ui.screens.Splash.SplashScreen
import com.example.frontend.ui.screens.Support.ReportProblemScreen
import com.example.frontend.ui.screens.Support.rememberEmailSender
import com.example.frontend.ui.screens.Status.OfflineScreen
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
import com.example.frontend.ui.screens.home.ElectionViewModel
import com.example.frontend.ui.screens.home.HomeScreen
import com.example.frontend.ui.screens.home.electionHero
import com.example.frontend.ui.screens.home.formatReminderTime
import com.example.frontend.ui.screens.Station.VotingStationScreen
import com.example.frontend.ui.screens.Station.VotingStationViewModel
import com.example.frontend.ui.screens.Station.registeredAddressLine
import com.example.frontend.ui.theme.MotionDurations
import com.example.frontend.ui.theme.StandardEasing
import com.example.frontend.ui.theme.motionDuration
import com.example.frontend.ui.theme.motionEnabled
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

/** Old screen drifts 1/3 of the width while the new one covers it, as on iOS. */
private const val PARALLAX_FRACTION = 3

/** Close to UIKit's push: settles in roughly 400 ms, no overshoot. */
private const val NAV_SPRING_STIFFNESS = 320f

/** The top level of each bottom tab: switching between them is lateral, not deeper. */
private fun NavDestination.isTabRoot(): Boolean =
    hasRoute<Screen.Home>() || hasRoute<Screen.Manifestos>() || hasRoute<Screen.Info>() || hasRoute<Screen.Settings>()

/** Entry points the app jumps to when the session changes; nothing to slide back to. */
private fun NavDestination.isSessionRoot(): Boolean =
    hasRoute<Screen.Splash>() || hasRoute<Screen.Offline>()

/** Which tab should look selected for the destination currently on screen. */
private fun tabFor(destination: NavDestination?): BottomTab = when {
    destination == null -> BottomTab.Vote
    destination.hasRoute<Screen.Home>() -> BottomTab.Home
    destination.hasRoute<Screen.VotingStation>() -> BottomTab.Home
    destination.hasRoute<Screen.Manifestos>() -> BottomTab.Manifestos
    destination.hasRoute<Screen.PartyDetail>() -> BottomTab.Manifestos
    destination.hasRoute<Screen.Info>() -> BottomTab.Info
    destination.hasRoute<Screen.InfoDetail>() -> BottomTab.Info
    destination.hasRoute<Screen.Profile>() -> BottomTab.Settings
    destination.hasRoute<Screen.Settings>() -> BottomTab.Settings
    destination.hasRoute<Screen.ReportProblem>() -> BottomTab.Settings
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
    partiesViewModel: PartiesViewModel = viewModel(),
    sessionViewModel: SessionViewModel = viewModel(),
    // Activity-scoped so Home's countdown and Settings' reminders share one schedule.
    electionViewModel: ElectionViewModel = viewModel()
) {
    // Flow state that outlives a single destination. Anything a later screen
    // needs but cannot be derived from its own route lives here.
    var registeredName by remember { mutableStateOf("") }
    var registeredIdNumber by remember { mutableStateOf("") }
    var registeredPhone by remember { mutableStateOf("") }
    var registeredStreet by remember { mutableStateOf("") }
    var registeredCity by remember { mutableStateOf("") }
    var registeredProvince by remember { mutableStateOf("") }
    var registeredPostalCode by remember { mutableStateOf("") }

    val registrationState by registrationViewModel.uiState.collectAsStateWithLifecycle()
    val voteState by voteFlowViewModel.state.collectAsStateWithLifecycle()
    val sessionState by sessionViewModel.state.collectAsStateWithLifecycle()
    val signedInProfile by sessionViewModel.profile.collectAsStateWithLifecycle()
    val electionsState by electionViewModel.state.collectAsStateWithLifecycle()
    val reminderSettings by electionViewModel.settings.collectAsStateWithLifecycle()
    val nextReminder by electionViewModel.nextReminder.collectAsStateWithLifecycle()

    LaunchedEffect(sessionState) {
        if (sessionState == SessionState.SignedIn) electionViewModel.refresh()
    }

    LaunchedEffect(signedInProfile) {
        signedInProfile?.let { profile ->
            if (profile.fullName.isNotBlank()) registeredName = profile.fullName
            if (profile.idNumber.isNotBlank()) registeredIdNumber = profile.idNumber
            registeredStreet = profile.street
            registeredCity = profile.city
            registeredProvince = profile.province
            registeredPostalCode = profile.postalCode
            if (profile.phone.isNotBlank()) registeredPhone = profile.phone
        }
    }

    /** Resume where the voter left off; after voting, show the receipts instead. */
    fun startVoting() {
        val target = when {
            voteState.hasVoted -> Screen.Receipt
            voteState.isComplete -> Screen.Review
            else -> Screen.PartySelection(voteState.firstUnfilledBallot ?: Ballot.National)
        }
        navController.navigate(target)
    }

    // Read through State objects, never as plain values: the NavHost graph builder
    // below captures them, and a plain value that changes on every navigation would
    // make Compose rebuild the whole graph each time a screen opens.
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentTab by remember { derivedStateOf { tabFor(backStackEntry?.destination) } }

    // Set just before a bottom-bar navigation so the transition cross-fades instead of
    // pushing. A plain holder, not State: reading it must not trigger recomposition.
    val tabSwitch = remember { object { var pending = false } }
    LaunchedEffect(backStackEntry) { tabSwitch.pending = false }

    fun openTab(tab: BottomTab) {
        if (currentTab == tab) return
        tabSwitch.pending = true
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

    fun openSessionStart(state: SessionState) {
        val target = when (state) {
            SessionState.Checking -> return
            SessionState.SignedIn -> Screen.Home
            SessionState.SignedOut -> Screen.Welcome
            SessionState.Offline -> Screen.Offline
        }
        navController.navigate(target) {
            popUpTo(navController.graph.id) { inclusive = true }
            launchSingleTop = true
        }
    }

    fun logout() {
        sessionViewModel.signOut()
        electionViewModel.clear()
        registrationViewModel.onEvent(RegistrationUiEvent.OnRegistrationStarted)
        registeredName = ""
        registeredIdNumber = ""
        registeredPhone = ""
        registeredStreet = ""
        registeredCity = ""
        registeredProvince = ""
        registeredPostalCode = ""
        navController.navigate(Screen.Login) {
            popUpTo(navController.graph.id) { inclusive = true }
            launchSingleTop = true
        }
    }

    // iOS-style navigation. Forward: the new screen slides in from the right edge,
    // over the old one, which drifts a third of the way left (parallax). Back is the
    // exact reverse, and Navigation draws the dismissed screen on top so it peels off.
    // Offsets run on a critically damped spring, so motion eases out naturally with
    // no bounce, and the same specs drive the predictive back gesture as you drag.
    // Bottom-tab switches and session changes (sign in/out) cross-fade instead:
    // they are peers, not a step deeper.
    val animate = motionEnabled
    val fadeMillis = motionDuration(MotionDurations.medium)

    fun offsetSpec(): FiniteAnimationSpec<IntOffset> =
        if (animate) spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = NAV_SPRING_STIFFNESS) else snap()

    fun isPeerSwitch(from: NavDestination, to: NavDestination): Boolean =
        tabSwitch.pending || (from.isTabRoot() && to.isTabRoot()) || from.isSessionRoot() || to.isSessionRoot()

    NavHost(
        navController = navController,
        startDestination = Screen.Splash,
        modifier = modifier,
        enterTransition = {
            if (isPeerSwitch(initialState.destination, targetState.destination)) {
                fadeIn(tween(fadeMillis, easing = StandardEasing))
            } else {
                slideInHorizontally(offsetSpec()) { it }
            }
        },
        exitTransition = {
            if (isPeerSwitch(initialState.destination, targetState.destination)) {
                fadeOut(tween(fadeMillis, easing = StandardEasing))
            } else {
                slideOutHorizontally(offsetSpec()) { -it / PARALLAX_FRACTION }
            }
        },
        popEnterTransition = {
            if (isPeerSwitch(initialState.destination, targetState.destination)) {
                fadeIn(tween(fadeMillis, easing = StandardEasing))
            } else {
                slideInHorizontally(offsetSpec()) { -it / PARALLAX_FRACTION }
            }
        },
        popExitTransition = {
            if (isPeerSwitch(initialState.destination, targetState.destination)) {
                fadeOut(tween(fadeMillis, easing = StandardEasing))
            } else {
                slideOutHorizontally(offsetSpec()) { it }
            }
        }
    ) {
        composable<Screen.Splash> {
            var splashFinished by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) { sessionViewModel.check() }
            LaunchedEffect(splashFinished, sessionState) {
                if (splashFinished) openSessionStart(sessionState)
            }

            if (splashFinished) {
                FullScreenLoading(label = stringResource(R.string.offline_looking))
            } else {
                SplashScreen(onFinished = { splashFinished = true })
            }
        }

        composable<Screen.Offline> {
            LaunchedEffect(sessionState) {
                if (sessionState == SessionState.SignedIn || sessionState == SessionState.SignedOut) {
                    openSessionStart(sessionState)
                }
            }

            OfflineScreen(
                onRetry = sessionViewModel::check,
                onBackClick = {
                    navController.navigate(Screen.Welcome) {
                        popUpTo<Screen.Offline> { inclusive = true }
                    }
                },
                wasCastingVote = false,
                isRetrying = sessionState == SessionState.Checking
            )
        }

        composable<Screen.Welcome> {
            WelcomeScreen(onGetStarted = { navController.navigate(Screen.Login) })
        }

        composable<Screen.Login> {
            LoginScreen(
                onFaceVerification = { idNumber -> navController.navigate(Screen.LoginFace(idNumber)) },
                onRegister = {
                    registrationViewModel.onEvent(RegistrationUiEvent.OnRegistrationStarted)
                    navController.navigate(Screen.Register)
                }
            )
        }

        composable<Screen.LoginFace> { entry ->
            val route = entry.toRoute<Screen.LoginFace>()
            val faceViewModel: LoginFaceViewModel = viewModel()
            val faceState by faceViewModel.uiState.collectAsStateWithLifecycle()

            LaunchedEffect(route.idNumber) { faceViewModel.start(route.idNumber) }

            LoginFaceScreen(
                uiState = faceState,
                onEvent = faceViewModel::onEvent,
                onBackClick = { navController.popBackStack() },
                onVerified = {
                    sessionViewModel.onSignedIn()
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
                    navController.navigate(Screen.PrivacyConsent)
                },
                onSignIn = { navController.popBackStack() },
                onBackClick = { navController.popBackStack() }
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
                    sessionViewModel.onSignedIn()
                    navController.navigate(Screen.Home) {
                        popUpTo<Screen.Welcome> { inclusive = false }
                    }
                }
            )
        }

        composable<Screen.Home> {
            LaunchedEffect(Unit) { electionViewModel.refresh() }
            val hero = electionHero(
                elections = electionsState,
                hasVoted = voteState.hasVoted,
                nextReminder = nextReminder,
                remindersEnabled = reminderSettings.enabled
            )
            HomeScreen(
                onVoteNow = { startVoting() },
                voterName = registeredName,
                heroState = hero.state,
                electionName = hero.title ?: stringResource(R.string.home_sample_election),
                closingTime = hero.closingLine ?: stringResource(R.string.home_sample_closing),
                openingTime = hero.openingLine ?: stringResource(R.string.home_sample_opening),
                comingUpFootnote = hero.footnote,
                receiptSummary = voteState.receipts.firstOrNull()?.let { first ->
                    stringResource(R.string.home_receipt_summary, first.castAt, voteState.receipts.size)
                } ?: "",
                onViewReceipt = { navController.navigate(Screen.Receipt) },
                selectedTab = currentTab,
                onTabSelected = { openTab(it) },
                onVotingStation = { navController.navigate(Screen.VotingStation) },
                onVotingInformation = { openInfo(InfoTopic.Rights) },
                onCheckRegistration = { navController.navigate(Screen.Profile) },
                onRemindMe = { navController.navigate(Screen.Settings) },
                onReportProblem = { navController.navigate(Screen.ReportProblem) },
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
            val faceState by faceViewModel.uiState.collectAsStateWithLifecycle()

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
            val chainStatus by produceState<ChainStatus>(ChainStatus.Checking) {
                value = runCatching { AppContainer.blockchainRepository.status() }
                    .getOrDefault(ChainStatus.Unavailable)
            }
            VoteVerifyReceiptScreen(
                onBackClick = { navController.popBackStack() },
                selectedTab = currentTab,
                onTabSelected = { openTab(it) },
                chainStatus = chainStatus
            )
        }

        composable<Screen.Manifestos> {
            val partiesState by partiesViewModel.state.collectAsStateWithLifecycle()
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
            val detailState by partiesViewModel.detail.collectAsStateWithLifecycle()
            LaunchedEffect(route.partyId) { partiesViewModel.openParty(route.partyId) }
            when (val state = detailState.takeIf { it.partyId == route.partyId }) {
                null, is PartyDetailUiState.Loading ->
                    FullScreenLoading(label = stringResource(R.string.parties_loading))
                is PartyDetailUiState.Error ->
                    PartiesLoadError(onRetry = { partiesViewModel.openParty(route.partyId) })
                is PartyDetailUiState.NotFound ->
                    // Unknown id (stale link): leave instead of crashing.
                    LaunchedEffect(route.partyId) { navController.popBackStack() }
                is PartyDetailUiState.Loaded ->
                    PartyDetailScreen(party = state.party, onBackClick = { navController.popBackStack() })
            }
        }

        composable<Screen.Info> {
            InfoScreen(
                onBackClick = { navController.popBackStack() },
                onVotingInformation = { openInfo(InfoTopic.Rights) },
                onVotingStation = { navController.navigate(Screen.VotingStation) },
                onPrivacy = { openInfo(InfoTopic.Privacy) },
                onHelp = { openInfo(InfoTopic.Help) },
                selectedTab = currentTab,
                onTabSelected = { openTab(it) }
            )
        }

        composable<Screen.InfoDetail> { entry ->
            when (val topic = entry.toRoute<Screen.InfoDetail>().topic) {
                InfoTopic.Rights -> ElectionGuideScreen(
                    onBackClick = { navController.popBackStack() },
                    selectedTab = currentTab,
                    onTabSelected = { openTab(it) }
                )
                InfoTopic.Help -> HelpFaqScreen(
                    onBackClick = { navController.popBackStack() },
                    onAction = { action ->
                        when (action) {
                            HelpAction.Settings -> navController.navigate(Screen.Settings)
                            HelpAction.Profile -> navController.navigate(Screen.Profile)
                            HelpAction.VerifyVote -> navController.navigate(Screen.VerifyReceipt)
                            HelpAction.VotingStation -> navController.navigate(Screen.VotingStation)
                            HelpAction.ReportProblem -> navController.navigate(Screen.ReportProblem)
                            HelpAction.ElectionGuide -> openInfo(InfoTopic.Rights)
                            // Dialling and the website are handled inside the screen.
                            HelpAction.CallIec, HelpAction.Website -> Unit
                        }
                    },
                    selectedTab = currentTab,
                    onTabSelected = { openTab(it) }
                )
                else -> InfoDetailScreen(
                    topic = topic,
                    onBackClick = { navController.popBackStack() },
                    selectedTab = currentTab,
                    onTabSelected = { openTab(it) }
                )
            }
        }

        composable<Screen.Settings> {
            SettingsScreen(
                onBackClick = { navController.popBackStack() },
                onProfileClick = { navController.navigate(Screen.Profile) },
                onCheckRegistrationClick = { navController.navigate(Screen.Profile) },
                onVotingInformationClick = { openInfo(InfoTopic.Rights) },
                onPrivacyClick = { openInfo(InfoTopic.Privacy) },
                onHelpClick = { openInfo(InfoTopic.Help) },
                onReportProblemClick = { navController.navigate(Screen.ReportProblem) },
                onLogoutClick = { logout() },
                remindersEnabled = reminderSettings.enabled,
                reminderIntervals = reminderSettings.intervals,
                nextReminder = nextReminder?.let { formatReminderTime(it.at) },
                onRemindersEnabledChange = electionViewModel::setRemindersEnabled,
                onReminderIntervalToggle = electionViewModel::toggleInterval,
                selectedTab = currentTab,
                onTabSelected = { openTab(it) }
            )
        }

        composable<Screen.VotingStation> {
            val stationViewModel: VotingStationViewModel = viewModel()
            val stationState by stationViewModel.state.collectAsStateWithLifecycle()
            val address = registeredAddressLine(
                street = registeredStreet,
                city = registeredCity,
                postalCode = registeredPostalCode,
                province = registeredProvince
            )
            LaunchedEffect(address) { stationViewModel.load(address) }

            VotingStationScreen(
                state = stationState,
                onSelect = stationViewModel::select,
                onRetry = stationViewModel::retry,
                onEditAddress = { navController.navigate(Screen.Profile) },
                onBackClick = { navController.popBackStack() },
                selectedTab = currentTab,
                onTabSelected = { openTab(it) }
            )
        }

        composable<Screen.ReportProblem> {
            ReportProblemScreen(
                onBackClick = { navController.popBackStack() },
                selectedTab = currentTab,
                onTabSelected = { openTab(it) },
                onSend = rememberEmailSender()
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
