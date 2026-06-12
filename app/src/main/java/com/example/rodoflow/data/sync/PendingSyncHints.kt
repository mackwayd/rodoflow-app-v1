package com.example.rodoflow.data.sync

import com.example.rodoflow.data.local.PendingOperationStore
import com.example.rodoflow.data.local.entity.PendingOperationEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object PendingSyncHints {
    fun observePendingFinalizeForViagem(
        store: PendingOperationStore,
        viagemId: String,
    ): Flow<Boolean> =
        store.observeAll().map { ops ->
            ops.any { op ->
                op.status == PendingOperationEntity.STATUS_PENDING &&
                    op.type == PendingOperationType.FINALIZAR_VIAGEM.wire &&
                    runCatching {
                        PendingPayloadCodec.decodeFinalizarViagem(op.payloadJson).viagemId == viagemId
                    }.getOrDefault(false)
            }
        }

    suspend fun hasPendingFinalize(store: PendingOperationStore, viagemId: String): Boolean {
        val pending = store.getByStatus(PendingOperationEntity.STATUS_PENDING)
        return pending.any { op ->
            op.type == PendingOperationType.FINALIZAR_VIAGEM.wire &&
                runCatching {
                    PendingPayloadCodec.decodeFinalizarViagem(op.payloadJson).viagemId == viagemId
                }.getOrDefault(false)
        }
    }
}
