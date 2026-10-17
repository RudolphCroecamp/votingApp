package com.example.frontend.data.auth

import android.content.Context
import androidx.core.content.edit

interface TokenStore {
    val token: String?

    fun save(token: String)

    fun clear()
}

class PrefsTokenStore(context: Context) : TokenStore {

    private val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)

    override val token: String?
        get() = prefs.getString(KEY_TOKEN, null)

    override fun save(token: String) = prefs.edit { putString(KEY_TOKEN, token) }

    override fun clear() = prefs.edit { remove(KEY_TOKEN) }

    private companion object {
        const val KEY_TOKEN = "token"
    }
}
