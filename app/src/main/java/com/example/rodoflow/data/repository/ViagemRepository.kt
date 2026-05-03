package com.example.rodoflow.data.repository

import com.example.rodoflow.data.api.ApiService
import com.example.rodoflow.data.api.RetrofitInstance
import com.example.rodoflow.data.model.CreateAbastecimentoRequest
import com.example.rodoflow.data.model.CreateDespesaRequest
import com.example.rodoflow.data.model.CreateViagemRequest
import com.example.rodoflow.data.model.Viagem

class ViagemRepository(
    private val apiService: ApiService = RetrofitInstance.api,
) {

    suspend fun getViagens(motoristaId: String): List<Viagem> = apiService.getViagens(motoristaId)
    suspend fun getViagemById(id: String): Viagem? = runCatching {
        apiService.getViagemById(id)
    }.getOrNull()

    suspend fun finalizarViagem(id: String) {
        apiService.finalizarViagem(id).use { }
    }

    suspend fun pagarViagem(id: String) {
        apiService.pagarViagem(id).use { }
    }

    suspend fun createViagem(
        motoristaId: String,
        caminhaoId: String,
        origem: String,
        destino: String,
        valorBruto: Double,
    ) {
        apiService.createViagem(
            CreateViagemRequest(
                motoristaId = motoristaId,
                caminhaoId = caminhaoId,
                origem = origem,
                destino = destino,
                valorBruto = valorBruto,
            ),
        ).use { }
    }

    suspend fun createDespesa(
        viagemId: String,
        descricao: String,
        valor: Double,
    ) {
        apiService.createDespesa(
            CreateDespesaRequest(
                viagemId = viagemId,
                descricao = descricao,
                valor = valor,
            ),
        ).use { }
    }

    suspend fun createAbastecimento(
        viagemId: String,
        litros: Double,
        valorTotal: Double,
    ) {
        apiService.createAbastecimento(
            CreateAbastecimentoRequest(
                viagemId = viagemId,
                litros = litros,
                valorTotal = valorTotal,
            ),
        ).use { }
    }
}
