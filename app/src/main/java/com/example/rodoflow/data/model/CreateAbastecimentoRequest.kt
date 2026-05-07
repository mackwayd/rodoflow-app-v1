package com.example.rodoflow.data.model

data class CreateAbastecimentoRequest(
    val caminhaoId: String,
    val litros: Double,
    val valorTotal: Double,
    val data: String,
)
