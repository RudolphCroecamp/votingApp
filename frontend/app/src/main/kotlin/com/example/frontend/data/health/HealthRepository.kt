package com.example.frontend.data.health

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.concurrent.TimeUnit

fun interface HealthRepository {
    suspend fun isHealthy(): Boolean
}

@Serializable
private data class HealthResponse(val status: String? = null)

class HttpHealthRepository(
    baseUrl: String,
    private val client: OkHttpClient = OkHttpClient.Builder()
        .callTimeout(10, TimeUnit.SECONDS)
        .build(),
    private val json: Json = Json { ignoreUnknownKeys = true }
) : HealthRepository {

    private val healthUrl = baseUrl.trimEnd('/') + "/api/Health/health"

    override suspend fun isHealthy(): Boolean = withContext(Dispatchers.IO) {
        try {
            client.newCall(Request.Builder().url(healthUrl).get().build()).execute().use { response ->
                if (!response.isSuccessful) return@use false
                val body = runCatching { json.decodeFromString<HealthResponse>(response.body.string()) }.getOrNull()
                body?.status.equals("Healthy", ignoreCase = true)
            }
        } catch (e: IOException) {
            false
        }
    }
}
