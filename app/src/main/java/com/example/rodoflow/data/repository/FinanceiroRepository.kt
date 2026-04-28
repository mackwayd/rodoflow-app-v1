package com.example.rodoflow.data.repository

import com.example.rodoflow.data.api.ApiService
import com.example.rodoflow.data.api.RetrofitInstance
import com.example.rodoflow.data.model.ResumoViagem
import com.example.rodoflow.data.model.SaldoMotorista

class FinanceiroRepository(
    private val apiService: ApiService = RetrofitInstance.api,
) {

    suspend fun getResumo(): List<ResumoViagem> = apiService.getResumo()

    suspend fun getSaldo(): List<SaldoMotorista> = apiService.getSaldo()
}
