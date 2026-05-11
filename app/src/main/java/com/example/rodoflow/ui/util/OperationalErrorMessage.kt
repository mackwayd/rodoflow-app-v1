package com.example.rodoflow.ui.util

import retrofit2.HttpException
import java.io.IOException
import java.io.InterruptedIOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException

const val MSG_NO_INTERNET = "Sem conexão com a internet"
const val MSG_SERVER_UNAVAILABLE = "Servidor indisponível no momento"
const val MSG_TIMEOUT = "A conexão demorou mais que o esperado"
const val MSG_GENERIC_OPERATION = "Não foi possível concluir a operação"

const val MSG_LOAD_DATA_FAILED_TITLE = "Não foi possível carregar os dados"
const val MSG_LOAD_DATA_FAILED_SUBTITLE = "Verifique sua conexão e tente novamente"
const val MSG_RETRY_BUTTON = "Tentar novamente"

/**
 * Converte falhas de rede/HTTP em texto seguro para o motorista (sem detalhes técnicos).
 * Detalhes permanecem apenas em logs.
 */
fun userMessageForThrowable(e: Throwable): String {
    if (e is HttpException) {
        return when (e.code()) {
            408 -> MSG_TIMEOUT
            in 500..599 -> MSG_SERVER_UNAVAILABLE
            else -> MSG_GENERIC_OPERATION
        }
    }

    var current: Throwable? = e
    while (current != null) {
        when (current) {
            is SocketTimeoutException -> return MSG_TIMEOUT
            is UnknownHostException -> return MSG_NO_INTERNET
            is ConnectException -> return MSG_SERVER_UNAVAILABLE
            is SSLException -> return MSG_NO_INTERNET
            is InterruptedIOException -> {
                val m = current.message.orEmpty()
                if (m.contains("timeout", ignoreCase = true)) return MSG_TIMEOUT
            }
        }
        current = current.cause
    }

    if (e is IOException) {
        val m = e.message.orEmpty()
        if (m.contains("ENETUNREACH", ignoreCase = true) ||
            m.contains("network is unreachable", ignoreCase = true) ||
            m.contains("failed to connect", ignoreCase = true)
        ) {
            return MSG_NO_INTERNET
        }
        if (m.contains("timeout", ignoreCase = true)) return MSG_TIMEOUT
    }

    return MSG_GENERIC_OPERATION
}
