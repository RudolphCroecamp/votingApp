package com.example.frontend.ui.screens.Manifestos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.frontend.data.Party
import com.example.frontend.data.PartyRepository
import com.example.frontend.data.SamplePartyRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface PartiesUiState {
    data object Loading : PartiesUiState
    data class Loaded(val parties: List<Party>) : PartiesUiState
    data object Error : PartiesUiState
}

fun PartiesUiState.partyById(id: String): Party? =
    (this as? PartiesUiState.Loaded)?.parties?.firstOrNull { it.id == id }

/** Loads the party list once for the Manifestos tab and party detail (KAN-25). */
class PartiesViewModel(
    private val repository: PartyRepository = SamplePartyRepository
) : ViewModel() {

    private val _state = MutableStateFlow<PartiesUiState>(PartiesUiState.Loading)
    val state: StateFlow<PartiesUiState> = _state.asStateFlow()

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
}
