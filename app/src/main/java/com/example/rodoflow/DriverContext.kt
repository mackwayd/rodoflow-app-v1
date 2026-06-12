package com.example.rodoflow

/** App dedicado a um único motorista e caminhão (seed do backend). */
object DriverContext {
    const val CAMINHAO_ID = "cam-1"

    val motoristaId: String
        get() = AppServices.authSession.motoristaIdOrDefault()
}
