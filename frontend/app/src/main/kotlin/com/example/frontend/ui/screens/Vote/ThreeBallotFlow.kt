package com.example.frontend.ui.screens.Vote

import androidx.lifecycle.SavedStateHandle
import com.example.frontend.data.Party
import androidx.lifecycle.ViewModel
import com.example.frontend.ui.components.BALLOT_ORDER
import com.example.frontend.ui.components.Ballot
import com.example.frontend.ui.screens.Auth.FACE_MAX_ATTEMPTS
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.random.Random

/*
 * Three-ballot vote flow (KAN-17, KAN-18, KAN-19 / requirements V-08, V-10).
 *
 * Decision (KAN-17): the picks live in a shared [VoteFlowViewModel] scoped to the
 * activity, not in nav-graph state. One owner for the whole flow means:
 *  - Back between ballots and "Change" from Review keep every pick.
 *  - The state is mirrored into SavedStateHandle, so it survives the OS killing
 *    the app in the background.
 *  - Routes stay small: they carry only which ballot to show, never a party.
 */

@Serializable
data class BallotReceipt(
    val ballot: Ballot,
    val code: String,
    val castAt: String
)

/** Why Review is showing an error. The screen turns this into translated text. */
@Serializable
enum class ReviewError {
    /** Face check failed; the voter still has attempts left. */
    FaceMismatch,
    /** Face check failed too many times; confirming is blocked. */
    FaceLocked
}

@Serializable
data class VoteFlowState(
    val selections: Map<Ballot, String> = emptyMap(),
    val receipts: List<BallotReceipt> = emptyList(),
    val faceAttemptsUsed: Int = 0,
    val reviewError: ReviewError? = null
) {
    /** True once every ballot has a party picked. */
    val isComplete: Boolean
        get() = BALLOT_ORDER.all { it in selections }

    /** The first ballot still missing a pick, or null when all are done. */
    val firstUnfilledBallot: Ballot?
        get() = BALLOT_ORDER.firstOrNull { it !in selections }

    val hasVoted: Boolean
        get() = receipts.isNotEmpty()

    val faceAttemptsLeft: Int
        get() = (FACE_MAX_ATTEMPTS - faceAttemptsUsed).coerceAtLeast(0)

    /** Too many failed face checks: confirming is blocked for this session. */
    val isFaceLocked: Boolean
        get() = faceAttemptsLeft == 0

    fun selectionFor(ballot: Ballot): String? = selections[ballot]
}

/** Ballot that follows [ballot], or null after the last one. */
fun nextBallotAfter(ballot: Ballot): Ballot? =
    BALLOT_ORDER.getOrNull(BALLOT_ORDER.indexOf(ballot) + 1)

fun VoteFlowState.select(ballot: Ballot, partyId: String): VoteFlowState =
    if (hasVoted) this else copy(selections = selections + (ballot to partyId), reviewError = null)

fun VoteFlowState.faceCheckFailed(): VoteFlowState {
    val used = faceAttemptsUsed + 1
    val left = (FACE_MAX_ATTEMPTS - used).coerceAtLeast(0)
    val error = if (left == 0) ReviewError.FaceLocked else ReviewError.FaceMismatch
    return copy(faceAttemptsUsed = used, reviewError = error)
}

/**
 * Face check passed: record one receipt per ballot and forget the picks.
 * The app must not keep who the voter chose once the vote is cast.
 */
fun VoteFlowState.submitted(receipts: List<BallotReceipt>): VoteFlowState {
    if (!isComplete || hasVoted) return this
    return VoteFlowState(receipts = receipts)
}

fun VoteFlowState.reviewErrorCleared(): VoteFlowState = copy(reviewError = null)

private const val RECEIPT_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789" // no 0/O or 1/I

/** Receipt code in the existing XXXX-XXXX-XXXX-XXXX shape. */
fun generateReceiptCode(random: Random = Random.Default): String =
    (1..4).joinToString("-") {
        (1..4).map { RECEIPT_ALPHABET[random.nextInt(RECEIPT_ALPHABET.length)] }.joinToString("")
    }

class VoteFlowViewModel(
    private val savedState: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(restore())
    val state: StateFlow<VoteFlowState> = _state.asStateFlow()

    fun select(ballot: Ballot, partyId: String) = update { it.select(ballot, partyId) }

    fun onFaceCheckFailed() = update { it.faceCheckFailed() }

    fun clearReviewError() = update { it.reviewErrorCleared() }

    /**
     * Face check passed. Builds one receipt per ballot. Submission to the backend
     * plugs in here once the vote endpoint exists.
     */
    fun onFaceCheckPassed(castAt: String, random: Random = Random.Default) = update { state ->
        state.submitted(BALLOT_ORDER.map { BallotReceipt(it, generateReceiptCode(random), castAt) })
    }

    private inline fun update(transform: (VoteFlowState) -> VoteFlowState) {
        val next = transform(_state.value)
        _state.value = next
        savedState[STATE_KEY] = json.encodeToString(VoteFlowState.serializer(), next)
    }

    private fun restore(): VoteFlowState =
        savedState.get<String>(STATE_KEY)
            ?.let { runCatching { json.decodeFromString(VoteFlowState.serializer(), it) }.getOrNull() }
            ?: VoteFlowState()

    private companion object {
        const val STATE_KEY = "vote_flow_state"
        val json = Json { ignoreUnknownKeys = true }
    }
}

/** Preview/demo receipts so screens render before a vote is cast. */
val SampleReceipts: List<BallotReceipt> = listOf(
    BallotReceipt(Ballot.National, "AB7F-92K4-6ZP1-3QX8", "2026-05-27 09:42"),
    BallotReceipt(Ballot.Regional, "KD3M-7WQ2-PX9H-4TNA", "2026-05-27 09:42"),
    BallotReceipt(Ballot.Provincial, "R8ZC-5EVB-2HJL-9YMF", "2026-05-27 09:42")
)

/** Turns stored party IDs into parties; IDs no longer in [parties] are dropped. */
fun Map<Ballot, String>.resolveParties(parties: List<Party>): Map<Ballot, Party> =
    mapNotNull { (ballot, id) -> parties.firstOrNull { it.id == id }?.let { ballot to it } }.toMap()
