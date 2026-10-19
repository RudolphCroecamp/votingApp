package com.example.frontend.data.election

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.time.LocalDate
import java.time.format.DateTimeParseException
import java.util.concurrent.TimeUnit

/** One election as the admin scheduled it. Voting runs from [startDate] to [endDate], inclusive. */
data class Election(
    val id: String,
    val name: String,
    val type: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val isActive: Boolean
) {
    fun phase(today: LocalDate): ElectionPhase = when {
        today.isBefore(startDate) -> ElectionPhase.Upcoming
        today.isAfter(endDate) -> ElectionPhase.Ended
        else -> ElectionPhase.Open
    }
}

enum class ElectionPhase { Upcoming, Open, Ended }

/** The election the home screen should talk about: one that is open now, else the next to open. */
fun List<Election>.nextRelevant(today: LocalDate): Election? =
    firstOrNull { it.phase(today) == ElectionPhase.Open }
        ?: filter { it.phase(today) == ElectionPhase.Upcoming }.minByOrNull { it.startDate }

class ElectionException(message: String?, cause: Throwable? = null) : Exception(message, cause)

fun interface ElectionRepository {
    /** Elections that have not ended yet, soonest first. */
    suspend fun upcoming(): List<Election>
}

@Serializable
private data class ElectionDto(
    @SerialName("election_id") val id: String,
    @SerialName("election_name") val name: String = "",
    @SerialName("election_type") val type: String = "",
    @SerialName("start_date") val startDate: String,
    @SerialName("end_date") val endDate: String,
    val isActive: Boolean = false
)

@Serializable
private data class ElectionsResponse(val elections: List<ElectionDto> = emptyList())

class HttpElectionRepository(
    baseUrl: String,
    private val token: () -> String?,
    private val client: OkHttpClient = OkHttpClient.Builder()
        .callTimeout(20, TimeUnit.SECONDS)
        .build(),
    private val json: Json = Json { ignoreUnknownKeys = true; explicitNulls = false }
) : ElectionRepository {

    private val electionsUrl = baseUrl.trimEnd('/') + "/api/Elections"

    override suspend fun upcoming(): List<Election> = withContext(Dispatchers.IO) {
        val bearer = token() ?: throw ElectionException("No voter session")
        val request = Request.Builder()
            .url("$electionsUrl/upcoming")
            .header("Authorization", "Bearer $bearer")
            .get()
            .build()
        val body = try {
            client.newCall(request).execute().use { response ->
                if (response.code !in 200..299) throw ElectionException("HTTP ${response.code}")
                response.body.string()
            }
        } catch (e: IOException) {
            throw ElectionException(e.message, e)
        }
        val dtos = runCatching { json.decodeFromString<ElectionsResponse>(body).elections }
            .getOrElse { throw ElectionException("Unreadable election list", it) }
        dtos.mapNotNull { it.toElection() }.sortedBy { it.startDate }
    }
}

private fun ElectionDto.toElection(): Election? = try {
    Election(
        id = id,
        name = name,
        type = type,
        startDate = LocalDate.parse(startDate.take(10)),
        endDate = LocalDate.parse(endDate.take(10)),
        isActive = isActive
    )
} catch (e: DateTimeParseException) {
    null
}
