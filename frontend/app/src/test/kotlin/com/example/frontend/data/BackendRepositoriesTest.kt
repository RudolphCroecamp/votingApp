package com.example.frontend.data

import com.example.frontend.data.blockchain.ChainStatus
import com.example.frontend.data.blockchain.HttpBlockchainRepository
import com.example.frontend.data.vote.HttpVoteRepository
import com.example.frontend.data.vote.VoteException
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Parses the real backend JSON shapes without touching the network. */
class BackendRepositoriesTest {

    private val requests = mutableListOf<Request>()

    private fun client(routes: Map<String, Pair<Int, String>>) = OkHttpClient.Builder()
        .addInterceptor(Interceptor { chain ->
            val request = chain.request()
            requests += request
            val (code, body) = routes.entries.first { request.url.encodedPath.endsWith(it.key) }.value
            Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(code)
                .message("")
                .body(body.toResponseBody("application/json".toMediaType()))
                .build()
        })
        .build()

    private fun Request.bodyText(): String = Buffer().also { body!!.writeTo(it) }.readUtf8()

    @Test
    fun `cast vote fetches a token then posts it with the party`() = runBlocking {
        val repo = HttpVoteRepository(
            "http://api/", token = { "jwt" },
            client = client(
                mapOf(
                    "get_vote_token/e1" to (200 to """{"message":"Success","token":{"token_id":"t1","token":"abc","issuedAt":"2026-10-19T12:00:00Z"}}"""),
                    "cast_vote" to (200 to """{"message":"Vote broadcasted successfully","broadCastSuccess":{"messages":["ok","ok"],"status":[true,true]},"receipt":{"election_id":"e1","hasVoted":true,"voted_at":"2026-10-19T12:00:01Z"}}""")
                )
            )
        )

        val result = repo.castVote("e1", "p1")

        assertEquals("e1", result.electionId)
        assertTrue(result.allNodesAccepted)
        assertEquals("Bearer jwt", requests[1].header("Authorization"))
        val posted = requests[1].bodyText()
        assertTrue(posted, posted.contains("\"token_id\":\"t1\"") && posted.contains("\"party_id\":\"p1\""))
    }

    @Test(expected = VoteException.NotSignedIn::class)
    fun `voting without a session fails before calling the backend`(): Unit = runBlocking {
        HttpVoteRepository("http://api/", token = { null }, client = client(emptyMap())).isEligible("e1")
    }

    @Test(expected = VoteException.NotSignedIn::class)
    fun `a forbidden response means the voter must sign in again`(): Unit = runBlocking {
        HttpVoteRepository(
            "http://api/", token = { "jwt" },
            client = client(mapOf("IsUserEligibleToVote/e1" to (403 to "")))
        ).isEligible("e1")
    }

    @Test
    fun `unknown vote id verifies as not found`() = runBlocking {
        val repo = HttpVoteRepository(
            "http://api/", token = { "jwt" },
            client = client(mapOf("verify_vote/v1" to (500 to "Vote not found")))
        )
        assertNull(repo.verifyVote("v1"))
    }

    @Test
    fun `blockchain status combines node health and chain validity`() = runBlocking {
        val repo = HttpBlockchainRepository(
            "http://api/",
            client = client(
                mapOf(
                    "Health/nodes" to (200 to """{"status":"Healthy","totalNodes":4,"healthyNodes":3,"nodes":[]}"""),
                    "IsBlockchainValid" to (200 to """{"message":"Blockchain data","isBlockChainValid":"Some nodes' blockchains are invalid"}""")
                )
            )
        )
        assertEquals(ChainStatus.Known(healthyNodes = 3, totalNodes = 4, chainValid = false), repo.status())
    }

    @Test
    fun `blockchain is unavailable when node health can't be read`() = runBlocking {
        val repo = HttpBlockchainRepository(
            "http://api/",
            client = client(mapOf("Health/nodes" to (503 to ""), "IsBlockchainValid" to (503 to "")))
        )
        assertEquals(ChainStatus.Unavailable, repo.status())
    }
}
