package com.example.rodoflow.data.model

data class Viagem(
    val id: String,
    val origem: String,
    val destino: String,
    val valorBruto: Double,
    val status: String,
    val statusPagamento: String? = null,
)
