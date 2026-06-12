package com.example.rodoflow.data.local

import com.example.rodoflow.data.local.entity.PendingOperationEntity
import com.example.rodoflow.data.model.Viagem
import com.example.rodoflow.data.sync.CreateDespesaPayload
import com.example.rodoflow.data.sync.CreateViagemPayload
import com.example.rodoflow.data.sync.PendingOperationType
import com.example.rodoflow.data.sync.PendingPayloadCodec
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OptimisticCacheUpdaterTest {

    @Test
    fun applyPendingToList_addsQueuedViagem() {
        val op = PendingOperationEntity(
            id = "op-1",
            type = PendingOperationType.CREATE_VIAGEM.wire,
            payloadJson = PendingPayloadCodec.encode(
                CreateViagemPayload(
                    origem = "A",
                    destino = "B",
                    numeroToneladas = 30.0,
                    valorTonelada = 100.0,
                    cliente = "Cliente",
                    cnpjCliente = "123",
                    tipoCarga = "SOJA",
                    kmInicial = 1000.0,
                ),
            ),
            createdAt = 1_700_000_000_000L,
        )

        val result = OptimisticCacheUpdater.applyPendingToList(emptyList(), listOf(op))

        assertEquals(1, result.size)
        assertEquals("pending-op-1", result.first().id)
        assertEquals("EM_ANDAMENTO", result.first().status)
        assertEquals("A", result.first().origem)
    }

    @Test
    fun applyPendingToDetail_addsQueuedDespesa() {
        val viagem = Viagem(
            id = "v1",
            origem = "A",
            destino = "B",
            status = "EM_ANDAMENTO",
        )
        val op = PendingOperationEntity(
            id = "d1",
            type = PendingOperationType.CREATE_DESPESA.wire,
            payloadJson = PendingPayloadCodec.encode(
                CreateDespesaPayload(
                    caminhaoId = "cam-1",
                    valor = 50.0,
                    tipo = "PEDAGIO",
                    data = "2026-05-19T10:00:00Z",
                    descricao = "Teste",
                    viagemId = "v1",
                ),
            ),
        )

        val result = OptimisticCacheUpdater.applyPendingToDetail(viagem, listOf(op))

        assertEquals(1, result.despesas.size)
        assertEquals(50.0, result.totalDespesas, 0.001)
        assertTrue(result.despesas.first().id.startsWith(OptimisticCacheUpdater.PENDING_ID_PREFIX))
    }
}
