package com.example.frontend.data.vote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

private val JSON_MEDIA = "application/json; charset=utf-8".toMediaType()

sealed class VoteException(message: String?) : Exception(message) {
    class NotSignedIn : VoteException("No voter session")
    class Rejected(message: String?) : VoteException(message)
    class Network(cause: IOException) : VoteException(cause.message)
}

data class CastVoteResult(val electionId: String, val votedAt: String, val allNodesAccepted: Boolean)

data class VoteRecord(val electionId: String, val votedAt: String)

interface VoteRepository {
    suspend fun isEligible(electionId: String): Boolean

    suspend fun castVote(electionId: String, partyId: String): CastVoteResult

    suspend fun verifyVote(voteId: String): VoteRecord?

    suspend fun requestAccess(electionId: String)
}

@Serializable
private data class VoteTokenDto(
    @SerialName("token_id") val tokenId: String,
    val token: String,
    val issuedAt: String
)

@Serializable
private data class EligibleResponse(val message: String? = null, val eligible: Boolean = false)

@Serializable
private data class VoteTokenResponse(val message: String? = null, val token: VoteTokenDto? = null)

@Serializable
private data class CastVoteRequest(
    val voteToken: VoteTokenDto,
    @SerialName("election_id") val electionId: String,
    @SerialName("party_id") val partyId: String
)

@Serializable
private data class BroadcastResult(val status: List<Boolean> = emptyList())

@Serializable
private data class Receipt(
    @SerialName("election_id") val electionId: String,
    @SerialName("voted_at") val votedAt: String
)

@Serializable
private data class CastVoteResponse(
    val message: String? = null,
    @SerialName("broadCastSuccess") val broadcast: BroadcastResult? = null,
    val receipt: Receipt? = null
)

@Serializable
private data class AccessRequest(@SerialName("election_id") val electionId: String)

@Serializable
private data class MessageResponse(val message: String? = null)

class HttpVoteRepository(
    baseUrl: String,
    private val token: () -> String?,
    private val client: OkHttpClient = OkHttpClient.Builder()
        .callTimeout(30, TimeUnit.SECONDS)
        .build(),
    private val json: Json = Json { ignoreUnknownKeys = true; explicitNulls = false }
) : VoteRepository {

    private val votesUrl = baseUrl.trimEnd('/') + "/api/Votes"

    override suspend fun isEligible(electionId: String): Boolean {
        val (code, body) = call(Request.Builder().url("$votesUrl/IsUserEligibleToVote/$electionId").get())
        if (code !in 200..299) throw VoteException.Rejected(message(body))
        return decode<EligibleResponse>(body)?.eligible ?: false
    }

    override suspend fun castVote(electionId: String, partyId: String): CastVoteResult {
        val (tokenCode, tokenBody) = call(Request.Builder().url("$votesUrl/get_vote_token/$electionId").get())
        val voteToken = decode<VoteTokenResponse>(tokenBody)?.token
        if (tokenCode !in 200..299 || voteToken == null) throw VoteException.Rejected(message(tokenBody))

        val payload = json.encodeToString(CastVoteRequest(voteToken, electionId, partyId))
        val (castCode, castBody) = call(
            Request.Builder().url("$votesUrl/cast_vote").post(payload.toRequestBody(JSON_MEDIA))
        )
        val cast = decode<CastVoteResponse>(castBody)
        val receipt = cast?.receipt
        if (castCode !in 200..299 || receipt == null) throw VoteException.Rejected(message(castBody))
        val nodes = cast.broadcast?.status.orEmpty()
        return CastVoteResult(receipt.electionId, receipt.votedAt, nodes.isNotEmpty() && nodes.all { it })
    }

    override suspend fun verifyVote(voteId: String): VoteRecord? {
        val (code, body) = call(Request.Builder().url("$votesUrl/verify_vote/$voteId").get())
        if (code == 404 || code >= 500) return null
        if (code !in 200..299) throw VoteException.Rejected(message(body))
        return decode<Receipt>(body)?.let { VoteRecord(it.electionId, it.votedAt) }
    }

    override suspend fun requestAccess(electionId: String) {
        val payload = json.encodeToString(AccessRequest(electionId))
        val (code, body) = call(
            Request.Builder().url("$votesUrl/RequestAccessToElection").post(payload.toRequestBody(JSON_MEDIA))
        )
        if (code !in 200..299) throw VoteException.Rejected(message(body))
    }

    private suspend fun call(builder: Request.Builder): Pair<Int, String> = withContext(Dispatchers.IO) {
        val bearer = token() ?: throw VoteException.NotSignedIn()
        val request = builder.header("Authorization", "Bearer $bearer").build()
        try {
            client.newCall(request).execute().use { response ->
                if (response.code == 401 || response.code == 403) throw VoteException.NotSignedIn()
                response.code to response.body.string()
            }
        } catch (e: IOException) {
            throw VoteException.Network(e)
        }
    }

    private inline fun <reified T> decode(body: String): T? =
        runCatching { json.decodeFromString<T>(body) }.getOrNull()

    private fun message(body: String): String? = decode<MessageResponse>(body)?.message ?: body.takeIf { it.isNotBlank() }
}
