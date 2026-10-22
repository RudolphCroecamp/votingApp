package com.example.frontend.ui.screens.Vote

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.frontend.R
import com.example.frontend.data.AppContainer
import com.example.frontend.data.election.Election
import com.example.frontend.data.election.ElectionRepository
import com.example.frontend.data.vote.VoteRepository
import com.example.frontend.ui.components.AppBottomBar
import com.example.frontend.ui.components.AppTopBar
import com.example.frontend.ui.components.BottomTab
import com.example.frontend.ui.components.IndeterminateLinearIndicator
import com.example.frontend.ui.components.SectionHeading
import com.example.frontend.ui.components.SimpleButton
import com.example.frontend.ui.components.StatusLabel
import com.example.frontend.ui.components.StatusType
import com.example.frontend.ui.theme.CardBorder
import com.example.frontend.ui.theme.FrontendTheme
import com.example.frontend.ui.theme.PageBackground
import com.example.frontend.ui.theme.Spacing
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface RequestVoteUiState {
    data object Loading : RequestVoteUiState
    data class Loaded(
        val elections: List<Election>,
        val selectedElectionId: String? = null,
        val isSubmitting: Boolean = false,
        val successMessage: String? = null,
        val errorMessage: String? = null
    ) : RequestVoteUiState
    data class Error(val message: String) : RequestVoteUiState
}

class RequestVoteViewModel(
    private val electionRepository: ElectionRepository = AppContainer.electionRepository,
    private val voteRepository: VoteRepository = AppContainer.voteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<RequestVoteUiState>(RequestVoteUiState.Loading)
    val uiState: StateFlow<RequestVoteUiState> = _uiState.asStateFlow()

    init {
        loadElections()
    }

    fun loadElections() {
        _uiState.value = RequestVoteUiState.Loading
        viewModelScope.launch {
            try {
                val elections = electionRepository.upcoming()
                _uiState.value = RequestVoteUiState.Loaded(elections = elections)
            } catch (e: Exception) {
                _uiState.value = RequestVoteUiState.Error(e.message ?: "Failed to load upcoming elections.")
            }
        }
    }

    fun selectElection(electionId: String) {
        _uiState.update { state ->
            if (state is RequestVoteUiState.Loaded) {
                state.copy(selectedElectionId = electionId, errorMessage = null, successMessage = null)
            } else {
                state
            }
        }
    }

    fun requestAccess(onSuccess: () -> Unit = {}) {
        val currentState = _uiState.value as? RequestVoteUiState.Loaded ?: return
        val electionId = currentState.selectedElectionId ?: return

        _uiState.value = currentState.copy(isSubmitting = true, errorMessage = null, successMessage = null)
        viewModelScope.launch {
            try {
                voteRepository.requestAccess(electionId)
                _uiState.value = currentState.copy(
                    isSubmitting = false,
                    successMessage = "Successfully requested access to vote in the election!"
                )
                onSuccess()
            } catch (e: Exception) {
                _uiState.value = currentState.copy(
                    isSubmitting = false,
                    errorMessage = e.message ?: "Failed to request election access."
                )
            }
        }
    }
}

@Composable
fun RequestVoteScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RequestVoteViewModel = viewModel(),
    selectedTab: BottomTab = BottomTab.Vote,
    onTabSelected: (BottomTab) -> Unit = {},
    backgroundColor: Color = PageBackground
) {
    val state by viewModel.uiState.collectAsState()
    RequestVoteScreen(
        uiState = state,
        onSelectElection = viewModel::selectElection,
        onSubmitRequest = { viewModel.requestAccess() },
        onRetry = { viewModel.loadElections() },
        onBackClick = onBackClick,
        modifier = modifier,
        selectedTab = selectedTab,
        onTabSelected = onTabSelected,
        backgroundColor = backgroundColor
    )
}

@Composable
fun RequestVoteScreen(
    uiState: RequestVoteUiState,
    onSelectElection: (String) -> Unit,
    onSubmitRequest: () -> Unit,
    onRetry: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    selectedTab: BottomTab = BottomTab.Vote,
    onTabSelected: (BottomTab) -> Unit = {},
    backgroundColor: Color = PageBackground
) {
    Scaffold(
        modifier = modifier,
        containerColor = backgroundColor,
        topBar = {
            AppTopBar(
                title = "Request to Vote",
                subtitle = "Select an election to request voting access",
                onBackClick = onBackClick
            )
        },
        bottomBar = { AppBottomBar(selected = selectedTab, onSelect = onTabSelected) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(Spacing.screen),
            verticalArrangement = Arrangement.spacedBy(Spacing.item)
        ) {
            when (val s = uiState) {
                is RequestVoteUiState.Loading -> {
                    IndeterminateLinearIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        label = "Loading upcoming elections..."
                    )
                }
                is RequestVoteUiState.Error -> {
                    StatusLabel(message = s.message, type = StatusType.Error)
                    SimpleButton(
                        label = stringResource(R.string.action_try_again),
                        onClick = onRetry,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                is RequestVoteUiState.Loaded -> {
                    SectionHeading(
                        title = "Upcoming Elections",
                        description = "Choose an election to submit your access request"
                    )

                    if (s.successMessage != null) {
                        StatusLabel(message = s.successMessage, type = StatusType.Success)
                    }
                    if (s.errorMessage != null) {
                        StatusLabel(message = s.errorMessage, type = StatusType.Error)
                    }

                    if (s.elections.isEmpty()) {
                        StatusLabel(message = "No upcoming elections available for access request.", type = StatusType.Warning)
                    } else {
                        s.elections.forEach { election ->
                            Card(
                                onClick = { onSelectElection(election.id) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.medium,
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                border = CardDefaults.outlinedCardBorder().copy(
                                    brush = androidx.compose.ui.graphics.SolidColor(
                                        if (election.id == s.selectedElectionId) MaterialTheme.colorScheme.secondary else CardBorder
                                    ),
                                    width = if (election.id == s.selectedElectionId) 2.dp else 1.dp
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(Spacing.card),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(Spacing.item)
                                ) {
                                    RadioButton(
                                        selected = election.id == s.selectedElectionId,
                                        onClick = { onSelectElection(election.id) }
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = election.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Type: ${election.type} | ${election.startDate} to ${election.endDate}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(Spacing.tight))

                        SimpleButton(
                            label = if (s.isSubmitting) "Submitting Request..." else "Submit Access Request",
                            onClick = onSubmitRequest,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = s.selectedElectionId != null && !s.isSubmitting,
                            trailingIcon = Icons.AutoMirrored.Filled.ArrowForward
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RequestVoteScreenPreview() {
    FrontendTheme {
        RequestVoteScreen(
            uiState = RequestVoteUiState.Loaded(
                elections = listOf(
                    Election(
                        id = "1",
                        name = "2026 Local Government Election",
                        type = "Local Government",
                        startDate = java.time.LocalDate.of(2026, 5, 20),
                        endDate = java.time.LocalDate.of(2026, 5, 22),
                        isActive = true
                    ),
                    Election(
                        id = "2",
                        name = "Youth Representative Election",
                        type = "Representative",
                        startDate = java.time.LocalDate.of(2026, 8, 10),
                        endDate = java.time.LocalDate.of(2026, 8, 12),
                        isActive = false
                    )
                ),
                selectedElectionId = "1"
            ),
            onSelectElection = {},
            onSubmitRequest = {},
            onRetry = {},
            onBackClick = {}
        )
    }
}
