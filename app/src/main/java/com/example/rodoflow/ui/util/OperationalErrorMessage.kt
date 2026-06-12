package com.example.rodoflow.ui.util

import com.google.gson.JsonParser
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
const val MSG_VIAGEM_EM_ANDAMENTO = "Finalize a viagem atual antes de iniciar outra"
const val MSG_ACESSO_NEGADO = "Você não tem permissão para esta ação"
const val MSG_NAO_AUTORIZADO = "Sessão inválida. Verifique a chave de acesso do app"

const val MSG_LOAD_DATA_FAILED_TITLE = "Não foi possível carregar os dados"
const val MSG_LOAD_DATA_FAILED_SUBTITLE = "Verifique sua conexão e tente novamente"
const val MSG_RETRY_BUTTON = "Tentar novamente"
const val MSG_QUEUED_OFFLINE =
    "Salvo no celular. Será enviado automaticamente quando houver internet."
const val MSG_SYNC_IN_PROGRESS = "Enviando dados pendentes…"
const val MSG_SYNC_COMPLETE = "Envios concluídos. Dados atualizados."
const val MSG_CACHED_DATA_BANNER =
    "Exibindo dados salvos no celular. Puxe para atualizar quando houver internet."
const val MSG_REFRESH_FAILED_WITH_CACHE =
    "Não foi possível atualizar agora. Exibindo a última versão salva no celular."
const val MSG_OFFLINE_NO_DATA =
    "Sem conexão e viagem não disponível offline. Tente novamente quando houver internet."
const val MSG_VISIBLE_WINDOW_HINT =
    "Mostrando viagens dos últimos 30 dias"
const val MSG_FINANCEIRO_WINDOW_HINT =
    "Últimos 30 dias"
const val MSG_FINANCEIRO_EMPTY_TITLE =
    "Tudo tranquilo por aqui"
const val MSG_FINANCEIRO_EMPTY_SUBTITLE =
    "Não há viagens com movimentação financeira neste período."
const val MSG_FINANCEIRO_EMPTY_HINT =
    "Novas viagens aparecem aqui automaticamente. O histórico completo continua disponível no painel administrativo."

fun operationSuccessMessage(baseMessage: String, queued: Boolean): String =
    if (queued) MSG_QUEUED_OFFLINE else baseMessage

/**
 * Converte falhas de rede/HTTP em texto seguro para o motorista (sem detalhes técnicos).
 * Detalhes permanecem apenas em logs.
 */
fun userMessageForThrowable(e: Throwable): String {
    if (e is HttpException) {
        return userMessageForHttpException(e)
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

fun userMessageForHttpException(e: HttpException): String {
    val apiMessage = parseApiErrorMessage(e)
    when (e.code()) {
        401 -> return MSG_NAO_AUTORIZADO
        403 -> return apiMessage ?: MSG_ACESSO_NEGADO
        408 -> return MSG_TIMEOUT
        409 -> return apiMessage ?: MSG_VIAGEM_EM_ANDAMENTO
        in 500..599 -> return MSG_SERVER_UNAVAILABLE
    }
    return apiMessage ?: MSG_GENERIC_OPERATION
}

/** Indica se a falha pode ser reenviada mais tarde (rede/servidor). */
fun isRetriableNetworkThrowable(e: Throwable): Boolean {
    if (e is HttpException) {
        return e.code() == 408 || e.code() in 500..599
    }
    return when (userMessageForThrowable(e)) {
        MSG_NO_INTERNET,
        MSG_TIMEOUT,
        MSG_SERVER_UNAVAILABLE,
        -> true
        else -> false
    }
}

private fun parseApiErrorMessage(e: HttpException): String? {
    val raw = runCatching { e.response()?.errorBody()?.string()?.trim() }.getOrNull()
        ?: return null
    if (raw.isEmpty()) return null

    return runCatching {
        val json = JsonParser.parseString(raw)
        if (json.isJsonObject && json.asJsonObject.has("error")) {
            json.asJsonObject.get("error").asString.trim().takeIf { it.isNotEmpty() }
        } else {
            null
        }
    }.getOrNull() ?: raw.takeIf { it.length <= 120 }
}
