package com.example.frontend.data.auth

import java.io.File

interface AuthRepository {
    suspend fun login(idNumber: String, faceEmbedding: DoubleArray): String

    suspend fun register(form: RegistrationForm)

    suspend fun validate(token: String): Boolean

    suspend fun refresh(token: String): String?

    suspend fun logout(token: String)

    suspend fun profile(token: String): VoterProfile? = null
}

data class VoterProfile(
    val idNumber: String,
    val firstName: String,
    val lastName: String,
    val province: String,
    val city: String,
    val street: String,
    val postalCode: String,
    val phone: String = ""
) {
    val fullName: String
        get() = listOf(firstName, lastName).filter { it.isNotBlank() }.joinToString(" ")
}

class RegistrationForm(
    val idNumber: String,
    val firstName: String,
    val lastName: String,
    val province: String,
    val city: String,
    val street: String,
    val postalCode: Int,
    val phone: String,
    val faceEmbedding: DoubleArray,
    val idFront: File,
    val idBack: File
)

sealed class AuthException(message: String?, cause: Throwable? = null) : Exception(message, cause) {
    class InvalidCredentials(message: String?) : AuthException(message)
    class AccountBlocked(message: String?) : AuthException(message)
    class InvalidDetails(message: String?) : AuthException(message)
    class AlreadyRegistered(message: String?) : AuthException(message)
    class Network(cause: Throwable) : AuthException(cause.message, cause)
    class Server(message: String?) : AuthException(message)
}
