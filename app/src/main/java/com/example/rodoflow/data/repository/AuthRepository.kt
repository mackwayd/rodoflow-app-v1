package com.example.rodoflow.data.repository

import android.util.Log
import com.example.rodoflow.AppContext
import com.example.rodoflow.data.api.ApiService
import com.example.rodoflow.data.api.RetrofitInstance
import com.example.rodoflow.data.auth.AuthTokenStore
import com.example.rodoflow.data.model.LoginRequest
import com.example.rodoflow.data.model.UsuarioMe

class AuthRepository(
    private val api: ApiService = RetrofitInstance.api,
) {

    private val appContext get() = AppContext.applicationContext

    suspend fun login(email: String, password: String): Result<Pair<UsuarioMe, String>> {
        val emailFormatted = email.trim()
        val passwordFormatted = password.trim()
        return runCatching {
            AuthTokenStore.clear(appContext)
            val loginResponse = api.login(LoginRequest(emailFormatted, passwordFormatted))
            val bearer = loginResponse.requireBearerToken()
            AuthTokenStore.save(appContext, bearer)
            val me = api.getUsuarioMe()
            val userId = me.id.orEmpty()
            me to userId
        }.also { result ->
            result.exceptionOrNull()?.let { e ->
                Log.e(TAG, e.message ?: "Erro no login", e)
            }
        }
    }

    suspend fun loginTesteHardcoded(): Result<Pair<UsuarioMe, String>> =
        login(TEST_EMAIL, TEST_PASSWORD)

    suspend fun tryRestoreSession(): Result<Pair<UsuarioMe, String>> {
        if (AuthTokenStore.accessToken.isNullOrBlank()) {
            return Result.failure(Exception("no session"))
        }
        return runCatching {
            val me = api.getUsuarioMe()
            val userId = me.id.orEmpty()
            me to userId
        }
    }

    private companion object {
        private const val TAG = "AuthRepository"
        private const val TEST_EMAIL = "teste@rodoflow.com"
        private const val TEST_PASSWORD = "123456"
    }
}
