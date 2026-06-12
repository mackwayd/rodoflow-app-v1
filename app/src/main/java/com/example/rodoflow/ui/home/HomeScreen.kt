package com.example.rodoflow.ui.home

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.AddRoad
import androidx.compose.material.icons.outlined.LocalGasStation
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rodoflow.data.model.Viagem
import com.example.rodoflow.ui.components.CachedDataBanner
import com.example.rodoflow.ui.components.LoadDataErrorPanel
import com.example.rodoflow.ui.components.PullRefreshBox
import com.example.rodoflow.ui.components.ViagemStatusBanner
import com.example.rodoflow.ui.components.StatusEmAndamentoColor
import com.example.rodoflow.ui.components.StatusFinalizadaColor
import com.example.rodoflow.ui.components.StatusPagaColor
import com.example.rodoflow.ui.components.saldoResultadoColor
import com.example.rodoflow.ui.theme.AppButtonShape
import com.example.rodoflow.ui.theme.AppCardShape
import com.example.rodoflow.ui.theme.AppCompactCardShape
import com.example.rodoflow.ui.util.formatBrl
import com.example.rodoflow.ui.util.formatRouteSegment
import com.example.rodoflow.ui.util.formatToneladas

@Composable
fun HomeScreen(
    reloadNonce: Int = 0,
    onNovaViagem: () -> Unit = {},
    onNovoAbastecimento: (viagemId: String?) -> Unit = {},
    onNovaDespesa: (viagemId: String?) -> Unit = {},
    onAbrirViagemAtual: (viagemId: String) -> Unit = {},
    viewModel: HomeViewModel = viewModel(),
) {
    val loading by viewModel.loading.collectAsStateWithLifecycle()
    val loadFailed by viewModel.loadFailed.collectAsStateWithLifecycle()
    val isShowingCachedData by viewModel.isShowingCachedData.collectAsStateWithLifecycle()
    val refreshFailedWithData by viewModel.refreshFailedWithData.collectAsStateWithLifecycle()
    val saldo by viewModel.saldo.collectAsStateWithLifecycle()
    val viagemAtual by viewModel.viagemAtual.collectAsStateWithLifecycle()
    val statusCounts by viewModel.statusCounts.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadDashboard()
    }
    LaunchedEffect(reloadNonce) {
        if (reloadNonce > 0) {
            viewModel.loadDashboard()
        }
    }

    when {
        loading && viagemAtual == null && saldo.isEmpty() -> {
            DashboardLoading()
        }
        loadFailed && viagemAtual == null && saldo.isEmpty() -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                LoadDataErrorPanel(onRetry = { viewModel.loadDashboard() })
            }
        }
        else -> {
            PullRefreshBox(
                refreshing = loading,
                onRefresh = { viewModel.loadDashboard() },
                modifier = Modifier.fillMaxSize(),
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    CachedDataBanner(
                        isShowingCachedData = isShowingCachedData,
                        refreshFailedWithData = refreshFailedWithData,
                    )
                    DashboardContent(
                    viagemAtual = viagemAtual,
                    resultadoAcumulado = saldo.firstOrNull()?.saldoPendente ?: 0.0,
                    statusCounts = statusCounts,
                    temViagemEmAndamento = viagemAtual != null,
                    onNovaViagem = onNovaViagem,
                    onNovoAbastecimento = onNovoAbastecimento,
                    onNovaDespesa = onNovaDespesa,
                    onAbrirViagemAtual = onAbrirViagemAtual,
                    )
                }
            }
        }
    }
}

@Composable
private fun DashboardLoading() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun DashboardContent(
    viagemAtual: Viagem?,
    resultadoAcumulado: Double,
    statusCounts: StatusCounts,
    temViagemEmAndamento: Boolean,
    onNovaViagem: () -> Unit,
    onNovoAbastecimento: (String?) -> Unit,
    onNovaDespesa: (String?) -> Unit,
    onAbrirViagemAtual: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            ResumoHeroCard(
                resultadoAcumulado = resultadoAcumulado,
                statusCounts = statusCounts,
            )
        }
        item {
            ViagemAtualCard(
                viagem = viagemAtual,
                onAbrirViagemAtual = onAbrirViagemAtual,
            )
        }
        item {
            AcoesRapidasRow(
                viagemAtual = viagemAtual,
                temViagemEmAndamento = temViagemEmAndamento,
                onNovaViagem = onNovaViagem,
                onNovoAbastecimento = onNovoAbastecimento,
                onNovaDespesa = onNovaDespesa,
            )
        }
    }
}

@Composable
private fun ResumoHeroCard(
    resultadoAcumulado: Double,
    statusCounts: StatusCounts,
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = AppCardShape) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Resultado acumulado",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = formatBrl(resultadoAcumulado),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = saldoResultadoColor(resultadoAcumulado),
            )
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                StatusChip(
                    modifier = Modifier.weight(1f),
                    count = statusCounts.emAndamento,
                    label = "Ativas",
                    accent = StatusEmAndamentoColor,
                )
                StatusChip(
                    modifier = Modifier.weight(1f),
                    count = statusCounts.finalizadas,
                    label = "Finalizadas",
                    accent = StatusFinalizadaColor,
                )
                StatusChip(
                    modifier = Modifier.weight(1f),
                    count = statusCounts.pagas,
                    label = "Pagas",
                    accent = StatusPagaColor,
                )
            }
        }
    }
}

@Composable
private fun StatusChip(
    modifier: Modifier = Modifier,
    count: Int,
    label: String,
    accent: Color,
) {
    Card(
        modifier = modifier,
        shape = AppCompactCardShape,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f),
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = accent,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun ViagemAtualCard(
    viagem: Viagem?,
    onAbrirViagemAtual: (String) -> Unit,
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = AppCardShape) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (viagem == null) {
                Text(
                    text = "Nenhuma viagem ativa",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Inicie uma nova viagem pelo atalho abaixo.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                ViagemStatusBanner(status = viagem.status)
                Spacer(modifier = Modifier.height(10.dp))
                val origem = formatRouteSegment(viagem.origem.ifBlank { "-" })
                val destino = formatRouteSegment(viagem.destino.ifBlank { "-" })
                Text(
                    text = "$origem → $destino",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = formatRouteSegment(viagem.cliente.ifBlank { "-" }),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = formatToneladas(viagem.numeroToneladas),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
                MetricRow(
                    items = listOf(
                        "Bruto" to formatBrl(viagem.valorBrutoEfetivo ?: viagem.valorBruto),
                        "Despesas" to formatBrl(viagem.totalDespesas),
                        "Abast." to formatBrl(viagem.totalAbastecimentos),
                    ),
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Column {
                        Text(
                            text = "Resultado desta viagem",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = formatBrl(viagem.saldoEmpresa),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = saldoResultadoColor(viagem.saldoEmpresa),
                        )
                    }
                    OutlinedButton(
                        onClick = { onAbrirViagemAtual(viagem.id) },
                        shape = AppButtonShape,
                    ) {
                        Text("Detalhes")
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricRow(items: List<Pair<String, String>>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        items.forEach { (label, value) ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun AcoesRapidasRow(
    viagemAtual: Viagem?,
    temViagemEmAndamento: Boolean,
    onNovaViagem: () -> Unit,
    onNovoAbastecimento: (String?) -> Unit,
    onNovaDespesa: (String?) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            QuickActionButton(
                modifier = Modifier.weight(1f),
                label = "Despesa",
                icon = { Icon(Icons.AutoMirrored.Outlined.ReceiptLong, contentDescription = null) },
                onClick = { onNovaDespesa(viagemAtual?.id) },
            )
            QuickActionButton(
                modifier = Modifier.weight(1f),
                label = "Abastec.",
                icon = { Icon(Icons.Outlined.LocalGasStation, contentDescription = null) },
                onClick = { onNovoAbastecimento(viagemAtual?.id) },
            )
            QuickActionButton(
                modifier = Modifier.weight(1f),
                label = "Viagem",
                icon = { Icon(Icons.Outlined.AddRoad, contentDescription = null) },
                enabled = !temViagemEmAndamento,
                primary = true,
                onClick = onNovaViagem,
            )
        }
        if (temViagemEmAndamento) {
            Text(
                text = "Finalize a viagem atual para iniciar outra.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun QuickActionButton(
    modifier: Modifier = Modifier,
    label: String,
    icon: @Composable () -> Unit,
    enabled: Boolean = true,
    primary: Boolean = false,
    onClick: () -> Unit,
) {
    if (primary) {
        Button(
            onClick = onClick,
            enabled = enabled,
            modifier = modifier.height(72.dp),
            shape = AppButtonShape,
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp),
        ) {
            QuickActionContent(label = label, icon = icon)
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            enabled = enabled,
            modifier = modifier.height(72.dp),
            shape = AppButtonShape,
        ) {
            QuickActionContent(label = label, icon = icon)
        }
    }
}

@Composable
private fun QuickActionContent(
    label: String,
    icon: @Composable () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        icon()
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1,
        )
    }
}
