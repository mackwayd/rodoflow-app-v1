package com.example.rodoflow.data.api

import com.example.rodoflow.data.model.CreateViagemRequest
import com.example.rodoflow.data.model.CreateAbastecimentoRequest
import com.example.rodoflow.data.model.CreateDespesaRequest
import com.example.rodoflow.data.model.FinalizarViagemRequest
import com.example.rodoflow.data.model.ResumoViagem
import com.example.rodoflow.data.model.SaldoMotorista
import com.example.rodoflow.data.model.Viagem
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {

    @GET("financeiro/resumo")
    suspend fun getResumo(): ResponseBody

    @GET("financeiro/saldo")
    suspend fun getSaldo(): ResponseBody

    @GET("viagens")
    suspend fun getViagens(@Query("motoristaId") motoristaId: String): List<Viagem>

    @GET("viagens/{id}")
    suspend fun getViagemById(@Path("id") id: String): Viagem

    @PATCH("viagens/{id}/finalizar")
    suspend fun finalizarViagem(
        @Path("id") id: String,
        @Body body: FinalizarViagemRequest,
    ): ResponseBody

    @PATCH("viagens/{id}/pagar")
    suspend fun pagarViagem(@Path("id") id: String): ResponseBody

    @POST("viagens")
    suspend fun createViagem(@Body body: CreateViagemRequest): ResponseBody

    @POST("despesas")
    suspend fun createDespesa(@Body body: CreateDespesaRequest): ResponseBody

    @POST("abastecimentos")
    suspend fun createAbastecimento(@Body body: CreateAbastecimentoRequest): ResponseBody
}
