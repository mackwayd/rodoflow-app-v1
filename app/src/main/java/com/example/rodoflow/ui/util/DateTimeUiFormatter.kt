package com.example.rodoflow.ui.util

import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val outputFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
private val appZoneId: ZoneId = ZoneId.of("America/Sao_Paulo")

fun formatIsoDateTimeBr(isoDateTime: String?): String {
    if (isoDateTime.isNullOrBlank()) return "-"

    val value = isoDateTime.trim()

    return runCatching {
        OffsetDateTime.parse(value).atZoneSameInstant(appZoneId).format(outputFormatter)
    }.recoverCatching {
        Instant.parse(value).atZone(appZoneId).format(outputFormatter)
    }.recoverCatching {
        LocalDateTime.parse(value).format(outputFormatter)
    }.getOrElse {
        value
    }
}
