package com.example.rodoflow.data.model

data class ResumoViagem(
    val viagemId: String = "",
    val motoristaId: String = "",
    val motoristaNome: String = "",
    val caminhaoPlaca: String = "",
    val origem: String = "",
    val destino: String = "",
    val cliente: String = "",
    val tipoCarga: String = "",
    val dataInicio: String = "",
    val dataFim: String? = null,
    val numeroToneladas: Double = 0.0,
    val toneladasFinais: Double? = null,
    val kgPerdido: Double? = null,
    val valorBruto: Double = 0.0,
    val valorBrutoContratado: Double = 0.0,
    val valorBrutoEfetivo: Double = 0.0,
    val valorMotorista: Double = 0.0,
    val valorQuebra: Double = 0.0,
    val totalDespesas: Double = 0.0,
    val totalAbastecimentos: Double = 0.0,
    val saldoEmpresa: Double = 0.0,
    val valorLiquido: Double = 0.0,
    val status: String = "",
    val statusPagamento: String = "",
) {
    val valorBrutoContratadoOrFallback: Double
        get() = if (valorBrutoContratado > 0.0) valorBrutoContratado else valorBruto

    val valorBrutoEfetivoOrFallback: Double
        get() = if (valorBrutoEfetivo > 0.0) valorBrutoEfetivo else valorBruto

    val valorMotoristaOrFallback: Double
        get() = if (valorMotorista > 0.0) valorMotorista else valorBrutoEfetivoOrFallback * 0.12

    val saldoEmpresaOrFallback: Double
        get() = if (saldoEmpresa != 0.0) saldoEmpresa else valorLiquido

    val temQuebra: Boolean
        get() = valorQuebra > 0.0 || kgPerdido != null || toneladasFinais != null
}
