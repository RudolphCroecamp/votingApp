package com.example.frontend.data.auth

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

private val JSON_MEDIA = "application/json; charset=utf-8".toMediaType()
private val JPEG_MEDIA = "image/jpeg".toMediaType()

@Serializable
private data class LoginRequest(
    @SerialName("id_number") val idNumber: String,
    @SerialName("face_embedding") val faceEmbedding: List<Double>
)

@Serializable
private data class AuthResponse(
    val message: String? = null,
    val token: String? = null
)

private class HttpResult(val code: Int, val body: AuthResponse)

class HttpAuthRepository(
    baseUrl: String,
    private val client: OkHttpClient = OkHttpClient.Builder()
        .callTimeout(30, TimeUnit.SECONDS)
        .build(),
    private val json: Json = Json { ignoreUnknownKeys = true }
) : AuthRepository {

    private val authUrl = baseUrl.trimEnd('/') + "/api/Auth"

    override suspend fun login(idNumber: String, faceEmbedding: DoubleArray): String {
        val payload = json.encodeToString(LoginRequest(idNumber, faceEmbedding.toList()))
        val result = execute(
            Request.Builder()
                .url("$authUrl/login")
                .post(payload.toRequestBody(JSON_MEDIA))
                .build()
        )
        val message = result.body.message
        return when {
            result.code in 200..299 -> result.body.token?.takeIf { it.isNotBlank() }
                ?: throw AuthException.Server(message)
            result.code >= 500 -> throw AuthException.Server(message)
            message?.contains("blocked", ignoreCase = true) == true -> throw AuthException.AccountBlocked(message)
            else -> throw AuthException.InvalidCredentials(message)
        }
    }

    override suspend fun register(form: RegistrationForm) {
        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("id_number", form.idNumber)
            .addFormDataPart("firstname", form.firstName)
            .addFormDataPart("lastname", form.lastName)
            .addFormDataPart("province", form.province)
            .addFormDataPart("city", form.city)
            .addFormDataPart("street", form.street)
            .addFormDataPart("postal", form.postalCode.toString())
            .apply { form.faceEmbedding.forEach { addFormDataPart("face_embedding", it.toString()) } }
            .addFormDataPart("id_front", "id_front.jpg", form.idFront.asRequestBody(JPEG_MEDIA))
            .addFormDataPart("id_back", "id_back.jpg", form.idBack.asRequestBody(JPEG_MEDIA))
            .build()

        val result = execute(Request.Builder().url("$authUrl/register").post(body).build())
        val message = result.body.message
        when {
            result.code in 200..299 -> Unit
            result.code == 409 -> throw AuthException.AlreadyRegistered(message)
            result.code in 400..499 -> throw AuthException.InvalidDetails(message)
            else -> throw AuthException.Server(message)
        }
    }

    private suspend fun execute(request: Request): HttpResult = withContext(Dispatchers.IO) {
        try {
            client.newCall(request).execute().use { response ->
                val text = response.body.string()
                val parsed = runCatching { json.decodeFromString<AuthResponse>(text) }
                    .getOrDefault(AuthResponse())
                HttpResult(response.code, parsed)
            }
        } catch (e: IOException) {
            throw AuthException.Network(e)
        }
    }
}
