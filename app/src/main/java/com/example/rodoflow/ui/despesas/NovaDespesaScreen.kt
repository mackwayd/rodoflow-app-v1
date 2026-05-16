package com.example.rodoflow.ui.despesas

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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
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
import com.example.rodoflow.ui.util.formatRouteSegment
import com.example.rodoflow.ui.util.humanizeTipoDespesa
import com.example.rodoflow.data.model.Viagem
import com.example.rodoflow.ui.components.LocalSnackbar
import com.example.rodoflow.ui.theme.AppBannerShape
import com.example.rodoflow.ui.theme.AppButtonShape
import com.example.rodoflow.ui.theme.AppCardShape

private val InfoSurface = Color(0xFFD9EFFF)
private val InfoOnSurface = Color(0xFF004B7A)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NovaDespesaScreen(
    preselectedViagemId: String? = null,
    onNavigateBack: () -> Unit,
    viewModel: NovaDespesaViewModel = viewModel(),
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
    val tiposDespesa = listOf("PEDAGIO", "MANUTENCAO", "ALIMENTACAO", "OUTROS")
    var descricao by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf("OUTROS") }
    var tipoExpanded by remember { mutableStateOf(false) }
    var valor by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var saving by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(horizontal = 20.dp, vertical = 18.dp),
    ) {
        Text(
            text = "Nova despesa",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = "Registre um gasto vinculado à viagem atual ou como avulso.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

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
            value = descricao,
            onValueChange = { descricao = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Descrição") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next,
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) },
            ),
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = valor,
            onValueChange = { valor = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Valor") },
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
        ExposedDropdownMenuBox(
            expanded = tipoExpanded,
            onExpandedChange = { tipoExpanded = !tipoExpanded },
        ) {
            OutlinedTextField(
                value = humanizeTipoDespesa(tipo),
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .menuAnchor(
                        type = MenuAnchorType.PrimaryNotEditable,
                        enabled = true,
                    )
                    .fillMaxWidth(),
                label = { Text("Tipo") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = tipoExpanded)
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() },
                ),
            )
            DropdownMenu(
                expanded = tipoExpanded,
                onDismissRequest = { tipoExpanded = false },
            ) {
                tiposDespesa.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(humanizeTipoDespesa(option)) },
                        onClick = {
                            tipo = option
                            tipoExpanded = false
                        },
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            enabled = !saving,
            onClick = {
                errorMessage = null
                val descricaoValue = descricao.trim()
                val tipoValue = tipo.trim()
                val valorDouble = valor.replace(',', '.').toDoubleOrNull()
                if (tipoValue.isEmpty()) {
                    errorMessage = "Tipo é obrigatório."
                    return@Button
                }
                if (valorDouble == null || !valorDouble.isFinite() || valorDouble <= 0.0) {
                    errorMessage = "Informe um valor válido."
                    return@Button
                }
                focusManager.clearFocus()
                val viagemIdParaEnviar: String? = if (podeVincular && vincularSelecionado) {
                    targetViagemId
                } else {
                    null
                }
                saving = true
                viewModel.createDespesa(
                    descricao = descricaoValue,
                    valor = valorDouble,
                    tipo = tipoValue,
                    viagemId = viagemIdParaEnviar,
                    onSuccess = { vinculada ->
                        saving = false
                        val msg = if (vinculada) {
                            "Despesa vinculada à viagem"
                        } else {
                            "Despesa registrada no caixa geral"
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
                .height(54.dp),
            shape = AppButtonShape,
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
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
                Text(text = if (saving) "Salvando..." else "Salvar despesa")
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
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = AppCardShape,
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Vínculo com viagem",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    val labelVincular = viagemLink
                        ?.let {
                            val o = formatRouteSegment(it.origem.ifBlank { "-" })
                            val d = formatRouteSegment(it.destino.ifBlank { "-" })
                            "Vincular à viagem $o → $d"
                        }
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
            .background(InfoSurface, AppBannerShape)
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
