package com.example.frontend.data

/**
 * Where the app gets the party list (KAN-25). Screens depend on this, never on
 * [SampleParties] directly, so the backend can be plugged in without touching UI.
 */
fun interface PartyRepository {
    /** Every party on the ballot. Throws when the list can't be loaded. */
    suspend fun parties(): List<Party>

    suspend fun party(id: String): Party? = parties().firstOrNull { it.id == id }
}

/** Serves the built-in [SampleParties]; used when the backend can't be reached. */
object SamplePartyRepository : PartyRepository {
    override suspend fun parties(): List<Party> = SampleParties
}
