package com.example.rodoflow.data.auth

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthSessionStore(context: Context) {

    private val prefs = EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    private val _session = MutableStateFlow(readSession())
    val session: StateFlow<AuthSession?> = _session.asStateFlow()

    fun currentSession(): AuthSession? = _session.value

    fun currentToken(): String? = _session.value?.token

    fun motoristaIdOrDefault(): String = _session.value?.userId ?: DEFAULT_MOTORISTA_ID

    fun save(session: AuthSession) {
        prefs.edit()
            .putString(KEY_TOKEN, session.token)
            .putString(KEY_USER_ID, session.userId)
            .putString(KEY_NOME, session.nome)
            .putString(KEY_EMAIL, session.email)
            .putString(KEY_ROLE, session.role)
            .apply()
        _session.value = session
    }

    fun clear() {
        prefs.edit().clear().apply()
        _session.value = null
    }

    private fun readSession(): AuthSession? {
        val token = prefs.getString(KEY_TOKEN, null)?.trim().orEmpty()
        val userId = prefs.getString(KEY_USER_ID, null)?.trim().orEmpty()
        val nome = prefs.getString(KEY_NOME, null)?.trim().orEmpty()
        val email = prefs.getString(KEY_EMAIL, null)?.trim().orEmpty()
        val role = prefs.getString(KEY_ROLE, null)?.trim().orEmpty()
        if (token.isEmpty() || userId.isEmpty()) return null
        return AuthSession(
            token = token,
            userId = userId,
            nome = nome,
            email = email,
            role = role,
        )
    }

    companion object {
        private const val PREFS_NAME = "rodoflow_auth_session"
        private const val KEY_TOKEN = "token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_NOME = "nome"
        private const val KEY_EMAIL = "email"
        private const val KEY_ROLE = "role"
        const val DEFAULT_MOTORISTA_ID = "motorista-1"
    }
}
