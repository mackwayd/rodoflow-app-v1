package com.example.rodoflow.data.sync

import com.example.rodoflow.data.local.entity.PendingOperationEntity
import com.example.rodoflow.data.repository.ViagemRepository
import com.example.rodoflow.ui.util.isRetriableNetworkThrowable
import com.example.rodoflow.ui.util.userMessageForThrowable

class SyncEngine(
    private val viagemRepository: ViagemRepository,
    private val comprovanteStorage: ComprovanteStorage,
) {

    suspend fun execute(operation: PendingOperationEntity) {
        try {
            when (PendingOperationType.fromWire(operation.type)) {
                PendingOperationType.CREATE_VIAGEM -> {
                    val p = PendingPayloadCodec.decodeCreateViagem(operation.payloadJson)
                    viagemRepository.createViagem(
                        origem = p.origem,
                        destino = p.destino,
                        numeroToneladas = p.numeroToneladas,
                        valorTonelada = p.valorTonelada,
                        cliente = p.cliente,
                        cnpjCliente = p.cnpjCliente,
                        tipoCarga = p.tipoCarga,
                        kmInicial = p.kmInicial,
                    )
                }
                PendingOperationType.CREATE_DESPESA -> {
                    val p = PendingPayloadCodec.decodeCreateDespesa(operation.payloadJson)
                    val comprovante = operation.comprovantePath?.let { comprovanteStorage.load(it) }
                    viagemRepository.createDespesa(
                        caminhaoId = p.caminhaoId,
                        valor = p.valor,
                        tipo = p.tipo,
                        data = p.data,
                        descricao = p.descricao,
                        viagemId = p.viagemId,
                        comprovante = comprovante,
                    )
                }
                PendingOperationType.CREATE_ABASTECIMENTO -> {
                    val p = PendingPayloadCodec.decodeCreateAbastecimento(operation.payloadJson)
                    val comprovante = operation.comprovantePath?.let { comprovanteStorage.load(it) }
                    viagemRepository.createAbastecimento(
                        caminhaoId = p.caminhaoId,
                        litros = p.litros,
                        valorLitro = p.valorLitro,
                        data = p.data,
                        viagemId = p.viagemId,
                        comprovante = comprovante,
                    )
                }
                PendingOperationType.FINALIZAR_VIAGEM -> {
                    val p = PendingPayloadCodec.decodeFinalizarViagem(operation.payloadJson)
                    viagemRepository.finalizarViagem(
                        id = p.viagemId,
                        teveQuebra = p.teveQuebra,
                        kgPerdido = p.kgPerdido,
                        valorQuebra = p.valorQuebra,
                        observacaoQuebra = p.observacaoQuebra,
                    )
                }
            }
        } catch (e: Exception) {
            if (isRetriableNetworkThrowable(e)) throw e
            throw NonRetriableSyncException(userMessageForThrowable(e), e)
        }
    }
}

class NonRetriableSyncException(
    val userMessage: String,
    cause: Throwable,
) : Exception(userMessage, cause)
