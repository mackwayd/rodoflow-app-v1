package com.example.rodoflow.data.repository

import android.util.Log
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
        dataInicio: String,
    ) {
        val body = CreateViagemRequest(
            motoristaId = motoristaId,
            caminhaoId = caminhaoId,
            origem = origem,
            destino = destino,
            valorBruto = valorBruto,
            dataInicio = dataInicio,
        )
        Log.d("CREATE_VIAGEM_BODY", body.toString())
        apiService.createViagem(body).use { }
    }

    suspend fun createDespesa(
        caminhaoId: String,
        valor: Double,
        tipo: String,
        data: String,
        descricao: String?,
    ) {
        val body = CreateDespesaRequest(
            caminhaoId = caminhaoId,
            valor = valor,
            tipo = tipo,
            data = data,
            descricao = descricao,
        )
        Log.d("CREATE_DESPESA_BODY", body.toString())
        apiService.createDespesa(body).use { }
    }

    suspend fun createAbastecimento(
        caminhaoId: String,
        litros: Double,
        valorTotal: Double,
        data: String,
    ) {
        val body = CreateAbastecimentoRequest(
            caminhaoId = caminhaoId,
            litros = litros,
            valorTotal = valorTotal,
            data = data,
        )
        Log.d("CREATE_ABASTECIMENTO_BODY", body.toString())
        apiService.createAbastecimento(body).use { }
    }
}
