package com.example.rodoflow.data.local

import com.example.rodoflow.DriverContext
import com.example.rodoflow.data.local.entity.PendingOperationEntity
import com.example.rodoflow.data.model.AbastecimentoViagem
import com.example.rodoflow.data.model.DespesaViagem
import com.example.rodoflow.data.model.Viagem
import com.example.rodoflow.data.sync.CreateAbastecimentoPayload
import com.example.rodoflow.data.sync.CreateDespesaPayload
import com.example.rodoflow.data.sync.CreateViagemPayload
import com.example.rodoflow.data.sync.FinalizarViagemPayload
import com.example.rodoflow.data.sync.PendingOperationType
import com.example.rodoflow.data.sync.PendingPayloadCodec
import java.time.Instant

/**
 * Mescla operações pendentes de sincronização sobre dados em cache,
 * para que o motorista veja lançamentos offline imediatamente.
 */
object OptimisticCacheUpdater {

    const val PENDING_ID_PREFIX = "pending-"
    const val LOCAL_COMPROVANTE_PREFIX = "local:"

    fun pendingViagemId(operationId: String): String = "$PENDING_ID_PREFIX$operationId"

    fun isPendingId(id: String): Boolean = id.startsWith(PENDING_ID_PREFIX)

    fun applyPendingToList(
        viagens: List<Viagem>,
        pendingOps: List<PendingOperationEntity>,
    ): List<Viagem> {
        var result = viagens.toMutableList()
        pendingOps
            .filter { it.status == PendingOperationEntity.STATUS_PENDING }
            .sortedBy { it.createdAt }
            .forEach { op ->
                result = applyToList(result, op).toMutableList()
            }
        return result
    }

    fun applyPendingToDetail(
        viagem: Viagem,
        pendingOps: List<PendingOperationEntity>,
    ): Viagem {
        var result = viagem
        pendingOps
            .filter { it.status == PendingOperationEntity.STATUS_PENDING }
            .sortedBy { it.createdAt }
            .forEach { op ->
                result = applyToDetail(result, op)
            }
        return result
    }

    private fun applyToList(viagens: MutableList<Viagem>, op: PendingOperationEntity): List<Viagem> {
        when (PendingOperationType.fromWire(op.type)) {
            PendingOperationType.CREATE_VIAGEM -> {
                val id = pendingViagemId(op.id)
                if (viagens.any { it.id == id }) return viagens
                val p = PendingPayloadCodec.decodeCreateViagem(op.payloadJson)
                viagens.add(0, viagemFromCreatePayload(p, id, op.createdAt))
            }
            PendingOperationType.CREATE_DESPESA -> {
                val p = PendingPayloadCodec.decodeCreateDespesa(op.payloadJson)
                val viagemId = p.viagemId ?: return viagens
                val index = viagens.indexOfFirst { it.id == viagemId }
                if (index >= 0) {
                    viagens[index] = applyToDetail(viagens[index], op)
                }
            }
            PendingOperationType.CREATE_ABASTECIMENTO -> {
                val p = PendingPayloadCodec.decodeCreateAbastecimento(op.payloadJson)
                val viagemId = p.viagemId ?: return viagens
                val index = viagens.indexOfFirst { it.id == viagemId }
                if (index >= 0) {
                    viagens[index] = applyToDetail(viagens[index], op)
                }
            }
            PendingOperationType.FINALIZAR_VIAGEM -> {
                val p = PendingPayloadCodec.decodeFinalizarViagem(op.payloadJson)
                val index = viagens.indexOfFirst { it.id == p.viagemId }
                if (index >= 0) {
                    viagens[index] = applyToDetail(viagens[index], op)
                }
            }
        }
        return viagens
    }

    private fun applyToDetail(viagem: Viagem, op: PendingOperationEntity): Viagem {
        return when (PendingOperationType.fromWire(op.type)) {
            PendingOperationType.CREATE_VIAGEM -> {
                val id = pendingViagemId(op.id)
                if (viagem.id == id) {
                    viagemFromCreatePayload(
                        PendingPayloadCodec.decodeCreateViagem(op.payloadJson),
                        id,
                        op.createdAt,
                    )
                } else {
                    viagem
                }
            }
            PendingOperationType.CREATE_DESPESA -> {
                val p = PendingPayloadCodec.decodeCreateDespesa(op.payloadJson)
                if (p.viagemId != viagem.id) return viagem
                val despesaId = pendingViagemId(op.id)
                if (viagem.despesas.any { it.id == despesaId }) return viagem
                val nova = DespesaViagem(
                    id = despesaId,
                    tipo = p.tipo,
                    valor = p.valor,
                    descricao = p.descricao,
                    data = p.data,
                    comprovanteUrl = op.comprovantePath?.let { localComprovanteUrl(it) },
                )
                val despesas = viagem.despesas + nova
                viagem.copy(
                    despesas = despesas,
                    totalDespesas = despesas.sumOf { it.valor },
                )
            }
            PendingOperationType.CREATE_ABASTECIMENTO -> {
                val p = PendingPayloadCodec.decodeCreateAbastecimento(op.payloadJson)
                if (p.viagemId != viagem.id) return viagem
                val abastId = pendingViagemId(op.id)
                if (viagem.abastecimentos.any { it.id == abastId }) return viagem
                val valorTotal = p.litros * p.valorLitro
                val novo = AbastecimentoViagem(
                    id = abastId,
                    litros = p.litros,
                    valorLitro = p.valorLitro,
                    valorTotal = valorTotal,
                    data = p.data,
                    comprovanteUrl = op.comprovantePath?.let { localComprovanteUrl(it) },
                )
                val abastecimentos = viagem.abastecimentos + novo
                viagem.copy(
                    abastecimentos = abastecimentos,
                    totalAbastecimentos = abastecimentos.sumOf { it.valorTotal },
                )
            }
            PendingOperationType.FINALIZAR_VIAGEM -> {
                val p = PendingPayloadCodec.decodeFinalizarViagem(op.payloadJson)
                if (p.viagemId != viagem.id) return viagem
                viagem.copy(
                    status = "FINALIZADA",
                    kgPerdido = p.kgPerdido,
                    valorQuebra = p.valorQuebra,
                    dataFim = Instant.ofEpochMilli(op.createdAt).toString(),
                )
            }
        }
    }

    private fun viagemFromCreatePayload(
        p: CreateViagemPayload,
        id: String,
        createdAt: Long,
    ): Viagem {
        val valorBruto = p.numeroToneladas * p.valorTonelada
        return Viagem(
            id = id,
            origem = p.origem,
            destino = p.destino,
            cliente = p.cliente,
            cnpjCliente = p.cnpjCliente,
            tipoCarga = p.tipoCarga,
            numeroToneladas = p.numeroToneladas,
            valorTonelada = p.valorTonelada,
            kmInicial = p.kmInicial,
            dataInicio = Instant.ofEpochMilli(createdAt).toString(),
            valorBruto = valorBruto,
            status = "EM_ANDAMENTO",
        )
    }

    fun localComprovanteUrl(absolutePath: String): String =
        "$LOCAL_COMPROVANTE_PREFIX$absolutePath"
}
