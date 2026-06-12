package com.example.rodoflow.data.sync

data class CreateViagemPayload(
    val origem: String,
    val destino: String,
    val numeroToneladas: Double,
    val valorTonelada: Double,
    val cliente: String,
    val cnpjCliente: String,
    val tipoCarga: String,
    val kmInicial: Double,
)

data class CreateDespesaPayload(
    val caminhaoId: String,
    val valor: Double,
    val tipo: String,
    val data: String,
    val descricao: String?,
    val viagemId: String?,
)

data class CreateAbastecimentoPayload(
    val caminhaoId: String,
    val litros: Double,
    val valorLitro: Double,
    val data: String,
    val viagemId: String?,
)

data class FinalizarViagemPayload(
    val viagemId: String,
    val teveQuebra: Boolean,
    val kgPerdido: Double?,
    val valorQuebra: Double?,
    val observacaoQuebra: String?,
)
