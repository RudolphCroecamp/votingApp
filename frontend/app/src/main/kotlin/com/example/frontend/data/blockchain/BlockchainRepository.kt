package com.example.frontend.data.blockchain

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.concurrent.TimeUnit

sealed interface ChainStatus {
    data object Checking : ChainStatus
    data object Unavailable : ChainStatus
    data class Known(val healthyNodes: Int, val totalNodes: Int, val chainValid: Boolean?) : ChainStatus
}

fun interface BlockchainRepository {
    suspend fun status(): ChainStatus
}

@Serializable
private data class NodesResponse(val totalNodes: Int = 0, val healthyNodes: Int = 0)

@Serializable
private data class ChainValidResponse(val isBlockChainValid: String? = null)

class HttpBlockchainRepository(
    baseUrl: String,
    private val client: OkHttpClient = OkHttpClient.Builder()
        .callTimeout(20, TimeUnit.SECONDS)
        .build(),
    private val json: Json = Json { ignoreUnknownKeys = true }
) : BlockchainRepository {

    private val apiUrl = baseUrl.trimEnd('/') + "/api"

    override suspend fun status(): ChainStatus = coroutineScope {
        val nodes = async { get<NodesResponse>("$apiUrl/Health/nodes") }
        val chain = async { get<ChainValidResponse>("$apiUrl/Blockchain/IsBlockchainValid") }
        val nodeInfo = nodes.await() ?: return@coroutineScope ChainStatus.Unavailable
        ChainStatus.Known(
            healthyNodes = nodeInfo.healthyNodes,
            totalNodes = nodeInfo.totalNodes,
            chainValid = chain.await()?.isBlockChainValid?.let { it.startsWith("All", ignoreCase = true) }
        )
    }

    private suspend inline fun <reified T> get(url: String): T? = withContext(Dispatchers.IO) {
        try {
            client.newCall(Request.Builder().url(url).get().build()).execute().use { response ->
                if (!response.isSuccessful) null
                else runCatching { json.decodeFromString<T>(response.body.string()) }.getOrNull()
            }
        } catch (e: IOException) {
            null
        }
    }
}
