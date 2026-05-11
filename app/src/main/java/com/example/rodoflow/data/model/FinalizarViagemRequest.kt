package com.example.rodoflow.data.model

data class FinalizarViagemRequest(
    val teveQuebra: Boolean,
    val kgPerdido: Double? = null,
    val valorQuebra: Double? = null,
    val observacaoQuebra: String? = null,
)
