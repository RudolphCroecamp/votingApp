package com.example.frontend.ui.screens.Vote

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.frontend.R
import com.example.frontend.ui.Components.AppTopBar
import com.example.frontend.ui.Components.ButtonVariant
import com.example.frontend.ui.Components.Camera.FaceCheckCamera
import com.example.frontend.ui.Components.Camera.rememberCameraPermission
import com.example.frontend.ui.Components.DescriptionLabel
import com.example.frontend.ui.Components.IndeterminateLinearIndicator
import com.example.frontend.ui.Components.SectionHeading
import com.example.frontend.ui.Components.SimpleButton
import com.example.frontend.ui.Components.StatusLabel
import com.example.frontend.ui.Components.StatusType
import com.example.frontend.ui.screens.Auth.LoginFaceUiEvent
import com.example.frontend.ui.screens.Auth.LoginFaceUiState
import com.example.frontend.ui.screens.Auth.checking
import com.example.frontend.ui.screens.Auth.matched
import com.example.frontend.ui.screens.Auth.retried
import com.example.frontend.ui.screens.Auth.withCaptureProblem
import com.example.frontend.ui.theme.FrontendTheme
import com.example.frontend.ui.theme.MutedText
import com.example.frontend.ui.theme.PageBackground
import com.example.frontend.ui.theme.Spacing
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/*
 * Face check shown when the voter taps Confirm Vote (KAN-19).
 * Same camera + liveness as the sign-in check (KAN-14) and the same LoginFaceUiState
 * transitions. Pass -> the vote is cast; fail -> back to Review with an error.
 * Attempt counting lives in VoteFlowState so it survives the trip back to Review.
 */

/** Matches a fresh scan against the voter's registration photo. */
fun interface FaceMatcher {
    suspend fun matches(idNumber: String, imagePath: String): Boolean
}

/** Stand-in until the backend face-match endpoint exists: always matches after a short delay. */
object DemoFaceMatcher : FaceMatcher {
    override suspend fun matches(idNumber: String, imagePath: String): Boolean {
        delay(1200)
        return true
    }
}

/** Outcome of one vote face check; drives navigation exactly once. */
enum class VoteFaceOutcome { Matched, Rejected }

fun LoginFaceUiState.voteOutcome(): VoteFaceOutcome? = when {
    isVerified -> VoteFaceOutcome.Matched
    didNotMatch -> VoteFaceOutcome.Rejected
    else -> null
}

/** A failed match ends this screen; the attempt is counted by the flow, not here. */
fun LoginFaceUiState.voteRejected(): LoginFaceUiState = copy(isChecking = false, didNotMatch = true)

class VoteFaceCheckViewModel(
    private val matcher: FaceMatcher = DemoFaceMatcher
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginFaceUiState())
    val uiState: StateFlow<LoginFaceUiState> = _uiState.asStateFlow()

    fun start(idNumber: String) {
        if (_uiState.value.idNumber == idNumber) return
        _uiState.value = LoginFaceUiState(idNumber = idNumber)
    }

    fun onEvent(event: LoginFaceUiEvent) {
        when (event) {
            is LoginFaceUiEvent.OnFaceCaptured -> match(event.imagePath)
            is LoginFaceUiEvent.OnCaptureProblem -> _uiState.update { it.withCaptureProblem(event.messageRes) }
            LoginFaceUiEvent.OnRetry -> _uiState.update { it.retried() }
        }
    }

    private fun match(imagePath: String) {
        if (!_uiState.value.cameraActive) return
        viewModelScope.launch {
            _uiState.update { it.checking() }
            val ok = matcher.matches(_uiState.value.idNumber, imagePath)
            _uiState.update { if (ok) it.matched() else it.voteRejected() }
        }
    }
}

@Composable
fun VoteFaceCheckScreen(
    uiState: LoginFaceUiState,
    onEvent: (LoginFaceUiEvent) -> Unit,
    onBackClick: () -> Unit,
    onVerified: () -> Unit,
    onFailed: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = PageBackground
) {
    val (cameraGranted, requestCamera) = rememberCameraPermission()

    val outcome = uiState.voteOutcome()
    LaunchedEffect(outcome) {
        when (outcome) {
            VoteFaceOutcome.Matched -> onVerified()
            VoteFaceOutcome.Rejected -> onFailed()
            null -> Unit
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = backgroundColor,
        topBar = {
            AppTopBar(
                title = stringResource(R.string.vote_face_title),
                subtitle = stringResource(R.string.vote_face_subtitle),
                onBackClick = onBackClick
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.screen, vertical = Spacing.section),
            verticalArrangement = Arrangement.spacedBy(Spacing.item),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SectionHeading(
                title = stringResource(R.string.vote_face_heading),
                description = stringResource(R.string.vote_face_description)
            )

            FaceCheckCamera(
                active = uiState.cameraActive,
                cameraGranted = cameraGranted,
                fileName = "vote_face.jpg",
                onFaceCaptured = { onEvent(LoginFaceUiEvent.OnFaceCaptured(it)) },
                onCaptureProblem = { onEvent(LoginFaceUiEvent.OnCaptureProblem(it)) },
                onRetry = { onEvent(LoginFaceUiEvent.OnRetry) },
                modifier = Modifier.fillMaxWidth()
            )

            StatusLabel(
                message = voteFaceStatus(uiState, cameraGranted),
                type = when {
                    uiState.isVerified -> StatusType.Success
                    uiState.captureProblem != null -> StatusType.Error
                    else -> StatusType.Info
                },
                modifier = Modifier.fillMaxWidth()
            )

            if (uiState.isChecking) {
                IndeterminateLinearIndicator(label = stringResource(R.string.status_checking))
            }

            when {
                uiState.captureProblem != null -> SimpleButton(
                    label = stringResource(R.string.action_try_again),
                    onClick = { onEvent(LoginFaceUiEvent.OnRetry) },
                    modifier = Modifier.fillMaxWidth(),
                    variant = ButtonVariant.Tertiary,
                    leadingIcon = Icons.Default.Refresh
                )
                !cameraGranted -> SimpleButton(
                    label = stringResource(R.string.camera_allow_access),
                    onClick = requestCamera,
                    modifier = Modifier.fillMaxWidth(),
                    variant = ButtonVariant.Tertiary,
                    leadingIcon = Icons.Default.CenterFocusStrong
                )
            }

            DescriptionLabel(
                text = stringResource(R.string.vote_face_privacy),
                modifier = Modifier.fillMaxWidth(),
                color = MutedText,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun voteFaceStatus(state: LoginFaceUiState, cameraGranted: Boolean): String = when {
    state.isVerified -> stringResource(R.string.vote_face_matched)
    state.isChecking -> stringResource(R.string.status_checking)
    state.captureProblem != null -> stringResource(state.captureProblem)
    !cameraGranted -> stringResource(R.string.camera_needed_confirm)
    else -> stringResource(R.string.login_face_hint)
}

@Preview(showBackground = true)
@Composable
private fun VoteFaceCheckScreenPreview() {
    FrontendTheme {
        VoteFaceCheckScreen(
            uiState = LoginFaceUiState(idNumber = "0203015800085"),
            onEvent = {},
            onBackClick = {},
            onVerified = {},
            onFailed = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun VoteFaceCheckScreenCheckingPreview() {
    FrontendTheme {
        VoteFaceCheckScreen(
            uiState = LoginFaceUiState(idNumber = "0203015800085", isChecking = true),
            onEvent = {},
            onBackClick = {},
            onVerified = {},
            onFailed = {}
        )
    }
}
