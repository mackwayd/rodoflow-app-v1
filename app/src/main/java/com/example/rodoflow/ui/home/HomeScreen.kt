package com.example.rodoflow.ui.home

import com.example.rodoflow.AppLog
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddRoad
import androidx.compose.material.icons.outlined.LocalGasStation
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
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
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
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
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Painel do motorista",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Operacional · custos · resultado",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "Saldo da empresa ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = formatBrl(saldoEmpresaTotal),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
        if (isRefreshing) {
            CircularProgressIndicator(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .size(22.dp),
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
        shape = AppCardShape,
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Operacional — viagem atual",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(10.dp))

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
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Nenhuma viagem em andamento",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Inicie uma nova viagem pelo atalho abaixo.",
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
    val origem = formatRouteSegment(viagem.origem.ifBlank { "-" })
    val destino = formatRouteSegment(viagem.destino.ifBlank { "-" })
    Text(
        text = "$origem → $destino",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.SemiBold,
    )
    Spacer(modifier = Modifier.height(12.dp))
    InfoLine(label = "Cliente", value = formatRouteSegment(viagem.cliente.ifBlank { "-" }))
    Spacer(modifier = Modifier.height(10.dp))
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = "Status",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        StatusBadge(status = viagem.status)
    }
    Spacer(modifier = Modifier.height(12.dp))
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    Spacer(modifier = Modifier.height(12.dp))
    val valorBruto = viagem.valorBrutoEfetivo ?: viagem.valorBruto
    Text(
        text = "Valor bruto",
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Text(
        text = formatBrl(valorBruto),
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
    )
    Spacer(modifier = Modifier.height(8.dp))
    InfoLine(
        label = "Toneladas",
        value = formatToneladas(viagem.numeroToneladas),
    )
    Spacer(modifier = Modifier.height(14.dp))
    OutlinedButton(
        onClick = { onAbrir(viagem.id) },
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = AppButtonShape,
    ) {
        Text(text = "Abrir detalhes da viagem")
    }
}

@Composable
private fun FinanceiroCard(viagem: Viagem) {
    ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = AppCardShape) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Custos e resultado",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(14.dp))
            InfoLine(label = "Total despesas", value = formatBrl(viagem.totalDespesas))
            Spacer(modifier = Modifier.height(8.dp))
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
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
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
        shape = AppCompactCardShape,
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.55f),
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .padding(bottom = 6.dp)
                    .width(40.dp)
                    .height(3.dp)
                    .background(accent, RoundedCornerShape(2.dp)),
            )
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = accent,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium,
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
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Atalhos",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        OutlinedButton(
            onClick = { onNovaDespesa(viagemAtual?.id) },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = AppButtonShape,
        ) {
            Icon(
                Icons.Outlined.ReceiptLong,
                contentDescription = null,
                modifier = Modifier.size(22.dp),
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(text = "Nova despesa", style = MaterialTheme.typography.titleSmall)
        }
        OutlinedButton(
            onClick = { onNovoAbastecimento(viagemAtual?.id) },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = AppButtonShape,
        ) {
            Icon(
                Icons.Outlined.LocalGasStation,
                contentDescription = null,
                modifier = Modifier.size(22.dp),
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(text = "Novo abastecimento", style = MaterialTheme.typography.titleSmall)
        }
        Button(
            onClick = onNovaViagem,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = AppButtonShape,
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
        ) {
            Icon(
                Icons.Outlined.AddRoad,
                contentDescription = null,
                modifier = Modifier.size(22.dp),
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(text = "Nova viagem", style = MaterialTheme.typography.titleSmall)
        }
        if (viagemAtual == null) {
            Text(
                text = "Sem viagem em andamento. Despesas e abastecimentos serão registrados como avulsos no caixa geral.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
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
