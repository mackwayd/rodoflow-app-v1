package com.example.rodoflow.ui.viagens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocalGasStation
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rodoflow.ui.theme.AppButtonShape
import com.example.rodoflow.ui.theme.AppCardShape
import com.example.rodoflow.ui.components.AppTopBar
import com.example.rodoflow.ui.components.CachedDataBanner
import com.example.rodoflow.ui.components.ComprovanteImage
import com.example.rodoflow.ui.components.PendingFinalizeBanner
import com.example.rodoflow.ui.components.LocalSnackbar
import com.example.rodoflow.ui.util.MSG_OFFLINE_NO_DATA
import com.example.rodoflow.ui.util.operationSuccessMessage
import com.example.rodoflow.ui.components.StatusBadge
import com.example.rodoflow.ui.components.ViagemStatusBanner
import com.example.rodoflow.ui.util.formatBrl
import com.example.rodoflow.ui.util.formatCnpj
import com.example.rodoflow.ui.util.formatIsoDateTimeBr
import com.example.rodoflow.ui.util.formatKg
import com.example.rodoflow.ui.util.formatKm
import com.example.rodoflow.ui.util.formatRouteSegment
import com.example.rodoflow.ui.util.formatToneladas
import com.example.rodoflow.ui.util.humanizeTipoCarga
import com.example.rodoflow.ui.util.humanizeTipoDespesa

@Composable
fun ViagemDetalheScreen(
    viagemId: String,
    onNavigateBack: () -> Unit = {},
    onNavigateNovaDespesa: (String) -> Unit = {},
    onNavigateNovoAbastecimento: (String) -> Unit = {},
    onFinanceiroChanged: () -> Unit = {},
    viewModel: ViagemDetalheViewModel = viewModel(),
) {
    val viagem by viewModel.viagem.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val notFound by viewModel.notFound.collectAsStateWithLifecycle()
    val offlineNoData by viewModel.offlineNoData.collectAsStateWithLifecycle()
    val fromCache by viewModel.fromCache.collectAsStateWithLifecycle()
    val isFinalizando by viewModel.isFinalizando.collectAsStateWithLifecycle()
    val actionError by viewModel.actionError.collectAsStateWithLifecycle()
    val pendingFinalize by viewModel.pendingFinalize.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    val showSnackbar = LocalSnackbar.current

    var showFinalizarDialog by remember { mutableStateOf(false) }
    var houveQuebra by remember { mutableStateOf<Boolean?>(null) }
    var kgPerdidoText by remember { mutableStateOf("") }
    var valorQuebraText by remember { mutableStateOf("") }
    var observacaoQuebra by remember { mutableStateOf("") }
    var finalizarError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(viagemId) {
        viewModel.loadViagem(viagemId)
    }

    DisposableEffect(lifecycleOwner, viagemId) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadViagem(viagemId)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(actionError) {
        val msg = actionError
        if (!msg.isNullOrBlank()) {
            showSnackbar(msg)
            viewModel.clearActionError()
        }
    }

    when {
        isLoading && viagem == null -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "Carregando...")
                }
            }
        }
        notFound && viagem == null -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp),
                ) {
                    Text(
                        text = "Viagem não encontrada",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Esta viagem não existe ou foi removida.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.loadViagem(viagemId) }) {
                        Text("Tentar novamente")
                    }
                }
            }
        }
        offlineNoData && viagem == null -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp),
                ) {
                    Text(
                        text = "Sem conexão",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = MSG_OFFLINE_NO_DATA,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.loadViagem(viagemId) }) {
                        Text("Tentar novamente")
                    }
                }
            }
        }
        viagem != null -> {
            val trip = viagem!!
            val emAndamento = trip.status == "EM_ANDAMENTO" && !pendingFinalize
            val valorMotorista = if (trip.valorMotorista > 0.0) {
                trip.valorMotorista
            } else {
                (trip.valorBrutoEfetivo ?: trip.valorBruto) * 0.12
            }
            Scaffold(
                topBar = {
                    AppTopBar(
                        title = "Detalhe da viagem",
                        onNavigateBack = onNavigateBack,
                    )
                },
                contentWindowInsets = ScaffoldDefaults.contentWindowInsets,
            ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text(
                    text = "${formatRouteSegment(trip.origem.ifBlank { "-" })} → ${formatRouteSegment(trip.destino.ifBlank { "-" })}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                ViagemStatusBanner(status = trip.status)
                CachedDataBanner(
                    isShowingCachedData = fromCache,
                    refreshFailedWithData = false,
                )
                if (pendingFinalize) {
                    PendingFinalizeBanner()
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = "Status",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    StatusBadge(status = trip.status)
                }

                if (emAndamento) {
                    Button(
                        onClick = {
                            showFinalizarDialog = true
                            houveQuebra = null
                            kgPerdidoText = ""
                            valorQuebraText = ""
                            observacaoQuebra = ""
                            finalizarError = null
                            viewModel.clearActionError()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = AppButtonShape,
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                    ) {
                        Text("Finalizar viagem")
                    }
                }

                SectionCard(title = "Operacional") {
                    DetailLine(label = "Origem", value = formatRouteSegment(trip.origem.ifBlank { "-" }))
                    DetailLine(label = "Destino", value = formatRouteSegment(trip.destino.ifBlank { "-" }))
                    DetailLine(label = "Cliente", value = formatRouteSegment(trip.cliente.ifBlank { "-" }))
                    DetailLine(label = "CNPJ", value = formatCnpj(trip.cnpjCliente))
                    DetailLine(label = "Tipo de carga", value = humanizeTipoCarga(trip.tipoCarga))
                    DetailLine(
                        label = "Número toneladas",
                        value = formatToneladas(trip.numeroToneladas),
                    )
                    DetailLine(
                        label = "Valor tonelada",
                        value = formatBrl(trip.valorTonelada),
                    )
                    DetailLine(
                        label = "KM inicial",
                        value = formatKm(trip.kmInicial),
                    )
                    DetailLine(label = "Data início", value = formatIsoDateTimeBr(trip.dataInicio))
                    DetailLine(label = "Data fim", value = formatIsoDateTimeBr(trip.dataFim))
                }

                SectionCard(title = "Financeiro") {
                    DetailLine(
                        label = "Valor bruto",
                        value = formatBrl(trip.valorBruto),
                    )
                    DetailLine(
                        label = "Total despesas",
                        value = formatBrl(trip.totalDespesas),
                    )
                    DetailLine(
                        label = "Total abastecimentos",
                        value = formatBrl(trip.totalAbastecimentos),
                    )
                    DetailLine(
                        label = "Resultado desta viagem",
                        value = formatBrl(trip.saldoEmpresa),
                    )
                    DetailLine(
                        label = "Seu percentual (12%)",
                        value = formatBrl(valorMotorista),
                    )
                    val temQuebraInfo = trip.kgPerdido != null ||
                        ((trip.valorQuebra ?: 0.0) > 0.0)
                    if (temQuebraInfo) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Quebra",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                        DetailLine(
                            label = "KG perdido",
                            value = formatKg(trip.kgPerdido),
                        )
                        DetailLine(
                            label = "Valor quebra",
                            value = formatBrl(trip.valorQuebra),
                        )
                    }
                }

                SectionCard(title = "Abastecimentos") {
                    if (trip.abastecimentos.isNullOrEmpty()) {
                        Text(
                            text = "Nenhum abastecimento registrado",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        trip.abastecimentos.forEach { abastecimento ->
                            DetailLine(label = "Litros", value = "${abastecimento.litros}")
                            DetailLine(
                                label = "Valor total",
                                value = formatBrl(abastecimento.valorTotal),
                            )
                            DetailLine(label = "Data", value = formatIsoDateTimeBr(abastecimento.data))
                            ComprovanteImage(
                                relativeUrl = abastecimento.comprovanteUrl,
                                contentDescription = "Comprovante do abastecimento",
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
                            )
                        }
                    }
                    OutlinedButton(
                        onClick = { onNavigateNovoAbastecimento(viagemId) },
                        enabled = emAndamento,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = AppButtonShape,
                    ) {
                        Icon(Icons.Outlined.LocalGasStation, contentDescription = null)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(text = "Registrar abastecimento")
                    }
                }

                SectionCard(title = "Despesas") {
                    if (trip.despesas.isNullOrEmpty()) {
                        Text(
                            text = "Nenhuma despesa registrada",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        trip.despesas.forEach { despesa ->
                            DetailLine(label = "Tipo", value = humanizeTipoDespesa(despesa.tipo))
                            DetailLine(
                                label = "Valor",
                                value = formatBrl(despesa.valor),
                            )
                            DetailLine(label = "Descrição", value = despesa.descricao.orEmpty())
                            DetailLine(label = "Data", value = formatIsoDateTimeBr(despesa.data))
                            ComprovanteImage(
                                relativeUrl = despesa.comprovanteUrl,
                                contentDescription = "Comprovante da despesa",
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
                            )
                        }
                    }
                    OutlinedButton(
                        onClick = { onNavigateNovaDespesa(viagemId) },
                        enabled = emAndamento,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = AppButtonShape,
                    ) {
                        Icon(Icons.Outlined.ReceiptLong, contentDescription = null)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(text = "Registrar despesa")
                    }
                }
            }
            }
        }
        else -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "Carregando...")
                }
            }
        }
    }

    if (showFinalizarDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!isFinalizando) {
                    showFinalizarDialog = false
                }
            },
            title = { Text("Finalizar viagem") },
            text = {
                val focusManager = LocalFocusManager.current
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .imePadding(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text("Houve quebra?")
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Button(
                            onClick = {
                                houveQuebra = true
                                finalizarError = null
                                viewModel.clearActionError()
                            },
                        ) {
                            Text("Sim")
                        }
                        Button(
                            onClick = {
                                houveQuebra = false
                                finalizarError = null
                                viewModel.clearActionError()
                            },
                        ) {
                            Text("Não")
                        }
                    }
                    if (houveQuebra == true) {
                        OutlinedTextField(
                            value = kgPerdidoText,
                            onValueChange = { kgPerdidoText = it },
                            label = { Text("KG perdido") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Decimal,
                                imeAction = ImeAction.Next,
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) },
                            ),
                        )
                        OutlinedTextField(
                            value = valorQuebraText,
                            onValueChange = { valorQuebraText = it },
                            label = { Text("Valor da quebra") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Decimal,
                                imeAction = ImeAction.Next,
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) },
                            ),
                        )
                        OutlinedTextField(
                            value = observacaoQuebra,
                            onValueChange = { observacaoQuebra = it },
                            label = { Text("Observação") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Done,
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = { focusManager.clearFocus() },
                            ),
                        )
                    }
                    finalizarError?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            },
            confirmButton = {
                val focusManager = LocalFocusManager.current
                Button(
                    enabled = !isFinalizando && houveQuebra != null,
                    onClick = {
                        finalizarError = null
                        viewModel.clearActionError()
                        when (houveQuebra) {
                            false -> {
                                focusManager.clearFocus()
                                viewModel.finalizarViagem(
                                    viagemId = viagemId,
                                    teveQuebra = false,
                                    onSuccess = { queued ->
                                        showSnackbar(operationSuccessMessage("Viagem finalizada", queued))
                                        if (!queued) onFinanceiroChanged()
                                        showFinalizarDialog = false
                                    },
                                )
                            }

                            true -> {
                                val kgPerdido = kgPerdidoText.replace(',', '.').toDoubleOrNull()
                                val valorQuebra = valorQuebraText.replace(',', '.').toDoubleOrNull()
                                if (kgPerdido == null || !kgPerdido.isFinite() || kgPerdido < 0.0) {
                                    finalizarError = "Informe um KG perdido válido."
                                    return@Button
                                }
                                if (valorQuebra == null || !valorQuebra.isFinite() || valorQuebra < 0.0) {
                                    finalizarError = "Informe um valor da quebra válido."
                                    return@Button
                                }
                                focusManager.clearFocus()
                                viewModel.finalizarViagem(
                                    viagemId = viagemId,
                                    teveQuebra = true,
                                    kgPerdido = kgPerdido,
                                    valorQuebra = valorQuebra,
                                    observacaoQuebra = observacaoQuebra.trim().ifBlank { null },
                                    onSuccess = { queued ->
                                        showSnackbar(operationSuccessMessage("Viagem finalizada", queued))
                                        if (!queued) onFinanceiroChanged()
                                        showFinalizarDialog = false
                                    },
                                )
                            }

                            null -> {
                                finalizarError = "Selecione se houve quebra."
                            }
                        }
                    },
                    modifier = Modifier.height(52.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        if (isFinalizando) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary,
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(if (isFinalizando) "Salvando..." else "Confirmar")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    enabled = !isFinalizando,
                    onClick = { showFinalizarDialog = false },
                ) {
                    Text("Cancelar")
                }
            },
        )
    }
}

@Composable
private fun SectionCard(
    title: String,
    content: @Composable () -> Unit,
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = AppCardShape,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
private fun DetailLine(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
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

