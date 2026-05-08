package com.example.rodoflow.data.model

data class FinalizarViagemRequest(
    val teveQuebra: Boolean,
    val toneladasFinais: Double? = null,
    val observacaoQuebra: String? = null,
)
