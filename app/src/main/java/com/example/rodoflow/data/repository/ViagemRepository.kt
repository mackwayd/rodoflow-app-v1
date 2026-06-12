package com.example.rodoflow.data.repository

import com.example.rodoflow.AppLog
import com.example.rodoflow.data.api.ApiService
import com.example.rodoflow.data.api.RetrofitInstance
import com.example.rodoflow.data.api.imagePart
import com.example.rodoflow.data.api.textPart
import com.example.rodoflow.data.local.OptimisticCacheUpdater
import com.example.rodoflow.data.local.PendingOperationStore
import com.example.rodoflow.data.local.ViagemCacheStore
import com.example.rodoflow.data.local.entity.PendingOperationEntity
import com.example.rodoflow.data.model.CreateViagemRequest
import com.example.rodoflow.data.model.FinalizarViagemRequest
import com.example.rodoflow.data.model.Viagem
import com.example.rodoflow.data.util.ComprovantePayload
import com.example.rodoflow.data.util.VisibleWindowFilter
import com.example.rodoflow.ui.util.isRetriableNetworkThrowable
import retrofit2.HttpException

class ViagemRepository(
    private val apiService: ApiService = RetrofitInstance.api,
    private val cache: ViagemCacheStore? = null,
    private val pendingStore: PendingOperationStore? = null,
) {

    suspend fun loadViagens(motoristaId: String): ViagemListLoadResult {
        return try {
            val list = apiService.getViagens(motoristaId)
            cache?.saveViagens(list)
            ViagemListLoadResult.Success(
                viagens = prepareListForDisplay(list),
                fromCache = false,
            )
        } catch (e: Exception) {
            if (isRetriableNetworkThrowable(e)) {
                val cached = cache?.getViagens()
                if (!cached.isNullOrEmpty()) {
                    AppLog.d("VIAGEM_CACHE", "loadViagens: usando cache (${cached.size} itens)")
                    return ViagemListLoadResult.Success(
                        viagens = prepareListForDisplay(cached),
                        fromCache = true,
                    )
                }
            }
            ViagemListLoadResult.Error(e)
        }
    }

    suspend fun getViagens(motoristaId: String): List<Viagem> {
        return when (val result = loadViagens(motoristaId)) {
            is ViagemListLoadResult.Success -> result.viagens
            is ViagemListLoadResult.Error -> throw result.cause
        }
    }

    suspend fun loadViagemById(id: String): ViagemDetailLoadResult {
        if (OptimisticCacheUpdater.isPendingId(id)) {
            return loadPendingViagemDetail(id)
        }

        return try {
            val viagem = apiService.getViagemById(id)
            cache?.saveViagemDetail(viagem)
            val prepared = prepareDetailForDisplay(viagem)
            if (prepared == null) {
                ViagemDetailLoadResult.NotFound
            } else {
                ViagemDetailLoadResult.Success(viagem = prepared, fromCache = false)
            }
        } catch (e: HttpException) {
            if (e.code() == 404) {
                ViagemDetailLoadResult.NotFound
            } else if (isRetriableNetworkThrowable(e)) {
                loadViagemDetailFromCache(id)
            } else {
                ViagemDetailLoadResult.Error(e)
            }
        } catch (e: Exception) {
            if (isRetriableNetworkThrowable(e)) {
                loadViagemDetailFromCache(id)
            } else {
                ViagemDetailLoadResult.Error(e)
            }
        }
    }

    suspend fun getViagemById(id: String): Viagem? {
        return when (val result = loadViagemById(id)) {
            is ViagemDetailLoadResult.Success -> result.viagem
            else -> null
        }
    }

    private suspend fun loadPendingViagemDetail(id: String): ViagemDetailLoadResult {
        val cachedList = cache?.getViagens()
        if (!cachedList.isNullOrEmpty()) {
            val fromList = prepareListForDisplay(cachedList).find { it.id == id }
            if (fromList != null) {
                return ViagemDetailLoadResult.Success(fromList, fromCache = true)
            }
        }
        val pendingOps = pendingOperations()
        for (op in pendingOps) {
            if (OptimisticCacheUpdater.pendingViagemId(op.id) == id) {
                val viagem = OptimisticCacheUpdater.applyPendingToDetail(
                    viagem = emptyViagem(id),
                    pendingOps = listOf(op),
                )
                return ViagemDetailLoadResult.Success(viagem, fromCache = true)
            }
        }
        return ViagemDetailLoadResult.NotFound
    }

    private suspend fun loadViagemDetailFromCache(id: String): ViagemDetailLoadResult {
        val cached = cache?.getViagemDetail(id)
        if (cached != null) {
            AppLog.d("VIAGEM_CACHE", "loadViagemById: usando cache id=$id")
            val prepared = prepareDetailForDisplay(cached)
            if (prepared != null) {
                return ViagemDetailLoadResult.Success(viagem = prepared, fromCache = true)
            }
        }
        val fromList = cache?.getViagens()
            ?.let { prepareListForDisplay(it).find { v -> v.id == id } }
        if (fromList != null) {
            return ViagemDetailLoadResult.Success(viagem = fromList, fromCache = true)
        }
        return ViagemDetailLoadResult.Error(
            IllegalStateException("Sem conexão e viagem não disponível offline"),
        )
    }

    private suspend fun pendingOperations(): List<PendingOperationEntity> {
        return pendingStore?.getByStatus(PendingOperationEntity.STATUS_PENDING).orEmpty()
    }

    private suspend fun prepareListForDisplay(viagens: List<Viagem>): List<Viagem> {
        val withPending = OptimisticCacheUpdater.applyPendingToList(viagens, pendingOperations())
        return VisibleWindowFilter.filterViagens(withPending)
    }

    private suspend fun prepareDetailForDisplay(viagem: Viagem): Viagem? {
        val withPending = OptimisticCacheUpdater.applyPendingToDetail(viagem, pendingOperations())
        return VisibleWindowFilter.applyToViagem(withPending)
    }

    private fun emptyViagem(id: String) = Viagem(
        id = id,
        origem = "",
        destino = "",
        status = "EM_ANDAMENTO",
    )

    suspend fun finalizarViagem(
        id: String,
        teveQuebra: Boolean,
        kgPerdido: Double? = null,
        valorQuebra: Double? = null,
        observacaoQuebra: String? = null,
    ) {
        val body = FinalizarViagemRequest(
            teveQuebra = teveQuebra,
            kgPerdido = kgPerdido,
            valorQuebra = valorQuebra,
            observacaoQuebra = observacaoQuebra,
        )
        AppLog.d("FINALIZAR_VIAGEM_BODY", body.toString())
        apiService.finalizarViagem(id, body).use { }
    }

    suspend fun pagarViagem(id: String) {
        apiService.pagarViagem(id).use { }
    }

    suspend fun createViagem(
        origem: String,
        destino: String,
        numeroToneladas: Double,
        valorTonelada: Double,
        cliente: String,
        cnpjCliente: String,
        tipoCarga: String,
        kmInicial: Double,
    ) {
        val body = CreateViagemRequest(
            origem = origem,
            destino = destino,
            numeroToneladas = numeroToneladas,
            valorTonelada = valorTonelada,
            cliente = cliente,
            cnpjCliente = cnpjCliente,
            tipoCarga = tipoCarga,
            kmInicial = kmInicial,
        )
        AppLog.d("CREATE_VIAGEM_BODY", body.toString())
        apiService.createViagem(body).use { }
    }

    suspend fun createDespesa(
        caminhaoId: String,
        valor: Double,
        tipo: String,
        data: String,
        descricao: String?,
        viagemId: String? = null,
        comprovante: ComprovantePayload? = null,
    ) {
        AppLog.d(
            "CREATE_DESPESA",
            "caminhaoId=$caminhaoId valor=$valor tipo=$tipo viagemId=$viagemId comprovante=${comprovante != null}",
        )
        apiService.createDespesa(
            caminhaoId = textPart(caminhaoId),
            valor = textPart(valor.toString()),
            tipo = textPart(tipo),
            data = textPart(data),
            viagemId = viagemId?.let { textPart(it) },
            comprovante = comprovante?.let {
                imagePart(it.bytes, it.mimeType, it.fileName)
            },
        ).use { }
    }

    suspend fun createAbastecimento(
        caminhaoId: String,
        litros: Double,
        valorLitro: Double,
        data: String,
        viagemId: String? = null,
        comprovante: ComprovantePayload? = null,
    ) {
        AppLog.d(
            "CREATE_ABASTECIMENTO",
            "caminhaoId=$caminhaoId litros=$litros valorLitro=$valorLitro viagemId=$viagemId comprovante=${comprovante != null}",
        )
        apiService.createAbastecimento(
            caminhaoId = textPart(caminhaoId),
            litros = textPart(litros.toString()),
            valorLitro = textPart(valorLitro.toString()),
            data = textPart(data),
            viagemId = viagemId?.let { textPart(it) },
            comprovante = comprovante?.let {
                imagePart(it.bytes, it.mimeType, it.fileName)
            },
        ).use { }
    }
}
