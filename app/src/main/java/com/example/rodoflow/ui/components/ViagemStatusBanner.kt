package com.example.rodoflow.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.util.Locale

@Composable
fun ViagemStatusBanner(
    status: String,
    modifier: Modifier = Modifier,
) {
    val message = statusBannerMessage(status) ?: return
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = statusColor(status).copy(alpha = 0.12f),
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = statusColor(status),
        )
    }
}

private fun statusBannerMessage(status: String): String? {
    return when (status.trim().uppercase(Locale.ROOT).replace(' ', '_')) {
        "EM_ANDAMENTO" -> "Registre despesas e abastecimentos durante a viagem."
        "FINALIZADA" -> "Aguardando acerto com a transportadora."
        "PAGA" -> "Viagem quitada."
        else -> null
    }
}
