package com.example.rodoflow.ui.sync

import com.example.rodoflow.data.local.entity.PendingOperationEntity
import com.example.rodoflow.data.sync.CreateAbastecimentoPayload
import com.example.rodoflow.data.sync.CreateDespesaPayload
import com.example.rodoflow.data.sync.CreateViagemPayload
import com.example.rodoflow.data.sync.FinalizarViagemPayload
import com.example.rodoflow.data.sync.PendingOperationType
import com.example.rodoflow.data.sync.PendingPayloadCodec
import com.example.rodoflow.ui.util.formatRouteSegment
import com.example.rodoflow.ui.util.humanizeTipoDespesa
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun humanizePendingOperationType(type: String): String =
    when (PendingOperationType.fromWire(type)) {
        PendingOperationType.CREATE_VIAGEM -> "Nova viagem"
        PendingOperationType.CREATE_DESPESA -> "Despesa"
        PendingOperationType.CREATE_ABASTECIMENTO -> "Abastecimento"
        PendingOperationType.FINALIZAR_VIAGEM -> "Finalizar viagem"
    }

fun pendingOperationSubtitle(operation: PendingOperationEntity): String {
    return when (PendingOperationType.fromWire(operation.type)) {
        PendingOperationType.CREATE_VIAGEM -> {
            val p = PendingPayloadCodec.decodeCreateViagem(operation.payloadJson)
            "${formatRouteSegment(p.origem)} → ${formatRouteSegment(p.destino)}"
        }
        PendingOperationType.CREATE_DESPESA -> {
            val p = PendingPayloadCodec.decodeCreateDespesa(operation.payloadJson)
            buildString {
                append(humanizeTipoDespesa(p.tipo))
                p.viagemId?.let { append(" · viagem ${it.take(8)}…") }
            }
        }
        PendingOperationType.CREATE_ABASTECIMENTO -> {
            val p = PendingPayloadCodec.decodeCreateAbastecimento(operation.payloadJson)
            buildString {
                append("${p.litros} L")
                p.viagemId?.let { append(" · viagem ${it.take(8)}…") }
            }
        }
        PendingOperationType.FINALIZAR_VIAGEM -> {
            val p = PendingPayloadCodec.decodeFinalizarViagem(operation.payloadJson)
            "Viagem ${p.viagemId.take(8)}…" + if (p.teveQuebra) " · com quebra" else ""
        }
    }
}

fun formatPendingCreatedAt(epochMs: Long): String {
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
    return Instant.ofEpochMilli(epochMs)
        .atZone(ZoneId.systemDefault())
        .format(formatter)
}
