package com.example.rodoflow.data.auth

data class AuthSession(
    val token: String,
    val userId: String,
    val nome: String,
    val email: String,
    val role: String,
)
