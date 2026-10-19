package com.example.frontend.data

import android.content.Context
import com.example.frontend.BuildConfig
import com.example.frontend.data.address.AddressSuggester
import com.example.frontend.data.address.GeocoderAddressSuggester
import com.example.frontend.data.auth.AuthRepository
import com.example.frontend.data.auth.HttpAuthRepository
import com.example.frontend.data.auth.PrefsTokenStore
import com.example.frontend.data.auth.TokenStore
import com.example.frontend.data.blockchain.BlockchainRepository
import com.example.frontend.data.blockchain.HttpBlockchainRepository
import com.example.frontend.data.address.AddressLocator
import com.example.frontend.data.address.GeocoderAddressLocator
import com.example.frontend.data.election.ElectionRepository
import com.example.frontend.data.election.HttpElectionRepository
import com.example.frontend.data.election.PrefsReminderStore
import com.example.frontend.data.election.ReminderScheduler
import com.example.frontend.data.election.ReminderStore
import com.example.frontend.data.election.WorkManagerReminderScheduler
import com.example.frontend.data.station.CachingVotingStationFinder
import com.example.frontend.data.station.FallbackVotingStationFinder
import com.example.frontend.data.station.NominatimVotingStationFinder
import com.example.frontend.data.station.OverpassVotingStationFinder
import com.example.frontend.data.station.VotingStationFinder
import com.example.frontend.data.face.FaceEmbedder
import com.example.frontend.data.face.TfliteFaceEmbedder
import com.example.frontend.data.health.HealthRepository
import com.example.frontend.data.health.HttpHealthRepository
import com.example.frontend.data.vote.HttpVoteRepository
import com.example.frontend.data.vote.VoteRepository

object AppContainer {

    private lateinit var appContext: Context

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    val authRepository: AuthRepository by lazy { HttpAuthRepository(BuildConfig.API_BASE_URL) }

    val healthRepository: HealthRepository by lazy { HttpHealthRepository(BuildConfig.API_BASE_URL) }

    val partyRepository: PartyRepository by lazy {
        HttpPartyRepository(BuildConfig.API_BASE_URL, token = { tokenStore.token })
    }

    val blockchainRepository: BlockchainRepository by lazy { HttpBlockchainRepository(BuildConfig.API_BASE_URL) }

    val voteRepository: VoteRepository by lazy {
        HttpVoteRepository(BuildConfig.API_BASE_URL, token = { tokenStore.token })
    }

    val electionRepository: ElectionRepository by lazy {
        HttpElectionRepository(BuildConfig.API_BASE_URL, token = { tokenStore.token })
    }

    val reminderStore: ReminderStore by lazy { PrefsReminderStore(appContext) }

    val reminderScheduler: ReminderScheduler by lazy { WorkManagerReminderScheduler(appContext) }

    val addressLocator: AddressLocator by lazy { GeocoderAddressLocator(appContext) }

    val votingStationFinder: VotingStationFinder by lazy {
        CachingVotingStationFinder(
            FallbackVotingStationFinder(
                listOf(
                    OverpassVotingStationFinder(appContext.packageName),
                    NominatimVotingStationFinder(appContext.packageName)
                )
            )
        )
    }

    val tokenStore: TokenStore by lazy { PrefsTokenStore(appContext) }

    val addressSuggester: AddressSuggester by lazy { GeocoderAddressSuggester(appContext) }

    val faceEmbedder: FaceEmbedder by lazy { TfliteFaceEmbedder(appContext) }
}
