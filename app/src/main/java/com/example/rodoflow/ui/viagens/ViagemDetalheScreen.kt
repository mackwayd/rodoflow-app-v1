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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.example.rodoflow.ui.components.LoadDataErrorPanel
import com.example.rodoflow.ui.components.LocalSnackbar
import com.example.rodoflow.ui.components.StatusBadge
import com.example.rodoflow.ui.util.formatBrl
import com.example.rodoflow.ui.util.formatCnpj
import com.example.rodoflow.ui.util.formatIsoDateTimeBr
import com.example.rodoflow.ui.util.formatKg
import com.example.rodoflow.ui.util.formatKm
import com.example.rodoflow.ui.util.formatToneladas

@Composable
fun ViagemDetalheScreen(
    viagemId: String,
    onNavigateNovaDespesa: (String) -> Unit = {},
    onNavigateNovoAbastecimento: (String) -> Unit = {},
    onFinanceiroChanged: () -> Unit = {},
    viewModel: ViagemDetalheViewModel = viewModel(),
) {
    val viagem by viewModel.viagem.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val notFound by viewModel.notFound.collectAsStateWithLifecycle()
    val isFinalizando by viewModel.isFinalizando.collectAsStateWithLifecycle()
    val actionError by viewModel.actionError.collectAsStateWithLifecycle()
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
                LoadDataErrorPanel(onRetry = { viewModel.loadViagem(viagemId) })
            }
        }
        viagem != null -> {
            val viagemAtual = viagem
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
            ) {
                StatusBadge(status = viagemAtual?.status.orEmpty())
                Spacer(modifier = Modifier.height(12.dp))

                if (viagemAtual?.status == "EM_ANDAMENTO") {
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
                            .height(52.dp),
                    ) {
                        Text("Finalizar viagem")
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                SectionCard(title = "Operacional") {
                    DetailLine(label = "Origem", value = viagemAtual?.origem.orEmpty())
                    DetailLine(label = "Destino", value = viagemAtual?.destino.orEmpty())
                    DetailLine(label = "Cliente", value = viagemAtual?.cliente.orEmpty())
                    DetailLine(label = "CNPJ", value = formatCnpj(viagemAtual?.cnpjCliente))
                    DetailLine(label = "Tipo de carga", value = viagemAtual?.tipoCarga.orEmpty())
                    DetailLine(
                        label = "Número toneladas",
                        value = formatToneladas(viagemAtual?.numeroToneladas ?: 0.0),
                    )
                    DetailLine(
                        label = "Valor tonelada",
                        value = formatBrl(viagemAtual?.valorTonelada),
                    )
                    DetailLine(
                        label = "KM inicial",
                        value = formatKm(viagemAtual?.kmInicial ?: 0.0),
                    )
                    DetailLine(label = "Status", value = viagemAtual?.status.orEmpty())
                    DetailLine(label = "Data início", value = formatIsoDateTimeBr(viagemAtual?.dataInicio))
                    DetailLine(label = "Data fim", value = formatIsoDateTimeBr(viagemAtual?.dataFim))
                }

                Spacer(modifier = Modifier.height(12.dp))
                SectionCard(title = "Financeiro") {
                    val percentualMotorista = (viagemAtual?.valorBruto ?: 0.0) * 0.12
                    DetailLine(
                        label = "Valor bruto",
                        value = formatBrl(viagemAtual?.valorBruto),
                    )
                    DetailLine(
                        label = "Total despesas",
                        value = formatBrl(viagemAtual?.totalDespesas),
                    )
                    DetailLine(
                        label = "Total abastecimentos",
                        value = formatBrl(viagemAtual?.totalAbastecimentos),
                    )
                    DetailLine(
                        label = "Saldo empresa",
                        value = formatBrl(viagemAtual?.saldoEmpresa),
                    )
                    DetailLine(
                        label = "Percentual motorista (12%)",
                        value = formatBrl(percentualMotorista),
                    )
                    val temQuebraInfo = viagemAtual?.kgPerdido != null ||
                        ((viagemAtual?.valorQuebra ?: 0.0) > 0.0)
                    if (temQuebraInfo) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Quebra",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                        DetailLine(
                            label = "KG perdido",
                            value = formatKg(viagemAtual?.kgPerdido),
                        )
                        DetailLine(
                            label = "Valor quebra",
                            value = formatBrl(viagemAtual?.valorQuebra),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                SectionCard(title = "Abastecimentos") {
                    if (viagemAtual?.abastecimentos.isNullOrEmpty()) {
                        Text(text = "Nenhum abastecimento registrado")
                    } else {
                        viagemAtual?.abastecimentos?.forEach { abastecimento ->
                            DetailLine(label = "Litros", value = "${abastecimento.litros}")
                            DetailLine(
                                label = "Valor total",
                                value = formatBrl(abastecimento.valorTotal),
                            )
                            DetailLine(label = "Data", value = formatIsoDateTimeBr(abastecimento.data))
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                    Button(
                        onClick = { onNavigateNovoAbastecimento(viagemId) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                    ) {
                        Text(text = "+ abastecimento")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                SectionCard(title = "Despesas") {
                    if (viagemAtual?.despesas.isNullOrEmpty()) {
                        Text(text = "Nenhuma despesa registrada")
                    } else {
                        viagemAtual?.despesas?.forEach { despesa ->
                            DetailLine(label = "Tipo", value = despesa.tipo)
                            DetailLine(
                                label = "Valor",
                                value = formatBrl(despesa.valor),
                            )
                            DetailLine(label = "Descrição", value = despesa.descricao.orEmpty())
                            DetailLine(label = "Data", value = formatIsoDateTimeBr(despesa.data))
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                    Button(
                        onClick = { onNavigateNovaDespesa(viagemId) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                    ) {
                        Text(text = "+ despesa")
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
                                    onSuccess = {
                                        showSnackbar("Viagem finalizada")
                                        onFinanceiroChanged()
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
                                    onSuccess = {
                                        showSnackbar("Viagem finalizada")
                                        onFinanceiroChanged()
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
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
        )
        Text(
            text = value.ifBlank { "-" },
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

