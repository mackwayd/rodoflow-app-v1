package com.example.rodoflow.data.repository

import com.example.rodoflow.AppLog
import com.example.rodoflow.data.api.ApiService
import com.example.rodoflow.data.api.RetrofitInstance
import com.example.rodoflow.data.model.CreateAbastecimentoRequest
import com.example.rodoflow.data.model.CreateDespesaRequest
import com.example.rodoflow.data.model.CreateViagemRequest
import com.example.rodoflow.data.model.FinalizarViagemRequest
import com.example.rodoflow.data.model.Viagem

class ViagemRepository(
    private val apiService: ApiService = RetrofitInstance.api,
) {

    suspend fun getViagens(motoristaId: String): List<Viagem> = apiService.getViagens(motoristaId)
    suspend fun getViagemById(id: String): Viagem? = runCatching {
        apiService.getViagemById(id)
    }.getOrNull()

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
    ) {
        val body = CreateDespesaRequest(
            caminhaoId = caminhaoId,
            valor = valor,
            tipo = tipo,
            data = data,
            descricao = descricao,
            viagemId = viagemId,
        )
        AppLog.d("CREATE_DESPESA_BODY", body.toString())
        apiService.createDespesa(body).use { }
    }

    suspend fun createAbastecimento(
        caminhaoId: String,
        litros: Double,
        valorLitro: Double,
        data: String,
        viagemId: String? = null,
    ) {
        val body = CreateAbastecimentoRequest(
            caminhaoId = caminhaoId,
            litros = litros,
            valorLitro = valorLitro,
            data = data,
            viagemId = viagemId,
        )
        AppLog.d("CREATE_ABASTECIMENTO_BODY", body.toString())
        apiService.createAbastecimento(body).use { }
    }
}
