package com.example.frontend.data.identity

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

enum class DocumentKind { SmartIdCard, GreenIdBook, Passport, Unknown }

/** What the ID card identifier found on the front of the document. */
data class DocumentScan(val kind: DocumentKind, val idNumber: String?) {
    val looksLikeSaDocument: Boolean get() = kind != DocumentKind.Unknown || idNumber != null
}

fun interface IdDocumentReader {
    suspend fun read(photo: Bitmap): DocumentScan
}

/** On-device OCR with ML Kit's bundled Latin model: nothing leaves the phone. */
class MlKitIdDocumentReader : IdDocumentReader {
    override suspend fun read(photo: Bitmap): DocumentScan {
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        return try {
            val text = suspendCancellableCoroutine { cont ->
                recognizer.process(InputImage.fromBitmap(photo, 0))
                    .addOnSuccessListener { cont.resume(it.text) }
                    .addOnFailureListener { cont.resume("") }
            }
            identifyDocument(text)
        } finally {
            recognizer.close()
        }
    }
}

private val SMART_CARD_WORDS = listOf("IDENTITY CARD", "NATIONALITY", "COUNTRY OF BIRTH", "IDENTITY NUMBER", "STATUS", "CITIZEN")
private val GREEN_BOOK_WORDS = listOf("IDENTITY DOCUMENT", "IDENTITEITSDOKUMENT", "I.D.NO", "I.D. NO", "SUID-AFRIKA", "BURGER")
private val PASSPORT_WORDS = listOf("PASSPORT", "PASPOORT", "P<ZAF")
private val COUNTRY_WORDS = listOf("REPUBLIC OF SOUTH AFRICA", "SOUTH AFRICA", "RSA", "ZAF")

/** OCR often reads these letters where the card has digits. */
private val DIGIT_LOOKALIKES = mapOf('O' to '0', 'o' to '0', 'D' to '0', 'I' to '1', 'l' to '1', '|' to '1', 'S' to '5', 'B' to '8', 'Z' to '2')

/**
 * Decides what kind of document the OCR text came from and pulls out a valid SA ID
 * number (13 digits passing the date and Luhn checks), tolerating spaces and the usual
 * letter-for-digit misreads.
 */
fun identifyDocument(text: String): DocumentScan {
    val upper = text.uppercase()
    fun score(words: List<String>) = words.count { it in upper }
    val country = score(COUNTRY_WORDS) > 0
    val kind = when {
        score(PASSPORT_WORDS) > 0 && country -> DocumentKind.Passport
        score(GREEN_BOOK_WORDS) >= 1 && country -> DocumentKind.GreenIdBook
        score(SMART_CARD_WORDS) >= 2 && country -> DocumentKind.SmartIdCard
        else -> DocumentKind.Unknown
    }
    return DocumentScan(kind, findIdNumber(text))
}

internal fun findIdNumber(text: String): String? {
    val candidates = text.lines().flatMap { line ->
        // Keep digits and look-alikes, drop spacing, then slide a 13-digit window along.
        val runs = Regex("[0-9OoDIl|SBZ ]{13,}").findAll(line).map { m ->
            m.value.filter { it != ' ' }.map { DIGIT_LOOKALIKES[it] ?: it }.joinToString("")
        }
        runs.flatMap { run -> (0..run.length - SA_ID_DIGITS).asSequence().map { run.substring(it, it + SA_ID_DIGITS) } }.toList()
    }
    return candidates.firstOrNull { SaIdNumber.parse(it) is SaIdResult.Valid }
}
