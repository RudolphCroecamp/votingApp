package com.example.frontend.ui.screens.Manifestos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.frontend.data.Party
import com.example.frontend.data.PartyRepository
import com.example.frontend.data.AppContainer
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface PartiesUiState {
    data object Loading : PartiesUiState
    data class Loaded(val parties: List<Party>) : PartiesUiState
    data object Error : PartiesUiState
}

sealed interface PartyDetailUiState {
    val partyId: String

    data class Loading(override val partyId: String) : PartyDetailUiState
    data class Loaded(override val partyId: String, val party: Party) : PartyDetailUiState
    data class NotFound(override val partyId: String) : PartyDetailUiState
    data class Error(override val partyId: String) : PartyDetailUiState
}

fun PartiesUiState.partyById(id: String): Party? =
    (this as? PartiesUiState.Loaded)?.parties?.firstOrNull { it.id == id }

/** Loads the party list once for the Manifestos tab and party detail (KAN-25). */
class PartiesViewModel(
    private val repository: PartyRepository = AppContainer.partyRepository
) : ViewModel() {

    private val _state = MutableStateFlow<PartiesUiState>(PartiesUiState.Loading)
    val state: StateFlow<PartiesUiState> = _state.asStateFlow()

    private val _detail = MutableStateFlow<PartyDetailUiState>(PartyDetailUiState.Loading(""))
    val detail: StateFlow<PartyDetailUiState> = _detail.asStateFlow()

    private var detailJob: Job? = null

    init {
        load()
    }

    fun retry() {
        if (_state.value != PartiesUiState.Loading) load()
    }

    private fun load() {
        _state.value = PartiesUiState.Loading
        viewModelScope.launch {
            _state.value = try {
                PartiesUiState.Loaded(repository.parties())
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                PartiesUiState.Error
            }
        }
    }

    fun openParty(id: String) {
        detailJob?.cancel()
        val cached = _state.value.partyById(id)
        _detail.value = cached?.let { PartyDetailUiState.Loaded(id, it) } ?: PartyDetailUiState.Loading(id)
        detailJob = viewModelScope.launch {
            val party = try {
                repository.party(id)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _detail.value = cached?.let { PartyDetailUiState.Loaded(id, it) } ?: PartyDetailUiState.Error(id)
                return@launch
            }
            _detail.value = (party ?: cached)?.let { PartyDetailUiState.Loaded(id, it) }
                ?: PartyDetailUiState.NotFound(id)
        }
    }
}
