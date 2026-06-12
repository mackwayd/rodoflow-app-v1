package com.example.rodoflow.data.model

data class Viagem(
    val id: String,
    val origem: String,
    val destino: String,
    val cliente: String = "",
    val cnpjCliente: String = "",
    val tipoCarga: String = "SOJA",
    val numeroToneladas: Double = 0.0,
    val valorTonelada: Double = 0.0,
    val kmInicial: Double = 0.0,
    val dataInicio: String = "",
    val dataFim: String? = null,
    val valorBruto: Double = 0.0,
    val valorMotorista: Double = 0.0,
    val totalDespesas: Double = 0.0,
    val totalAbastecimentos: Double = 0.0,
    val saldoEmpresa: Double = 0.0,
    val valorQuebra: Double? = null,
    val kgPerdido: Double? = null,
    val valorBrutoEfetivo: Double? = null,
    val status: String = "",
    val statusPagamento: String? = null,
    val abastecimentos: List<AbastecimentoViagem> = emptyList(),
    val despesas: List<DespesaViagem> = emptyList(),
)

data class AbastecimentoViagem(
    val id: String = "",
    val litros: Double = 0.0,
    val valorLitro: Double? = null,
    val valorTotal: Double = 0.0,
    val data: String = "",
    val comprovanteUrl: String? = null,
)

data class DespesaViagem(
    val id: String = "",
    val tipo: String = "",
    val valor: Double = 0.0,
    val descricao: String? = null,
    val data: String = "",
    val comprovanteUrl: String? = null,
)
