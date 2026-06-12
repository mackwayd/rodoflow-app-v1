package com.example.rodoflow.data.model

data class LoginRequest(
    val email: String,
    val senha: String,
)

data class LoginResponse(
    val token: String,
    val usuario: AuthUserDto,
)

data class AuthUserDto(
    val id: String,
    val nome: String,
    val email: String,
    val role: String,
)
