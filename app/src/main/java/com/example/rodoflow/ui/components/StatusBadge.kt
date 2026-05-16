package com.example.rodoflow.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.rodoflow.ui.theme.RodoFlowBlue
import com.example.rodoflow.ui.theme.RodoFlowBlueMuted
import java.util.Locale

/**
 * Cores oficiais dos status operacionais do RodoFlow.
 * Reutilizadas em Home, Financeiro e Viagem detalhe para manter consistência.
 */
val StatusEmAndamentoColor = RodoFlowBlue
val StatusFinalizadaColor = Color(0xFF2E7D32)
val StatusPagaColor = RodoFlowBlueMuted
val StatusNeutroColor = Color(0xFF64748B)

/** Verde para saldo / pagamento positivo (resultado favorável). */
val SaldoPositivoColor = Color(0xFF2E7D32)

/** Vermelho para saldo negativo. */
val SaldoNegativoColor = Color(0xFFC62828)

private fun normalizedStatusKey(status: String): String =
    status.trim().uppercase(Locale.ROOT).replace(" ", "_")

fun statusDisplayLabel(status: String): String {
    val key = normalizedStatusKey(status)
    if (key.isEmpty() || key == "SEM_STATUS") return "Pendente"
    return when (key) {
        "EM_ANDAMENTO" -> "Em andamento"
        "FINALIZADA" -> "Finalizada"
        "PAGA" -> "Paga"
        "PAGO" -> "Pago"
        "PENDENTE" -> "Pendente"
        else -> humanizeRawStatus(status.trim())
    }
}

private fun humanizeRawStatus(raw: String): String =
    raw.split("_").filter { it.isNotBlank() }.joinToString(" ") { part ->
        part.lowercase(Locale.getDefault()).replaceFirstChar { c ->
            if (c.isLowerCase()) c.titlecase(Locale.getDefault()) else c.toString()
        }
    }.ifBlank { raw }

fun statusColor(status: String): Color {
    val key = normalizedStatusKey(status)
    return when (key) {
        "EM_ANDAMENTO" -> StatusEmAndamentoColor
        "FINALIZADA" -> StatusFinalizadaColor
        "PAGA" -> StatusPagaColor
        "PAGO" -> SaldoPositivoColor
        "PENDENTE" -> StatusNeutroColor
        else -> StatusNeutroColor
    }
}

fun saldoResultadoColor(saldo: Double): Color = when {
    saldo > 0.0 -> SaldoPositivoColor
    saldo < 0.0 -> SaldoNegativoColor
    else -> StatusNeutroColor
}

@Composable
fun StatusBadge(
    status: String,
    modifier: Modifier = Modifier,
) {
    val label = statusDisplayLabel(status)
    Text(
        text = label,
        color = Color.White,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier
            .background(statusColor(status), RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 6.dp),
    )
}
