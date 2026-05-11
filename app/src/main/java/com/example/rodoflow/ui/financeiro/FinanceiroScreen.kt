package com.example.rodoflow.ui.financeiro

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rodoflow.data.model.ResumoViagem
import com.example.rodoflow.ui.components.LoadDataErrorPanel
import com.example.rodoflow.ui.components.StatusBadge
import com.example.rodoflow.ui.components.saldoResultadoColor
import com.example.rodoflow.ui.util.formatBrl
import com.example.rodoflow.ui.util.formatKg

private val WarningSurface = Color(0xFFFFF3E0)
private val WarningOnSurface = Color(0xFFB45309)

@Composable
fun FinanceiroScreen(
    reloadNonce: Int = 0,
    viewModel: FinanceiroViewModel = viewModel(),
) {
    val loading by viewModel.loading.collectAsStateWithLifecycle()
    val loadFailed by viewModel.loadFailed.collectAsStateWithLifecycle()
    val resumos by viewModel.resumos.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadResumo()
    }
    LaunchedEffect(reloadNonce) {
        if (reloadNonce > 0) {
            viewModel.loadResumo()
        }
    }

    when {
        loading && resumos.isEmpty() -> {
            FinanceiroLoading()
        }
        loadFailed && resumos.isEmpty() -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                LoadDataErrorPanel(onRetry = { viewModel.loadResumo() })
            }
        }
        resumos.isEmpty() -> {
            FinanceiroEmpty()
        }
        else -> {
            FinanceiroContent(
                resumos = resumos,
                isRefreshing = loading,
            )
        }
    }
}

@Composable
private fun FinanceiroLoading() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "Carregando...")
        }
    }
}

@Composable
private fun FinanceiroEmpty() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp),
        ) {
            Text(
                text = "Nenhuma viagem encontrada",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Quando houver viagens registradas, elas aparecem aqui.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun FinanceiroContent(
    resumos: List<ResumoViagem>,
    isRefreshing: Boolean,
) {
    val totals = remember(resumos) { computeTotals(resumos) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            FinanceiroHeader(isRefreshing = isRefreshing)
        }
        item {
            ResumoPrincipalCard(totals = totals)
        }
        item {
            CustosCard(totals = totals)
        }
        item {
            Text(
                text = "Viagens (${resumos.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
        items(items = resumos, key = { it.viagemId.ifBlank { it.hashCode().toString() } }) { resumo ->
            ViagemOperacionalCard(resumo = resumo)
        }
    }
}

@Composable
private fun FinanceiroHeader(isRefreshing: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            Text(
                text = "Painel financeiro",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "Operacional → custos → resultado",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "Visão detalhada das viagens",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (isRefreshing) {
            CircularProgressIndicator(
                modifier = Modifier.padding(start = 8.dp),
                strokeWidth = 2.dp,
            )
        }
    }
}

@Composable
private fun ResumoPrincipalCard(totals: FinanceiroTotals) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Resumo",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(12.dp))
            ResumoLine(label = "Total bruto", value = formatBrl(totals.valorBrutoEfetivo))
            Spacer(modifier = Modifier.height(6.dp))
            ResumoLine(label = "Total despesas", value = formatBrl(totals.totalDespesas))
            Spacer(modifier = Modifier.height(6.dp))
            ResumoLine(label = "Total abastecimentos", value = formatBrl(totals.totalAbastecimentos))
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Saldo final",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = formatBrl(totals.saldoEmpresa),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = saldoResultadoColor(totals.saldoEmpresa),
            )
        }
    }
}

@Composable
private fun ResumoLine(
    label: String,
    value: String,
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "$label: ",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun CustosCard(totals: FinanceiroTotals) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Custos",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(12.dp))
            ContextLine(label = "Despesas", value = formatBrl(totals.totalDespesas))
            Spacer(modifier = Modifier.height(6.dp))
            ContextLine(label = "Abastecimentos", value = formatBrl(totals.totalAbastecimentos))
            if (totals.valorQuebra > 0.0) {
                Spacer(modifier = Modifier.height(8.dp))
                ContextLine(label = "Valor quebra", value = formatBrl(totals.valorQuebra))
            }
        }
    }
}

@Composable
private fun ViagemOperacionalCard(resumo: ResumoViagem) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "${resumo.origem.ifBlank { "-" }} → ${resumo.destino.ifBlank { "-" }}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(10.dp))
            ContextLine(label = "Cliente", value = resumo.cliente)
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Valor bruto",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = formatBrl(resumo.valorBrutoEfetivoOrFallback),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "Status financeiro:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                StatusBadge(status = statusPagamentoParaBadge(resumo.statusPagamento))
            }
            if (resumo.temQuebra) {
                Spacer(modifier = Modifier.height(12.dp))
                QuebraBanner(
                    valorQuebra = resumo.valorQuebra,
                    kgPerdido = resumo.kgPerdido,
                )
            }
        }
    }
}

private fun statusPagamentoParaBadge(raw: String): String {
    val u = raw.trim().uppercase()
    return when {
        u.isEmpty() || u.equals("SEM_STATUS", ignoreCase = true) -> "PENDENTE"
        u == "PAGA" -> "PAGO"
        else -> raw.trim().ifBlank { "PENDENTE" }
    }
}

@Composable
private fun ContextLine(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "$label: ",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value.ifBlank { "-" },
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun QuebraBanner(
    valorQuebra: Double,
    kgPerdido: Double?,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(WarningSurface, RoundedCornerShape(8.dp))
            .padding(12.dp),
    ) {
        Column {
            Text(
                text = "Quebra registrada",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = WarningOnSurface,
            )
            Spacer(modifier = Modifier.height(4.dp))
            if (kgPerdido != null) {
                Text(
                    text = "KG perdido: ${formatKg(kgPerdido)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = WarningOnSurface,
                )
            }
            if (valorQuebra > 0.0) {
                Text(
                    text = "Valor quebra: ${formatBrl(valorQuebra)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = WarningOnSurface,
                )
            }
        }
    }
}

private data class FinanceiroTotals(
    val valorBrutoEfetivo: Double,
    val saldoEmpresa: Double,
    val totalDespesas: Double,
    val totalAbastecimentos: Double,
    val valorQuebra: Double,
)

private fun computeTotals(resumos: List<ResumoViagem>): FinanceiroTotals = FinanceiroTotals(
    valorBrutoEfetivo = resumos.sumOf { it.valorBrutoEfetivoOrFallback },
    saldoEmpresa = resumos.sumOf { it.saldoEmpresaOrFallback },
    totalDespesas = resumos.sumOf { it.totalDespesas },
    totalAbastecimentos = resumos.sumOf { it.totalAbastecimentos },
    valorQuebra = resumos.sumOf { it.valorQuebra },
)
