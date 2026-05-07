package com.example.rodoflow.data.model

data class CreateViagemRequest(
    val motoristaId: String,
    val caminhaoId: String,
    val origem: String,
    val destino: String,
    val valorBruto: Double,
    val dataInicio: String,
)
