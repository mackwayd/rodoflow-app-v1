package com.example.rodoflow.data.model

data class ResumoViagem(
    val viagemId: String,
    val motoristaId: String,
    val motoristaNome: String,
    val caminhaoPlaca: String,
    val valorBruto: Double,
    val totalDespesas: Double,
    val totalAbastecimentos: Double,
    val valorLiquido: Double,
    val status: String,
    val statusPagamento: String,
)
