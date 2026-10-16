package com.example.frontend.ui.screens.Vote

import androidx.lifecycle.SavedStateHandle
import com.example.frontend.data.SampleParties
import com.example.frontend.ui.components.BALLOT_ORDER
import com.example.frontend.ui.components.Ballot
import com.example.frontend.ui.screens.Auth.FACE_MAX_ATTEMPTS
import com.example.frontend.ui.screens.Auth.LoginFaceUiState
import com.example.frontend.ui.screens.Auth.checking
import com.example.frontend.ui.screens.Auth.matched
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class ThreeBallotFlowTest {

    private val allPicked = VoteFlowState()
        .select(Ballot.National, "anc")
        .select(Ballot.Regional, "da")
        .select(Ballot.Provincial, "eff")

    // --- KAN-18: ballot order -------------------------------------------------

    @Test
    fun `ballots run national then regional then provincial`() {
        assertEquals(listOf(Ballot.National, Ballot.Regional, Ballot.Provincial), BALLOT_ORDER)
        assertEquals(Ballot.Regional, nextBallotAfter(Ballot.National))
        assertEquals(Ballot.Provincial, nextBallotAfter(Ballot.Regional))
        assertNull(nextBallotAfter(Ballot.Provincial))
    }

    @Test
    fun `flow is complete only when every ballot has a pick`() {
        val partial = VoteFlowState().select(Ballot.National, "anc").select(Ballot.Regional, "da")

        assertFalse(partial.isComplete)
        assertEquals(Ballot.Provincial, partial.firstUnfilledBallot)
        assertTrue(allPicked.isComplete)
        assertNull(allPicked.firstUnfilledBallot)
    }

    @Test
    fun `changing a ballot replaces only that pick`() {
        val changed = allPicked.select(Ballot.Regional, "ifp")

        assertEquals("anc", changed.selectionFor(Ballot.National))
        assertEquals("ifp", changed.selectionFor(Ballot.Regional))
        assertEquals("eff", changed.selectionFor(Ballot.Provincial))
    }

    @Test
    fun `review shows the right party for each ballot`() {
        val resolved = allPicked.selections.resolveParties(SampleParties)

        assertEquals("anc", resolved.getValue(Ballot.National).id)
        assertEquals("da", resolved.getValue(Ballot.Regional).id)
        assertEquals("eff", resolved.getValue(Ballot.Provincial).id)
    }

    @Test
    fun `unknown party ids are dropped instead of crashing review`() {
        val resolved = mapOf(Ballot.National to "gone").resolveParties(SampleParties)
        assertTrue(resolved.isEmpty())
    }

    // --- KAN-18: receipts -----------------------------------------------------

    @Test
    fun `casting the vote gives one receipt per ballot and forgets the picks`() {
        val receipts = BALLOT_ORDER.map { BallotReceipt(it, generateReceiptCode(Random(1)), "now") }
        val cast = allPicked.submitted(receipts)

        assertTrue(cast.hasVoted)
        assertEquals(BALLOT_ORDER, cast.receipts.map { it.ballot })
        assertTrue("picks must not be kept after voting", cast.selections.isEmpty())
    }

    @Test
    fun `an incomplete ballot set cannot be submitted`() {
        val partial = VoteFlowState().select(Ballot.National, "anc")
        val result = partial.submitted(listOf(BallotReceipt(Ballot.National, "X", "now")))

        assertFalse(result.hasVoted)
        assertEquals(partial, result)
    }

    @Test
    fun `picks cannot change after the vote is cast`() {
        val cast = allPicked.submitted(listOf(BallotReceipt(Ballot.National, "X", "now")))
        assertEquals(cast, cast.select(Ballot.National, "da"))
    }

    @Test
    fun `receipt codes use the four by four format without look-alike characters`() {
        val random = Random(42)
        repeat(200) {
            val code = generateReceiptCode(random)
            assertTrue(code, Regex("^[A-Z2-9]{4}(-[A-Z2-9]{4}){3}$").matches(code))
            assertFalse(code, code.any { it in "01OI" })
        }
    }

    // --- KAN-19: face check outcome -------------------------------------------

    @Test
    fun `a failed face check returns an error and counts the attempt`() {
        val failed = allPicked.faceCheckFailed()

        assertEquals(1, failed.faceAttemptsUsed)
        assertEquals(FACE_MAX_ATTEMPTS - 1, failed.faceAttemptsLeft)
        assertNotNull(failed.reviewError)
        assertEquals(ReviewError.FaceMismatch, failed.reviewError)
        assertTrue("picks survive a failed check", failed.isComplete)
        assertFalse(failed.hasVoted)
    }

    @Test
    fun `too many failed face checks lock confirming`() {
        var state = allPicked
        repeat(FACE_MAX_ATTEMPTS) { state = state.faceCheckFailed() }

        assertTrue(state.isFaceLocked)
        assertEquals(ReviewError.FaceLocked, state.reviewError)
    }

    @Test
    fun `picking again clears the review error`() {
        val failed = allPicked.faceCheckFailed()
        assertNull(failed.select(Ballot.National, "da").reviewError)
        assertNull(failed.reviewErrorCleared().reviewError)
    }

    @Test
    fun `face screen reports matched and rejected exactly`() {
        val start = LoginFaceUiState(idNumber = "9201185082089")

        assertNull(start.voteOutcome())
        assertNull(start.checking().voteOutcome())
        assertEquals(VoteFaceOutcome.Matched, start.checking().matched().voteOutcome())
        assertEquals(VoteFaceOutcome.Rejected, start.checking().voteRejected().voteOutcome())
    }

    @Test
    fun `a rejected scan does not lock the face screen itself`() {
        val rejected = LoginFaceUiState(idNumber = "1").checking().voteRejected()
        assertFalse(rejected.isLocked)
        assertFalse(rejected.isChecking)
    }

    // --- KAN-17: state survives -----------------------------------------------

    @Test
    fun `picks survive the app being killed and restored`() {
        val handle = SavedStateHandle()
        VoteFlowViewModel(handle).apply {
            select(Ballot.National, "anc")
            select(Ballot.Regional, "da")
        }

        // A new ViewModel built from the same saved state = process death + restore.
        val restored = VoteFlowViewModel(handle).state.value

        assertEquals("anc", restored.selectionFor(Ballot.National))
        assertEquals("da", restored.selectionFor(Ballot.Regional))
        assertEquals(Ballot.Provincial, restored.firstUnfilledBallot)
    }

    @Test
    fun `passing the face check produces three receipts and survives restore`() {
        val handle = SavedStateHandle()
        val viewModel = VoteFlowViewModel(handle)
        BALLOT_ORDER.zip(listOf("anc", "da", "eff")).forEach { (b, p) -> viewModel.select(b, p) }

        viewModel.onFaceCheckPassed(castAt = "2026-10-15 10:00", random = Random(7))

        val restored = VoteFlowViewModel(handle).state.value
        assertEquals(3, restored.receipts.size)
        assertEquals(3, restored.receipts.map { it.code }.toSet().size)
        assertTrue(restored.selections.isEmpty())
    }

    @Test
    fun `corrupt saved state falls back to a fresh flow`() {
        val handle = SavedStateHandle(mapOf("vote_flow_state" to "not json"))
        assertEquals(VoteFlowState(), VoteFlowViewModel(handle).state.value)
    }
}
