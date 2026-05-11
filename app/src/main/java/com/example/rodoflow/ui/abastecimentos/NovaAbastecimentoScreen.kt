package com.example.rodoflow.ui.abastecimentos

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rodoflow.data.model.Viagem
import com.example.rodoflow.ui.components.LocalSnackbar

private val InfoSurface = Color(0xFFE3F2FD)
private val InfoOnSurface = Color(0xFF0D47A1)

@Composable
fun NovaAbastecimentoScreen(
    preselectedViagemId: String? = null,
    onNavigateBack: () -> Unit,
    viewModel: NovaAbastecimentoViewModel = viewModel(),
) {
    val showSnackbar = LocalSnackbar.current
    val viagemAtual by viewModel.viagemAtual.collectAsStateWithLifecycle()
    val carregandoViagemAtual by viewModel.carregandoViagemAtual.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.carregarViagemAtual()
    }

    val viagemLink: Viagem? = viagemAtual
    val podeVincular = viagemLink != null || preselectedViagemId != null
    val targetViagemId: String? = preselectedViagemId ?: viagemLink?.id

    var vincularSelecionado by remember(podeVincular) { mutableStateOf(podeVincular) }
    var litros by remember { mutableStateOf("") }
    var valorLitro by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var saving by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(16.dp),
    ) {
        Text(
            text = "Novo abastecimento",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(modifier = Modifier.height(12.dp))

        VinculoSelector(
            podeVincular = podeVincular,
            carregando = carregandoViagemAtual,
            vincularSelecionado = vincularSelecionado,
            viagemLink = viagemLink,
            preselectedViagemId = preselectedViagemId,
            onSelecionarVincular = { vincularSelecionado = true },
            onSelecionarAvulso = { vincularSelecionado = false },
        )

        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = litros,
            onValueChange = { litros = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Litros") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Next,
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) },
            ),
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = valorLitro,
            onValueChange = { valorLitro = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Valor por litro") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() },
            ),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            enabled = !saving,
            onClick = {
                errorMessage = null
                val litrosDouble = litros.replace(',', '.').toDoubleOrNull()
                val valorLitroDouble = valorLitro.replace(',', '.').toDoubleOrNull()
                if (litrosDouble == null || !litrosDouble.isFinite() || litrosDouble <= 0.0) {
                    errorMessage = "Informe uma quantidade de litros válida."
                    return@Button
                }
                if (valorLitroDouble == null || !valorLitroDouble.isFinite() || valorLitroDouble <= 0.0) {
                    errorMessage = "Informe um valor por litro válido."
                    return@Button
                }
                focusManager.clearFocus()
                val viagemIdParaEnviar: String? = if (podeVincular && vincularSelecionado) {
                    targetViagemId
                } else {
                    null
                }
                saving = true
                viewModel.createAbastecimento(
                    litros = litrosDouble,
                    valorLitro = valorLitroDouble,
                    viagemId = viagemIdParaEnviar,
                    onSuccess = { vinculado ->
                        saving = false
                        val msg = if (vinculado) {
                            "Abastecimento vinculado à viagem"
                        } else {
                            "Abastecimento avulso registrado"
                        }
                        showSnackbar(msg)
                        onNavigateBack()
                    },
                    onError = { message ->
                        saving = false
                        showSnackbar(message)
                    },
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (saving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(text = if (saving) "Salvando..." else "Salvar abastecimento")
            }
        }
        errorMessage?.let { msg ->
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = msg,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

@Composable
private fun VinculoSelector(
    podeVincular: Boolean,
    carregando: Boolean,
    vincularSelecionado: Boolean,
    viagemLink: Viagem?,
    preselectedViagemId: String?,
    onSelecionarVincular: () -> Unit,
    onSelecionarAvulso: () -> Unit,
) {
    when {
        carregando && !podeVincular -> {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                )
                Text(
                    text = "Carregando...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        podeVincular -> {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Vínculo com viagem",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    val labelVincular = viagemLink
                        ?.let { "Vincular à viagem ${it.origem} → ${it.destino}" }
                        ?: preselectedViagemId
                            ?.takeIf { it.isNotBlank() }
                            ?.let { "Vincular à viagem (${it.take(8)}…)" }
                        ?: "Vincular à viagem atual"
                    OptionRow(
                        selected = vincularSelecionado,
                        label = labelVincular,
                        onClick = onSelecionarVincular,
                    )
                    OptionRow(
                        selected = !vincularSelecionado,
                        label = "Registrar como avulso",
                        onClick = onSelecionarAvulso,
                    )
                }
            }
        }
        else -> {
            InfoBanner(
                text = "Sem viagem em andamento. Este gasto será registrado no caixa geral.",
            )
        }
    }
}

@Composable
private fun OptionRow(
    selected: Boolean,
    label: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun InfoBanner(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(InfoSurface, RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = InfoOnSurface,
        )
    }
}
