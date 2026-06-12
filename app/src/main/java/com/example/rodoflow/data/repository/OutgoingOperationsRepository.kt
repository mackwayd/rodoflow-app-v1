package com.example.rodoflow.data.repository

import android.content.Context
import com.example.rodoflow.data.local.PendingOperationStore
import com.example.rodoflow.data.local.entity.PendingOperationEntity
import com.example.rodoflow.data.sync.ComprovanteStorage
import com.example.rodoflow.data.sync.CreateAbastecimentoPayload
import com.example.rodoflow.data.sync.CreateDespesaPayload
import com.example.rodoflow.data.sync.CreateViagemPayload
import com.example.rodoflow.data.sync.FinalizarViagemPayload
import com.example.rodoflow.data.sync.PendingOperationType
import com.example.rodoflow.data.sync.PendingPayloadCodec
import com.example.rodoflow.data.sync.SyncScheduler
import com.example.rodoflow.data.util.ComprovantePayload
import com.example.rodoflow.ui.util.isRetriableNetworkThrowable
import java.util.UUID

class OutgoingOperationsRepository(
    private val context: Context,
    private val viagemRepository: ViagemRepository,
    private val pendingStore: PendingOperationStore,
    private val comprovanteStorage: ComprovanteStorage,
) {

    suspend fun createViagem(
        origem: String,
        destino: String,
        numeroToneladas: Double,
        valorTonelada: Double,
        cliente: String,
        cnpjCliente: String,
        tipoCarga: String,
        kmInicial: Double,
    ): OperationResult = runWrite(
        block = {
            viagemRepository.createViagem(
                origem = origem,
                destino = destino,
                numeroToneladas = numeroToneladas,
                valorTonelada = valorTonelada,
                cliente = cliente,
                cnpjCliente = cnpjCliente,
                tipoCarga = tipoCarga,
                kmInicial = kmInicial,
            )
        },
        onQueue = {
            enqueue(
                type = PendingOperationType.CREATE_VIAGEM,
                payload = CreateViagemPayload(
                    origem = origem,
                    destino = destino,
                    numeroToneladas = numeroToneladas,
                    valorTonelada = valorTonelada,
                    cliente = cliente,
                    cnpjCliente = cnpjCliente,
                    tipoCarga = tipoCarga,
                    kmInicial = kmInicial,
                ),
                comprovante = null,
            )
        },
    )

    suspend fun createDespesa(
        caminhaoId: String,
        valor: Double,
        tipo: String,
        data: String,
        descricao: String?,
        viagemId: String? = null,
        comprovante: ComprovantePayload? = null,
    ): OperationResult = runWrite(
        block = {
            viagemRepository.createDespesa(
                caminhaoId = caminhaoId,
                valor = valor,
                tipo = tipo,
                data = data,
                descricao = descricao,
                viagemId = viagemId,
                comprovante = comprovante,
            )
        },
        onQueue = {
            enqueue(
                type = PendingOperationType.CREATE_DESPESA,
                payload = CreateDespesaPayload(
                    caminhaoId = caminhaoId,
                    valor = valor,
                    tipo = tipo,
                    data = data,
                    descricao = descricao,
                    viagemId = viagemId,
                ),
                comprovante = comprovante,
            )
        },
    )

    suspend fun createAbastecimento(
        caminhaoId: String,
        litros: Double,
        valorLitro: Double,
        data: String,
        viagemId: String? = null,
        comprovante: ComprovantePayload? = null,
    ): OperationResult = runWrite(
        block = {
            viagemRepository.createAbastecimento(
                caminhaoId = caminhaoId,
                litros = litros,
                valorLitro = valorLitro,
                data = data,
                viagemId = viagemId,
                comprovante = comprovante,
            )
        },
        onQueue = {
            enqueue(
                type = PendingOperationType.CREATE_ABASTECIMENTO,
                payload = CreateAbastecimentoPayload(
                    caminhaoId = caminhaoId,
                    litros = litros,
                    valorLitro = valorLitro,
                    data = data,
                    viagemId = viagemId,
                ),
                comprovante = comprovante,
            )
        },
    )

    suspend fun finalizarViagem(
        id: String,
        teveQuebra: Boolean,
        kgPerdido: Double? = null,
        valorQuebra: Double? = null,
        observacaoQuebra: String? = null,
    ): OperationResult = runWrite(
        block = {
            viagemRepository.finalizarViagem(
                id = id,
                teveQuebra = teveQuebra,
                kgPerdido = kgPerdido,
                valorQuebra = valorQuebra,
                observacaoQuebra = observacaoQuebra,
            )
        },
        onQueue = {
            enqueue(
                type = PendingOperationType.FINALIZAR_VIAGEM,
                payload = FinalizarViagemPayload(
                    viagemId = id,
                    teveQuebra = teveQuebra,
                    kgPerdido = kgPerdido,
                    valorQuebra = valorQuebra,
                    observacaoQuebra = observacaoQuebra,
                ),
                comprovante = null,
            )
        },
    )

    suspend fun retryFailedAndSchedule() {
        pendingStore.resetFailedToPending()
        SyncScheduler.schedule(context)
    }

    suspend fun discardPendingOperation(id: String) {
        val op = pendingStore.findById(id) ?: return
        comprovanteStorage.delete(op.comprovantePath)
        pendingStore.deleteById(id)
    }

    private suspend inline fun runWrite(
        crossinline block: suspend () -> Unit,
        crossinline onQueue: suspend () -> Unit,
    ): OperationResult {
        return try {
            block()
            OperationResult.Sent
        } catch (e: Exception) {
            if (!isRetriableNetworkThrowable(e)) throw e
            onQueue()
            OperationResult.Queued
        }
    }

    private suspend fun enqueue(
        type: PendingOperationType,
        payload: Any,
        comprovante: ComprovantePayload?,
    ) {
        val comprovantePath = comprovante?.let { comprovanteStorage.save(it) }
        pendingStore.insert(
            PendingOperationEntity(
                id = UUID.randomUUID().toString(),
                type = type.wire,
                payloadJson = PendingPayloadCodec.encode(payload),
                comprovantePath = comprovantePath,
            ),
        )
        SyncScheduler.schedule(context)
    }
}
