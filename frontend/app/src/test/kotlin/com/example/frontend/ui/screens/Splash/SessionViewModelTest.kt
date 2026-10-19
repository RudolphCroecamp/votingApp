package com.example.frontend.ui.screens.Splash

import android.os.Looper
import com.example.frontend.data.auth.AuthException
import com.example.frontend.data.auth.AuthRepository
import com.example.frontend.data.auth.RegistrationForm
import com.example.frontend.data.auth.TokenStore
import com.example.frontend.data.auth.VoterProfile
import com.example.frontend.data.health.HealthRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SessionViewModelTest {

    private class MemoryTokenStore(override var token: String? = null) : TokenStore {
        override fun save(token: String) {
            this.token = token
        }

        override fun clear() {
            token = null
        }
    }

    private class FakeAuth(
        private val valid: Set<String> = emptySet(),
        private val refreshed: String? = null,
        private val failure: AuthException? = null,
        private val profiles: Map<String, VoterProfile> = emptyMap()
    ) : AuthRepository {
        override suspend fun profile(token: String) = profiles[token]
        val loggedOut = mutableListOf<String>()

        override suspend fun login(idNumber: String, faceEmbedding: DoubleArray) = error("unused")
        override suspend fun register(form: RegistrationForm) = error("unused")
        override suspend fun validate(token: String): Boolean {
            failure?.let { throw it }
            return token in valid
        }
        override suspend fun refresh(token: String) = refreshed
        override suspend fun logout(token: String) {
            loggedOut += token
        }
    }

    private fun idle() = shadowOf(Looper.getMainLooper()).idle()

    private fun check(
        auth: AuthRepository,
        tokens: TokenStore,
        healthy: Boolean = true
    ): SessionState {
        val vm = SessionViewModel(auth, { healthy }, tokens)
        vm.check()
        idle()
        return vm.state.value
    }

    @Test
    fun `unreachable server is offline`() {
        assertEquals(SessionState.Offline, check(FakeAuth(), MemoryTokenStore("t"), healthy = false))
    }

    @Test
    fun `no stored token is signed out`() {
        assertEquals(SessionState.SignedOut, check(FakeAuth(), MemoryTokenStore()))
    }

    @Test
    fun `valid token is signed in`() {
        assertEquals(SessionState.SignedIn, check(FakeAuth(valid = setOf("t")), MemoryTokenStore("t")))
    }

    @Test
    fun `expired token is refreshed and saved`() {
        val tokens = MemoryTokenStore("old")
        assertEquals(SessionState.SignedIn, check(FakeAuth(refreshed = "new"), tokens))
        assertEquals("new", tokens.token)
    }

    @Test
    fun `token that cannot be refreshed is cleared`() {
        val tokens = MemoryTokenStore("old")
        assertEquals(SessionState.SignedOut, check(FakeAuth(), tokens))
        assertNull(tokens.token)
    }

    @Test
    fun `server error during validation is offline`() {
        val auth = FakeAuth(failure = AuthException.Server("boom"))
        assertEquals(SessionState.Offline, check(auth, MemoryTokenStore("t")))
    }

    @Test
    fun `sign out clears the token and tells the server`() {
        val auth = FakeAuth()
        val tokens = MemoryTokenStore("t")
        val vm = SessionViewModel(auth, HealthRepository { true }, tokens)
        vm.signOut()
        idle()
        assertNull(tokens.token)
        assertEquals(listOf("t"), auth.loggedOut)
        assertEquals(SessionState.SignedOut, vm.state.value)
    }

    @Test
    fun `signing in loads the voter's own details and signing out forgets them`() {
        val adriaan = VoterProfile("0412305800081", "Adriaan", "van Wyk", "Eastern Cape", "Gqeberha", "12 Main Road", "6070")
        val tokens = MemoryTokenStore("t")
        val vm = SessionViewModel(FakeAuth(valid = setOf("t"), profiles = mapOf("t" to adriaan)), { true }, tokens)

        vm.onSignedIn()
        idle()
        assertEquals(adriaan, vm.profile.value)
        assertEquals("Adriaan van Wyk", vm.profile.value?.fullName)

        vm.signOut()
        idle()
        assertNull(vm.profile.value)
    }

    @Test
    fun `a stored session loads the profile on start`() {
        val adriaan = VoterProfile("0412305800081", "Adriaan", "van Wyk", "Eastern Cape", "Gqeberha", "12 Main Road", "6070")
        val vm = SessionViewModel(FakeAuth(valid = setOf("t"), profiles = mapOf("t" to adriaan)), { true }, MemoryTokenStore("t"))
        vm.check()
        idle()
        assertEquals(adriaan, vm.profile.value)
    }
}
