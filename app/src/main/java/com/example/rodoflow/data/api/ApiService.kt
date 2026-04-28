package com.example.rodoflow.data.api

import com.example.rodoflow.data.model.CreateViagemRequest
import com.example.rodoflow.data.model.CreateAbastecimentoRequest
import com.example.rodoflow.data.model.CreateDespesaRequest
import com.example.rodoflow.data.model.ResumoViagem
import com.example.rodoflow.data.model.SaldoMotorista
import com.example.rodoflow.data.model.Viagem
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {

    @GET("financeiro/resumo")
    suspend fun getResumo(): List<ResumoViagem>

    @GET("financeiro/saldo")
    suspend fun getSaldo(): List<SaldoMotorista>

    @GET("viagens")
    suspend fun getViagens(@Query("motoristaId") motoristaId: String): List<Viagem>

    @GET("viagens/{id}")
    suspend fun getViagemById(@Path("id") id: String): Viagem

    @POST("viagens")
    suspend fun createViagem(@Body body: CreateViagemRequest): ResponseBody

    @POST("despesas")
    suspend fun createDespesa(@Body body: CreateDespesaRequest): ResponseBody

    @POST("abastecimentos")
    suspend fun createAbastecimento(@Body body: CreateAbastecimentoRequest): ResponseBody
}
