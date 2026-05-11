package com.example.rodoflow.ui.home

import com.example.rodoflow.AppLog
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rodoflow.data.model.Viagem
import com.example.rodoflow.ui.components.LoadDataErrorPanel
import com.example.rodoflow.ui.components.StatusBadge
import com.example.rodoflow.ui.components.StatusEmAndamentoColor
import com.example.rodoflow.ui.components.StatusFinalizadaColor
import com.example.rodoflow.ui.components.StatusPagaColor
import com.example.rodoflow.ui.components.saldoResultadoColor
import com.example.rodoflow.ui.util.formatBrl
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
    val saldo by viewModel.saldo.collectAsStateWithLifecycle()
    val viagemAtual by viewModel.viagemAtual.collectAsStateWithLifecycle()
    val statusCounts by viewModel.statusCounts.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        AppLog.d("HOME_RELOAD", "LaunchedEffect(Unit) -> loadDashboard inicial")
        viewModel.loadDashboard()
    }
    LaunchedEffect(reloadNonce) {
        AppLog.d("HOME_RELOAD", "LaunchedEffect(reloadNonce=$reloadNonce) disparado")
        if (reloadNonce > 0) {
            AppLog.d("HOME_RELOAD", "reloadNonce>0 -> chamando loadDashboard")
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
            DashboardContent(
                viagemAtual = viagemAtual,
                saldoEmpresaTotal = saldo.firstOrNull()?.saldoPendente ?: 0.0,
                statusCounts = statusCounts,
                isRefreshing = loading,
                onNovaViagem = onNovaViagem,
                onNovoAbastecimento = onNovoAbastecimento,
                onNovaDespesa = onNovaDespesa,
                onAbrirViagemAtual = onAbrirViagemAtual,
            )
        }
    }
}

@Composable
private fun DashboardLoading() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "Carregando...")
        }
    }
}

@Composable
private fun DashboardContent(
    viagemAtual: Viagem?,
    saldoEmpresaTotal: Double,
    statusCounts: StatusCounts,
    isRefreshing: Boolean,
    onNovaViagem: () -> Unit,
    onNovoAbastecimento: (String?) -> Unit,
    onNovaDespesa: (String?) -> Unit,
    onAbrirViagemAtual: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            DashboardHeader(
                saldoEmpresaTotal = saldoEmpresaTotal,
                isRefreshing = isRefreshing,
            )
        }
        item {
            ViagemAtualCard(
                viagem = viagemAtual,
                onAbrirViagemAtual = onAbrirViagemAtual,
            )
        }
        if (viagemAtual != null) {
            item {
                FinanceiroCard(viagem = viagemAtual)
            }
        }
        item {
            IndicadoresRapidosRow(statusCounts = statusCounts)
        }
        item {
            AcoesRapidasSection(
                viagemAtual = viagemAtual,
                onNovaViagem = onNovaViagem,
                onNovoAbastecimento = onNovoAbastecimento,
                onNovaDespesa = onNovaDespesa,
            )
        }
    }
}

@Composable
private fun DashboardHeader(
    saldoEmpresaTotal: Double,
    isRefreshing: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            Text(
                text = "Painel do motorista",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "Operacional → custos → resultado",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "Saldo da empresa: ${formatBrl(saldoEmpresaTotal)}",
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
private fun ViagemAtualCard(
    viagem: Viagem?,
    onAbrirViagemAtual: (String) -> Unit,
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Operacional — viagem atual",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (viagem == null) {
                EmptyViagemAtual()
            } else {
                ViagemAtualBody(
                    viagem = viagem,
                    onAbrir = onAbrirViagemAtual,
                )
            }
        }
    }
}

@Composable
private fun EmptyViagemAtual() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Nenhuma viagem em andamento",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Inicie uma nova viagem pelo botão abaixo.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ViagemAtualBody(
    viagem: Viagem,
    onAbrir: (String) -> Unit,
) {
    Text(
        text = "${viagem.origem.ifBlank { "-" }} → ${viagem.destino.ifBlank { "-" }}",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.SemiBold,
    )
    Spacer(modifier = Modifier.height(10.dp))
    InfoLine(label = "Cliente", value = viagem.cliente.ifBlank { "-" })
    Spacer(modifier = Modifier.height(8.dp))
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "Status:",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        StatusBadge(status = viagem.status)
    }
    Spacer(modifier = Modifier.height(10.dp))
    val valorBruto = viagem.valorBrutoEfetivo ?: viagem.valorBruto
    Text(
        text = "Valor bruto",
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Text(
        text = formatBrl(valorBruto),
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
    )
    Spacer(modifier = Modifier.height(6.dp))
    InfoLine(
        label = "Toneladas",
        value = formatToneladas(viagem.numeroToneladas),
    )
    Spacer(modifier = Modifier.height(12.dp))
    OutlinedButton(
        onClick = { onAbrir(viagem.id) },
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
    ) {
        Text(text = "Abrir detalhes da viagem")
    }
}

@Composable
private fun FinanceiroCard(viagem: Viagem) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Custos e resultado",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(12.dp))
            InfoLine(label = "Total despesas", value = formatBrl(viagem.totalDespesas))
            Spacer(modifier = Modifier.height(6.dp))
            InfoLine(label = "Total abastecimentos", value = formatBrl(viagem.totalAbastecimentos))
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Saldo final",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = formatBrl(viagem.saldoEmpresa),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = saldoResultadoColor(viagem.saldoEmpresa),
            )
        }
    }
}

@Composable
private fun IndicadoresRapidosRow(statusCounts: StatusCounts) {
    Column {
        Text(
            text = "Indicadores rápidos",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            IndicadorCard(
                modifier = Modifier.weight(1f),
                label = "Em andamento",
                count = statusCounts.emAndamento,
                accent = StatusEmAndamentoColor,
            )
            IndicadorCard(
                modifier = Modifier.weight(1f),
                label = "Finalizadas",
                count = statusCounts.finalizadas,
                accent = StatusFinalizadaColor,
            )
            IndicadorCard(
                modifier = Modifier.weight(1f),
                label = "Pagas",
                count = statusCounts.pagas,
                accent = StatusPagaColor,
            )
        }
    }
}

@Composable
private fun IndicadorCard(
    modifier: Modifier = Modifier,
    label: String,
    count: Int,
    accent: Color,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = accent,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun AcoesRapidasSection(
    viagemAtual: Viagem?,
    onNovaViagem: () -> Unit,
    onNovoAbastecimento: (String?) -> Unit,
    onNovaDespesa: (String?) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Atalhos",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Button(
            onClick = { onNovaDespesa(viagemAtual?.id) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
            ),
        ) {
            Text(text = "Nova despesa", style = MaterialTheme.typography.titleMedium)
        }
        Button(
            onClick = { onNovoAbastecimento(viagemAtual?.id) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            ),
        ) {
            Text(text = "Novo abastecimento", style = MaterialTheme.typography.titleMedium)
        }
        Button(
            onClick = onNovaViagem,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
        ) {
            Text(text = "Nova viagem", style = MaterialTheme.typography.titleMedium)
        }
        if (viagemAtual == null) {
            Text(
                text = "Sem viagem em andamento. Despesas e abastecimentos serão registrados como avulsos no caixa geral.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun InfoLine(label: String, value: String) {
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

