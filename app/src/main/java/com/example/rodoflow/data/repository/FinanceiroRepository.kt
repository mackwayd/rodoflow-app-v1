package com.example.rodoflow.data.repository

import com.example.rodoflow.data.api.ApiService
import com.example.rodoflow.data.api.RetrofitInstance
import com.example.rodoflow.data.model.ResumoViagem
import com.example.rodoflow.data.model.SaldoEmpresaResponse
import com.example.rodoflow.data.model.SaldoMotorista
import com.example.rodoflow.AppLog
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken

class FinanceiroRepository(
    private val apiService: ApiService = RetrofitInstance.api,
) {

    private val gson = Gson()

    suspend fun getResumo(): List<ResumoViagem> {
        val json = apiService.getResumo().string()
        return parseListPayload(
            json = json,
            preferredKeys = listOf("data", "resumo", "resumos"),
        )
    }

    suspend fun getSaldo(): List<SaldoMotorista> {
        val json = apiService.getSaldo().string()
        AppLog.d("HOME_JSON", json)
        val saldoResponse = gson.fromJson(json, SaldoEmpresaResponse::class.java)
        val saldo = saldoResponse.saldoEmpresa.toDoubleOrNull() ?: 0.0
        return listOf(
            SaldoMotorista(
                motoristaId = "empresa",
                motoristaNome = "Empresa",
                totalViagens = 0,
                saldoPendente = saldo,
            ),
        )
    }

    private inline fun <reified T> parseListPayload(
        json: String,
        preferredKeys: List<String>,
    ): List<T> {
        val root = JsonParser.parseString(json)
        val listElement = extractListElement(root, preferredKeys)
            ?: error("Resposta inesperada: esperado array ou objeto com lista")
        val type = object : TypeToken<List<T>>() {}.type
        return gson.fromJson(listElement, type)
    }

    private fun extractListElement(root: JsonElement, preferredKeys: List<String>): JsonElement? {
        if (root.isJsonArray) return root
        if (!root.isJsonObject) return null
        val obj = root.asJsonObject
        preferredKeys.firstNotNullOfOrNull { key ->
            obj.get(key)?.takeIf { it.isJsonArray }
        }?.let { return it }
        return obj.entrySet().firstOrNull { (_, value) -> value.isJsonArray }?.value
    }
}
