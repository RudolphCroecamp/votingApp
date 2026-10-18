package com.example.frontend.ui.screens.Auth

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.annotation.StringRes
import com.example.frontend.R
import com.example.frontend.data.auth.AuthException
import com.example.frontend.data.face.FaceEmbeddingException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@StringRes
fun Throwable.toMessageRes(): Int = when (this) {
    is FaceEmbeddingException.NoFace -> R.string.camera_problem_no_face
    is FaceEmbeddingException.ModelMissing -> R.string.camera_problem_check_failed
    is AuthException.Network -> R.string.error_network
    is AuthException.AccountBlocked -> R.string.login_error_blocked
    is AuthException.AlreadyRegistered -> R.string.register_error_exists
    is AuthException.InvalidDetails -> R.string.register_error_invalid
    else -> R.string.error_server
}

suspend fun decodePhoto(path: String): Bitmap = withContext(Dispatchers.IO) {
    BitmapFactory.decodeFile(path) ?: throw FaceEmbeddingException.NoFace()
}
