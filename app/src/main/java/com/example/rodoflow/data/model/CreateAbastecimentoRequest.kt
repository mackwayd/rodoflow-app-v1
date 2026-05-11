package com.example.rodoflow.data.model

data class CreateAbastecimentoRequest(
    val caminhaoId: String,
    val litros: Double,
    val valorLitro: Double,
    val data: String,
    val viagemId: String? = null,
)
