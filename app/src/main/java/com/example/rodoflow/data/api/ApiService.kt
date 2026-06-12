package com.example.rodoflow.data.api

import com.example.rodoflow.data.model.CreateViagemRequest
import com.example.rodoflow.data.model.LoginRequest
import com.example.rodoflow.data.model.LoginResponse
import com.example.rodoflow.data.model.FinalizarViagemRequest
import com.example.rodoflow.data.model.ResumoViagem
import com.example.rodoflow.data.model.SaldoMotorista
import com.example.rodoflow.data.model.Viagem
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {

    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): LoginResponse

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

    @Multipart
    @POST("despesas")
    suspend fun createDespesa(
        @Part("caminhaoId") caminhaoId: okhttp3.RequestBody,
        @Part("valor") valor: okhttp3.RequestBody,
        @Part("tipo") tipo: okhttp3.RequestBody,
        @Part("data") data: okhttp3.RequestBody,
        @Part("viagemId") viagemId: okhttp3.RequestBody?,
        @Part comprovante: MultipartBody.Part?,
    ): ResponseBody

    @Multipart
    @POST("abastecimentos")
    suspend fun createAbastecimento(
        @Part("caminhaoId") caminhaoId: okhttp3.RequestBody,
        @Part("litros") litros: okhttp3.RequestBody,
        @Part("valorLitro") valorLitro: okhttp3.RequestBody,
        @Part("data") data: okhttp3.RequestBody,
        @Part("viagemId") viagemId: okhttp3.RequestBody?,
        @Part comprovante: MultipartBody.Part?,
    ): ResponseBody
}
