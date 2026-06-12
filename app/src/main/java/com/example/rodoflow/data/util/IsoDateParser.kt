package com.example.rodoflow.data.util

import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset

/**
 * Converte strings ISO (OffsetDateTime, Instant ou LocalDateTime) em [Instant] para comparação.
 */
fun parseIsoToInstant(isoDateTime: String?): Instant? {
    if (isoDateTime.isNullOrBlank()) return null
    val value = isoDateTime.trim()
    return runCatching {
        OffsetDateTime.parse(value).toInstant()
    }.recoverCatching {
        Instant.parse(value)
    }.recoverCatching {
        LocalDateTime.parse(value).toInstant(ZoneOffset.UTC)
    }.getOrNull()
}
