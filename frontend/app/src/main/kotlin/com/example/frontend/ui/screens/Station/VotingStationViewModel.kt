package com.example.frontend.ui.screens.Station

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.frontend.data.AppContainer
import com.example.frontend.data.address.AddressLocator
import com.example.frontend.data.address.GeoPoint
import com.example.frontend.data.station.StationLookupException
import com.example.frontend.data.station.VotingStation
import com.example.frontend.data.station.VotingStationFinder
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val TAG = "VotingStation"

sealed interface StationUiState {
    data object Locating : StationUiState
    /** Nothing on file to search from (e.g. profile still loading or never entered). */
    data object NoAddress : StationUiState
    /** The geocoder could not place the registered address on the map. */
    data object AddressNotFound : StationUiState
    data class NoneNearby(val home: GeoPoint) : StationUiState
    data object Failed : StationUiState
    data class Found(
        val home: GeoPoint,
        val stations: List<VotingStation>,
        val selected: Int = 0
    ) : StationUiState {
        val station: VotingStation get() = stations[selected]
    }
}

class VotingStationViewModel(
    private val locator: AddressLocator = AppContainer.addressLocator,
    private val finder: VotingStationFinder = AppContainer.votingStationFinder
) : ViewModel() {

    private val _state = MutableStateFlow<StationUiState>(StationUiState.Locating)
    val state: StateFlow<StationUiState> = _state.asStateFlow()

    private var lastAddress: String? = null
    private var job: Job? = null

    /** Searches around [address]; a repeat call with the same address keeps the current result. */
    fun load(address: String, force: Boolean = false) {
        if (!force && address == lastAddress && _state.value !is StationUiState.Failed) return
        lastAddress = address
        job?.cancel()
        if (address.isBlank()) {
            _state.value = StationUiState.NoAddress
            return
        }
        _state.value = StationUiState.Locating
        job = viewModelScope.launch {
            _state.value = try {
                val home = locator.locate(address)
                when {
                    home == null -> StationUiState.AddressNotFound
                    else -> finder.nearby(home).let { stations ->
                        if (stations.isEmpty()) StationUiState.NoneNearby(home) else StationUiState.Found(home, stations)
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: StationLookupException) {
                Log.w(TAG, "Station lookup failed", e)
                StationUiState.Failed
            }
        }
    }

    fun retry() = lastAddress?.let { load(it, force = true) }

    fun select(index: Int) = _state.update { state ->
        if (state is StationUiState.Found && index in state.stations.indices) state.copy(selected = index) else state
    }
}

/** One line the geocoder understands, built from the registration fields. */
fun registeredAddressLine(street: String, city: String, postalCode: String, province: String): String {
    val parts = listOf(street, city, postalCode, province).map { it.trim() }.filter { it.isNotEmpty() }
    // A province alone is too vague to place a voter near a station.
    if (street.isBlank() && city.isBlank()) return ""
    return (parts + "South Africa").joinToString(", ")
}
