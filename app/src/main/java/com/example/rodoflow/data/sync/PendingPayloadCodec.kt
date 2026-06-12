package com.example.rodoflow.data.sync

import com.google.gson.Gson

object PendingPayloadCodec {
    private val gson = Gson()

    fun encode(payload: Any): String = gson.toJson(payload)

    fun decodeCreateViagem(json: String): CreateViagemPayload =
        gson.fromJson(json, CreateViagemPayload::class.java)

    fun decodeCreateDespesa(json: String): CreateDespesaPayload =
        gson.fromJson(json, CreateDespesaPayload::class.java)

    fun decodeCreateAbastecimento(json: String): CreateAbastecimentoPayload =
        gson.fromJson(json, CreateAbastecimentoPayload::class.java)

    fun decodeFinalizarViagem(json: String): FinalizarViagemPayload =
        gson.fromJson(json, FinalizarViagemPayload::class.java)
}
