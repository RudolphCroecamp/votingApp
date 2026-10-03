package com.example.frontend.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.frontend.data.Party
import com.example.frontend.data.SampleParties
import com.example.frontend.ui.Auth.LoginScreen
import com.example.frontend.ui.Components.BottomTab
import com.example.frontend.ui.Info.InfoScreen
import com.example.frontend.ui.Vote.VoteConfirmationScreen
import com.example.frontend.ui.Vote.VotePartySelectionScreen
import com.example.frontend.ui.Vote.VoteReceiptScreen
import com.example.frontend.ui.Vote.VoteReviewScreen
import com.example.frontend.ui.Welcome.WelcomeScreen
import com.example.frontend.ui.home.HomeScreen

@Composable
fun FrontendNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    // Held here until a ViewModel owns the ballot state.
    var selectedParty by remember { mutableStateOf<Party?>(null) }

    fun openTab(tab: BottomTab) {
        when (tab) {
            BottomTab.Home -> navController.navigate(Destinations.HOME)
            BottomTab.Vote -> navController.navigate(Destinations.PARTY_SELECTION)
            BottomTab.Info -> navController.navigate(Destinations.INFO)
            BottomTab.Settings -> navController.navigate(Destinations.INFO)
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
                onSignIn = { _, _ -> navController.navigate(Destinations.HOME) },
                onFaceVerification = { navController.navigate(Destinations.HOME) },
                onRegister = {}
            )
        }

        composable(Destinations.HOME) {
            HomeScreen(
                onVoteNow = { navController.navigate(Destinations.PARTY_SELECTION) },
                selectedTab = BottomTab.Vote,
                onTabSelected = { openTab(it) }
            )
        }

        composable(Destinations.PARTY_SELECTION) {
            VotePartySelectionScreen(
                onBackClick = { navController.popBackStack() },
                onContinue = { party ->
                    selectedParty = party
                    navController.navigate(Destinations.REVIEW)
                }
            )
        }

        composable(Destinations.REVIEW) {
            VoteReviewScreen(
                party = selectedParty ?: SampleParties.first(),
                onChangeSelection = { navController.popBackStack() },
                onConfirmVote = { navController.navigate(Destinations.CONFIRMATION) }
            )
        }

        composable(Destinations.CONFIRMATION) {
            VoteConfirmationScreen(
                onDone = { navController.navigate(Destinations.HOME) },
                onViewReceipt = { navController.navigate(Destinations.RECEIPT) }
            )
        }

        composable(Destinations.RECEIPT) {
            VoteReceiptScreen(onBackClick = { navController.popBackStack() })
        }

        composable(Destinations.INFO) {
            InfoScreen(onBackClick = { navController.popBackStack() })
        }
    }
}
