package com.example.frontend.data

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.UUID
import java.util.concurrent.TimeUnit

private const val TAG = "Parties"

@Serializable
private data class PartyDto(
    @SerialName("party_id") val partyId: String,
    val name: String,
    val abbreviation: String? = null,
    val description: String = "",
    val allowed: Boolean = true
)

@Serializable
private data class PartiesResponse(val parties: List<PartyDto> = emptyList())

@Serializable
private data class PartyResponse(val party: PartyDto? = null)

class HttpPartyRepository(
    baseUrl: String,
    private val token: () -> String?,
    private val fallback: PartyRepository = SamplePartyRepository,
    private val client: OkHttpClient = OkHttpClient.Builder()
        .callTimeout(10, TimeUnit.SECONDS)
        .build(),
    private val json: Json = Json { ignoreUnknownKeys = true }
) : PartyRepository {

    private val adminUrl = baseUrl.trimEnd('/') + "/api/Admin"

    override suspend fun parties(): List<Party> =
        get<PartiesResponse>("$adminUrl/get_parties?limit=100&page=0")
            ?.parties
            ?.filter { it.allowed }
            ?.map { it.toParty() }
            ?.takeIf { it.isNotEmpty() }
            ?: fallback.parties()

    override suspend fun party(id: String): Party? {
        if (!id.isUuid()) return fallback.party(id)
        return get<PartyResponse>("$adminUrl/party_by_id/$id")?.party?.toParty()
    }

    private suspend inline fun <reified T> get(url: String): T? = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(url)
            .get()
            .apply { token()?.let { header("Authorization", "Bearer $it") } }
            .build()
        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.w(TAG, "GET $url failed with ${response.code}")
                    null
                } else {
                    runCatching { json.decodeFromString<T>(response.body.string()) }
                        .onFailure { Log.w(TAG, "GET $url returned an unreadable body", it) }
                        .getOrNull()
                }
            }
        } catch (e: IOException) {
            Log.w(TAG, "GET $url failed", e)
            null
        }
    }
}

private fun String.isUuid(): Boolean = runCatching { UUID.fromString(this) }.isSuccess

private fun PartyDto.toParty(): Party {
    val local = SampleParties.firstOrNull { it.id.equals(abbreviation?.trim(), ignoreCase = true) }
    return (local ?: Party(id = partyId, name = name, slogan = ""))
        .copy(id = partyId, name = name, description = description)
}
