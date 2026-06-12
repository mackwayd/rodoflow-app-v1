package com.example.rodoflow.data.repository

import com.example.rodoflow.data.api.ApiService
import com.example.rodoflow.data.api.RetrofitInstance
import com.example.rodoflow.data.auth.AuthSession
import com.example.rodoflow.data.model.LoginRequest
import retrofit2.HttpException
import java.io.IOException

sealed class AuthLoginResult {
    data class Success(val session: AuthSession) : AuthLoginResult()
    data class InvalidCredentials(val message: String) : AuthLoginResult()
    data class WrongRole(val message: String) : AuthLoginResult()
    data class NetworkError(val message: String) : AuthLoginResult()
    data class UnknownError(val message: String) : AuthLoginResult()
}

class AuthRepository(
    private val apiService: ApiService = RetrofitInstance.api,
) {
    suspend fun login(email: String, senha: String): AuthLoginResult {
        return try {
            val response = apiService.login(
                LoginRequest(
                    email = email.trim(),
                    senha = senha,
                ),
            )
            if (response.usuario.role != "MOTORISTA") {
                return AuthLoginResult.WrongRole(
                    "Este aplicativo é apenas para motoristas. Use o painel web para administradores.",
                )
            }
            AuthLoginResult.Success(
                AuthSession(
                    token = response.token,
                    userId = response.usuario.id,
                    nome = response.usuario.nome,
                    email = response.usuario.email,
                    role = response.usuario.role,
                ),
            )
        } catch (e: HttpException) {
            when (e.code()) {
                401 -> AuthLoginResult.InvalidCredentials("E-mail ou senha incorretos.")
                else -> AuthLoginResult.UnknownError("Não foi possível entrar. Tente novamente.")
            }
        } catch (_: IOException) {
            AuthLoginResult.NetworkError("Sem conexão com o servidor. Verifique a internet.")
        } catch (_: Exception) {
            AuthLoginResult.UnknownError("Não foi possível entrar. Tente novamente.")
        }
    }
}
