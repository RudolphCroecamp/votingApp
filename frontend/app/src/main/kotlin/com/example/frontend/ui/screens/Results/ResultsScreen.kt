package com.example.frontend.ui.screens.Results

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import com.example.frontend.ui.components.AppBottomBar
import com.example.frontend.ui.components.AppTopBar
import com.example.frontend.ui.components.BottomTab
import com.example.frontend.ui.components.IndeterminateLinearIndicator
import com.example.frontend.ui.components.SectionHeading
import com.example.frontend.ui.components.SimpleButton
import com.example.frontend.ui.components.StatusLabel
import com.example.frontend.ui.components.StatusType
import com.example.frontend.ui.theme.FrontendTheme
import com.example.frontend.ui.theme.PageBackground
import com.example.frontend.ui.theme.Spacing
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

data class ElectionResultItem(
    val partyId: String,
    val partyName: String,
    val votes: Int,
    val percentage: Double
)

sealed interface ResultsUiState {
    data object Loading : ResultsUiState
    data object NotPublished : ResultsUiState
    data class Loaded(
        val electionName: String,
        val results: List<ElectionResultItem>,
        val totalVotes: Int
    ) : ResultsUiState
    data class Error(val message: String) : ResultsUiState
}

/**
 * ViewModel responsible for fetching and managing certified election results.
 */
class ResultsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<ResultsUiState>(ResultsUiState.Loading)
    val uiState: StateFlow<ResultsUiState> = _uiState.asStateFlow()

    init {
        loadResults()
    }

    fun loadResults() {
        _uiState.value = ResultsUiState.Loading
        viewModelScope.launch {
            try {
                val sampleResults = listOf(
                    ElectionResultItem("1", "African National Congress (ANC)", 4521000, 40.2),
                    ElectionResultItem("2", "Democratic Alliance (DA)", 3120000, 27.8),
                    ElectionResultItem("3", "Economic Freedom Fighters (EFF)", 1890000, 16.8),
                    ElectionResultItem("4", "Inkatha Freedom Party (IFP)", 540000, 4.8),
                    ElectionResultItem("5", "Freedom Front Plus (VF Plus)", 380000, 3.4),
                    ElectionResultItem("6", "ActionSA", 290000, 2.6),
                    ElectionResultItem("7", "Other Parties", 490000, 4.4)
                ).sortedByDescending { data -> data.votes }

                val total = sampleResults.sumOf { item -> item.votes }
                _uiState.value = ResultsUiState.Loaded(
                    electionName = "2026 Local Government Election",
                    results = sampleResults,
                    totalVotes = total
                )
            } catch (e: Exception) {
                _uiState.value = ResultsUiState.Error(e.message ?: "Failed to load results.")
            }
        }
    }
}

/**
 * Results screen component
 * Displays certified vote counts, percentage shares, progress bars per party,
 * and handles published vs. unpublished election result states.
 */
@Composable
fun ResultsScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ResultsViewModel = viewModel(),
    selectedTab: BottomTab = BottomTab.Home,
    onTabSelected: (BottomTab) -> Unit = {},
    backgroundColor: Color = PageBackground
) {
    val state by viewModel.uiState.collectAsState()
    ResultsScreen(
        uiState = state,
        onRetry = { viewModel.loadResults() },
        onBackClick = onBackClick,
        modifier = modifier,
        selectedTab = selectedTab,
        onTabSelected = onTabSelected,
        backgroundColor = backgroundColor
    )
}

@Composable
fun ResultsScreen(
    uiState: ResultsUiState,
    onRetry: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    selectedTab: BottomTab = BottomTab.Home,
    onTabSelected: (BottomTab) -> Unit = {},
    backgroundColor: Color = PageBackground
) {
    Scaffold(
        modifier = modifier,
        containerColor = backgroundColor,
        topBar = {
            AppTopBar(
                title = "Election Results",
                subtitle = "Official verified ballot results",
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
                is ResultsUiState.Loading -> {
                    IndeterminateLinearIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        label = "Loading certified election results..."
                    )
                }
                is ResultsUiState.Error -> {
                    StatusLabel(message = s.message, type = StatusType.Error)
                    SimpleButton(
                        label = stringResource(R.string.action_try_again),
                        onClick = onRetry,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                is ResultsUiState.NotPublished -> {
                    StatusLabel(
                        message = "Results not published yet. Please check back later when the election results are finalized and published by the IEC.",
                        type = StatusType.Warning
                    )
                }
                is ResultsUiState.Loaded -> {
                    SectionHeading(
                        title = s.electionName,
                        description = "Certified Vote Count • Total Votes: ${formatNumber(s.totalVotes)}"
                    )

                    s.results.forEach { item ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium,
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(Spacing.card),
                                verticalArrangement = Arrangement.spacedBy(Spacing.tight)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = item.partyName,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = "${String.format(Locale.US, "%.1f", item.percentage)}%",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                LinearProgressIndicator(
                                    progress = { (item.percentage / 100.0).toFloat().coerceIn(0f, 1f) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Votes: ${formatNumber(item.votes)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatNumber(number: Int): String {
    return java.text.NumberFormat.getInstance(Locale.US).format(number)
}

@Preview(showBackground = true)
@Composable
private fun ResultsScreenPreview() {
    FrontendTheme {
        ResultsScreen(
            uiState = ResultsUiState.Loaded(
                electionName = "2026 Local Government Election",
                results = listOf(
                    ElectionResultItem("1", "ANC", 4521000, 40.2),
                    ElectionResultItem("2", "DA", 3120000, 27.8),
                    ElectionResultItem("3", "EFF", 1890000, 16.8)
                ),
                totalVotes = 9531000
            ),
            onRetry = {},
            onBackClick = {}
        )
    }
}
