package com.example.rodoflow.ui.viagens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rodoflow.ui.util.formatIsoDateTimeBr
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ViagemDetalheScreen(
    viagemId: String,
    reloadTrigger: Long = 0L,
    onNavigateNovaDespesa: (String) -> Unit = {},
    onNavigateNovoAbastecimento: (String) -> Unit = {},
    viewModel: ViagemDetalheViewModel = viewModel(),
) {
    val viagem by viewModel.viagem.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val notFound by viewModel.notFound.collectAsStateWithLifecycle()
    val isFinalizando by viewModel.isFinalizando.collectAsStateWithLifecycle()
    val actionError by viewModel.actionError.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current

    var showFinalizarDialog by remember { mutableStateOf(false) }
    var houveQuebra by remember { mutableStateOf<Boolean?>(null) }
    var toneladasFinaisText by remember { mutableStateOf("") }
    var observacaoQuebra by remember { mutableStateOf("") }
    var finalizarError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(viagemId, reloadTrigger) {
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

    val moneyFormat = remember {
        NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR")).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        if (isLoading) {
            Text(text = "Carregando viagem...")
        } else if (notFound) {
            Text(text = "Viagem não encontrada")
        } else if (viagem != null) {
            val viagemAtual = viagem
            val statusColor = when (viagemAtual?.status.orEmpty()) {
                "EM_ANDAMENTO" -> Color(0xFF1565C0)
                "FINALIZADA" -> Color(0xFF2E7D32)
                "PAGA" -> Color(0xFF6A1B9A)
                else -> Color(0xFF616161)
            }

            StatusBadge(
                status = viagemAtual?.status.orEmpty(),
                backgroundColor = statusColor,
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (viagemAtual?.status == "EM_ANDAMENTO") {
                Button(
                    onClick = {
                        showFinalizarDialog = true
                        houveQuebra = null
                        toneladasFinaisText = ""
                        observacaoQuebra = ""
                        finalizarError = null
                        viewModel.clearActionError()
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Finalizar viagem")
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            SectionCard(title = "Operacional") {
                DetailLine(label = "Origem", value = viagemAtual?.origem.orEmpty())
                DetailLine(label = "Destino", value = viagemAtual?.destino.orEmpty())
                DetailLine(label = "Cliente", value = viagemAtual?.cliente.orEmpty())
                DetailLine(label = "CNPJ", value = viagemAtual?.cnpjCliente.orEmpty())
                DetailLine(label = "Tipo de carga", value = viagemAtual?.tipoCarga.orEmpty())
                DetailLine(label = "Número toneladas", value = "${viagemAtual?.numeroToneladas ?: 0.0}")
                DetailLine(
                    label = "Valor tonelada",
                    value = moneyFormat.format(viagemAtual?.valorTonelada ?: 0.0),
                )
                DetailLine(label = "KM inicial", value = "${viagemAtual?.kmInicial ?: 0.0}")
                DetailLine(label = "Status", value = viagemAtual?.status.orEmpty())
                DetailLine(label = "Data início", value = formatIsoDateTimeBr(viagemAtual?.dataInicio))
                DetailLine(label = "Data fim", value = formatIsoDateTimeBr(viagemAtual?.dataFim))
            }

            Spacer(modifier = Modifier.height(12.dp))
            SectionCard(title = "Financeiro") {
                val percentualMotorista = (viagemAtual?.valorBruto ?: 0.0) * 0.12
                DetailLine(
                    label = "Valor bruto",
                    value = moneyFormat.format(viagemAtual?.valorBruto ?: 0.0),
                )
                DetailLine(
                    label = "Total despesas",
                    value = moneyFormat.format(viagemAtual?.totalDespesas ?: 0.0),
                )
                DetailLine(
                    label = "Total abastecimentos",
                    value = moneyFormat.format(viagemAtual?.totalAbastecimentos ?: 0.0),
                )
                DetailLine(
                    label = "Saldo empresa",
                    value = moneyFormat.format(viagemAtual?.saldoEmpresa ?: 0.0),
                )
                DetailLine(
                    label = "Percentual motorista (12%)",
                    value = moneyFormat.format(percentualMotorista),
                )
                if (viagemAtual?.valorQuebra != null || viagemAtual?.valorBrutoEfetivo != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Impacto de quebra",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                    DetailLine(
                        label = "Valor quebra",
                        value = moneyFormat.format(viagemAtual?.valorQuebra ?: 0.0),
                    )
                    DetailLine(
                        label = "Valor bruto efetivo",
                        value = moneyFormat.format(viagemAtual?.valorBrutoEfetivo ?: 0.0),
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            SectionCard(title = "Abastecimentos") {
                if (viagemAtual?.abastecimentos.isNullOrEmpty()) {
                    Text(text = "Nenhum abastecimento registrado.")
                } else {
                    viagemAtual?.abastecimentos?.forEach { abastecimento ->
                        DetailLine(label = "Litros", value = "${abastecimento.litros}")
                        DetailLine(
                            label = "Valor total",
                            value = moneyFormat.format(abastecimento.valorTotal),
                        )
                        DetailLine(label = "Data", value = formatIsoDateTimeBr(abastecimento.data))
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
                Button(
                    onClick = { onNavigateNovoAbastecimento(viagemId) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(text = "+ abastecimento")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            SectionCard(title = "Despesas") {
                if (viagemAtual?.despesas.isNullOrEmpty()) {
                    Text(text = "Nenhuma despesa registrada.")
                } else {
                    viagemAtual?.despesas?.forEach { despesa ->
                        DetailLine(label = "Tipo", value = despesa.tipo)
                        DetailLine(
                            label = "Valor",
                            value = moneyFormat.format(despesa.valor),
                        )
                        DetailLine(label = "Descrição", value = despesa.descricao.orEmpty())
                        DetailLine(label = "Data", value = formatIsoDateTimeBr(despesa.data))
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
                Button(
                    onClick = { onNavigateNovaDespesa(viagemId) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(text = "+ despesa")
                }
            }
            actionError?.let {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        } else {
            Text(text = "Viagem não encontrada")
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
                Column(verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)) {
                    Text("Houve quebra?")
                    Row {
                        Button(
                            onClick = {
                                houveQuebra = true
                                finalizarError = null
                            },
                        ) {
                            Text("SIM")
                        }
                        Spacer(modifier = Modifier.height(0.dp).padding(horizontal = 4.dp))
                        Button(
                            onClick = {
                                houveQuebra = false
                                finalizarError = null
                            },
                        ) {
                            Text("NÃO")
                        }
                    }
                    if (houveQuebra == true) {
                        OutlinedTextField(
                            value = toneladasFinaisText,
                            onValueChange = { toneladasFinaisText = it },
                            label = { Text("Toneladas finais") },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        OutlinedTextField(
                            value = observacaoQuebra,
                            onValueChange = { observacaoQuebra = it },
                            label = { Text("Observação") },
                            modifier = Modifier.fillMaxWidth(),
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
                Button(
                    enabled = !isFinalizando && houveQuebra != null,
                    onClick = {
                        finalizarError = null
                        when (houveQuebra) {
                            false -> {
                                viewModel.finalizarViagem(
                                    viagemId = viagemId,
                                    teveQuebra = false,
                                )
                                showFinalizarDialog = false
                            }

                            true -> {
                                val toneladasFinais = toneladasFinaisText.replace(',', '.').toDoubleOrNull()
                                if (toneladasFinais == null || !toneladasFinais.isFinite()) {
                                    finalizarError = "Toneladas finais é obrigatório."
                                    return@Button
                                }
                                if (toneladasFinais < 0.0) {
                                    finalizarError = "Toneladas finais deve ser maior ou igual a 0."
                                    return@Button
                                }
                                if (toneladasFinais > (viagem?.numeroToneladas ?: 0.0)) {
                                    finalizarError = "Toneladas finais não pode ser maior que número toneladas da viagem."
                                    return@Button
                                }
                                viewModel.finalizarViagem(
                                    viagemId = viagemId,
                                    teveQuebra = true,
                                    toneladasFinais = toneladasFinais,
                                    observacaoQuebra = observacaoQuebra.trim().ifBlank { null },
                                )
                                showFinalizarDialog = false
                            }

                            null -> {
                                finalizarError = "Selecione se houve quebra."
                            }
                        }
                    },
                ) {
                    Text(if (isFinalizando) "Finalizando..." else "Confirmar")
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

@Composable
private fun StatusBadge(
    status: String,
    backgroundColor: Color,
) {
    Text(
        text = status.ifBlank { "SEM_STATUS" },
        color = Color.White,
        modifier = Modifier
            .background(backgroundColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        style = MaterialTheme.typography.labelLarge,
    )
}
