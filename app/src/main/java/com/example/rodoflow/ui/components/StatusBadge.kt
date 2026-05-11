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

/**
 * Cores oficiais dos status operacionais do RodoFlow.
 * Reutilizadas em Home, Financeiro e Viagem detalhe para manter consistência.
 */
val StatusEmAndamentoColor = Color(0xFF1565C0)
val StatusFinalizadaColor = Color(0xFF2E7D32)
val StatusPagaColor = Color(0xFF6A1B9A)
val StatusNeutroColor = Color(0xFF616161)

/** Verde para saldo / pagamento positivo (resultado favorável). */
val SaldoPositivoColor = Color(0xFF2E7D32)

/** Vermelho para saldo negativo. */
val SaldoNegativoColor = Color(0xFFC62828)

fun statusDisplayLabel(status: String): String {
    val t = status.trim()
    if (t.isEmpty() || t.equals("SEM_STATUS", ignoreCase = true)) return "PENDENTE"
    return t
}

fun statusColor(status: String): Color {
    val key = statusDisplayLabel(status).uppercase()
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
            .background(statusColor(status), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    )
}
