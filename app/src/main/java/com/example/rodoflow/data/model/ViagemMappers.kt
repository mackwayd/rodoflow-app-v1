package com.example.rodoflow.data.model

import com.example.rodoflow.DriverContext

fun Viagem.toResumoViagem(): ResumoViagem = ResumoViagem(
    viagemId = id,
    motoristaId = DriverContext.motoristaId,
    motoristaNome = "Motorista",
    origem = origem,
    destino = destino,
    cliente = cliente,
    tipoCarga = tipoCarga,
    dataInicio = dataInicio,
    dataFim = dataFim,
    numeroToneladas = numeroToneladas,
    kgPerdido = kgPerdido,
    valorBruto = valorBruto,
    valorBrutoEfetivo = valorBrutoEfetivo ?: valorBruto,
    valorMotorista = valorMotorista,
    valorQuebra = valorQuebra ?: 0.0,
    totalDespesas = totalDespesas,
    totalAbastecimentos = totalAbastecimentos,
    saldoEmpresa = saldoEmpresa,
    status = status,
    statusPagamento = statusPagamento ?: status,
)

fun List<Viagem>.toResumoViagens(): List<ResumoViagem> = map { it.toResumoViagem() }
