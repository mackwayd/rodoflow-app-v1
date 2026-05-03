package com.example.rodoflow.data.model

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("token") val token: String? = null,
    @SerializedName("accessToken") val accessToken: String? = null,
    @SerializedName("access_token") val accessTokenSnake: String? = null,
) {
    fun requireBearerToken(): String =
        listOf(token, accessToken, accessTokenSnake).firstOrNull { !it.isNullOrBlank() }
            ?: error("Resposta de login sem token")
}
