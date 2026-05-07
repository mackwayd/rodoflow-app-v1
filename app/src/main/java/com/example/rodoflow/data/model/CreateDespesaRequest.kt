package com.example.rodoflow.data.model

data class CreateDespesaRequest(
    val caminhaoId: String,
    val valor: Double,
    val tipo: String,
    val data: String,
    val descricao: String? = null,
)
