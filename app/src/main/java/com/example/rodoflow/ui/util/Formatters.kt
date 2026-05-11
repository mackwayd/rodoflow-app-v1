package com.example.rodoflow.ui.util

import java.text.NumberFormat
import java.util.Locale

private val ptBr: Locale = Locale.forLanguageTag("pt-BR")

private val brlFormatter: NumberFormat by lazy {
    NumberFormat.getCurrencyInstance(ptBr).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }
}

private val toneladasFormatter: NumberFormat by lazy {
    NumberFormat.getNumberInstance(ptBr).apply {
        minimumFractionDigits = 0
        maximumFractionDigits = 2
    }
}

private val kmFormatter: NumberFormat by lazy {
    NumberFormat.getNumberInstance(ptBr).apply {
        minimumFractionDigits = 0
        maximumFractionDigits = 0
        isGroupingUsed = true
    }
}

fun formatBrl(value: Double): String = brlFormatter.format(value)

fun formatBrl(value: Double?): String = brlFormatter.format(value ?: 0.0)

fun formatToneladas(value: Double): String {
    if (value <= 0.0) return "-"
    return "${toneladasFormatter.format(value)} t"
}

fun formatToneladas(value: Double?): String =
    if (value == null) "-" else formatToneladas(value)

fun formatKm(value: Double): String {
    if (value < 0.0) return "-"
    return "${kmFormatter.format(value)} km"
}

fun formatKm(value: Double?): String =
    if (value == null) "-" else formatKm(value)

fun formatKg(value: Double): String {
    if (value < 0.0) return "-"
    return "${kmFormatter.format(value)} kg"
}

fun formatKg(value: Double?): String =
    if (value == null) "-" else formatKg(value)

fun formatCnpj(raw: String?): String {
    val source = raw?.trim().orEmpty()
    if (source.isEmpty()) return ""
    val digits = source.filter { it.isDigit() }
    if (digits.length != 14) return source
    return buildString {
        append(digits, 0, 2)
        append('.')
        append(digits, 2, 5)
        append('.')
        append(digits, 5, 8)
        append('/')
        append(digits, 8, 12)
        append('-')
        append(digits, 12, 14)
    }
}
