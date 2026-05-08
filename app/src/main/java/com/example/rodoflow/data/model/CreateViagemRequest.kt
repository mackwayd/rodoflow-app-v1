package com.example.rodoflow.data.model

data class CreateViagemRequest(
    val origem: String,
    val destino: String,
    val numeroToneladas: Double,
    val valorTonelada: Double,
    val cliente: String,
    val cnpjCliente: String,
    val tipoCarga: String,
    val kmInicial: Double,
)
