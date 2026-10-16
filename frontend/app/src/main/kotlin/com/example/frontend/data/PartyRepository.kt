package com.example.frontend.data

/**
 * Where the app gets the party list (KAN-25). Screens depend on this, never on
 * [SampleParties] directly, so the backend can be plugged in without touching UI.
 */
fun interface PartyRepository {
    /** Every party on the ballot. Throws when the list can't be loaded. */
    suspend fun parties(): List<Party>
}

/**
 * Serves [SampleParties] until the backend exposes a voter-facing party endpoint
 * (today only AdminController/get_parties exists, behind admin auth).
 */
object SamplePartyRepository : PartyRepository {
    override suspend fun parties(): List<Party> = SampleParties
}
