package com.example.frontend.data

import android.content.Context
import com.example.frontend.BuildConfig
import com.example.frontend.data.auth.AuthRepository
import com.example.frontend.data.auth.HttpAuthRepository
import com.example.frontend.data.auth.PrefsTokenStore
import com.example.frontend.data.auth.TokenStore
import com.example.frontend.data.face.FaceEmbedder
import com.example.frontend.data.face.TfliteFaceEmbedder

object AppContainer {

    private lateinit var appContext: Context

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    val authRepository: AuthRepository by lazy { HttpAuthRepository(BuildConfig.API_BASE_URL) }

    val tokenStore: TokenStore by lazy { PrefsTokenStore(appContext) }

    val faceEmbedder: FaceEmbedder by lazy { TfliteFaceEmbedder(appContext) }
}
