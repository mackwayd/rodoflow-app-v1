package com.example.rodoflow.data.repository

import com.example.rodoflow.data.model.Viagem

sealed class ViagemListLoadResult {
    data class Success(
        val viagens: List<Viagem>,
        val fromCache: Boolean = false,
    ) : ViagemListLoadResult()

    data class Error(val cause: Throwable) : ViagemListLoadResult()
}

sealed class ViagemDetailLoadResult {
    data class Success(
        val viagem: Viagem,
        val fromCache: Boolean = false,
    ) : ViagemDetailLoadResult()

    data object NotFound : ViagemDetailLoadResult()

    data class Error(val cause: Throwable) : ViagemDetailLoadResult()
}
