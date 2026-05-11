package com.example.rodoflow.ui.viagens

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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rodoflow.ui.components.LocalSnackbar
import com.example.rodoflow.ui.util.formatBrl

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NovaViagemScreen(
    onNavigateBack: () -> Unit,
    viewModel: NovaViagemViewModel = viewModel(),
) {
    val showSnackbar = LocalSnackbar.current
    var origem by remember { mutableStateOf("") }
    var destino by remember { mutableStateOf("") }
    var numeroToneladasText by remember { mutableStateOf("") }
    var valorToneladaText by remember { mutableStateOf("") }
    var cliente by remember { mutableStateOf("") }
    var cnpjCliente by remember { mutableStateOf("") }
    var tipoCarga by remember { mutableStateOf("SOJA") }
    var kmInicialText by remember { mutableStateOf("") }
    var tipoCargaExpanded by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var saving by remember { mutableStateOf(false) }

    val numeroToneladas = numeroToneladasText.replace(',', '.').toDoubleOrNull()
    val valorTonelada = valorToneladaText.replace(',', '.').toDoubleOrNull()
    val valorBrutoEstimado = (numeroToneladas ?: 0.0) * (valorTonelada ?: 0.0)

    val tiposCarga = listOf("SOJA", "MILHO", "FERTILIZANTE", "RAÇÃO", "OUTROS")
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Nova viagem",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
        )
        OutlinedTextField(
            value = origem,
            onValueChange = { origem = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Origem") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next,
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) },
            ),
        )
        OutlinedTextField(
            value = destino,
            onValueChange = { destino = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Destino") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next,
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) },
            ),
        )
        OutlinedTextField(
            value = numeroToneladasText,
            onValueChange = { numeroToneladasText = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Número toneladas") },
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
            value = valorToneladaText,
            onValueChange = { valorToneladaText = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Valor tonelada") },
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
            value = cliente,
            onValueChange = { cliente = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Cliente") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next,
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) },
            ),
        )
        OutlinedTextField(
            value = cnpjCliente,
            onValueChange = { cnpjCliente = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("CNPJ") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next,
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) },
            ),
        )
        ExposedDropdownMenuBox(
            expanded = tipoCargaExpanded,
            onExpandedChange = { tipoCargaExpanded = !tipoCargaExpanded },
        ) {
            OutlinedTextField(
                value = tipoCarga,
                onValueChange = { },
                readOnly = true,
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                label = { Text("Tipo carga") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = tipoCargaExpanded) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) },
                ),
            )
            DropdownMenu(
                expanded = tipoCargaExpanded,
                onDismissRequest = { tipoCargaExpanded = false },
            ) {
                tiposCarga.forEach { tipo ->
                    DropdownMenuItem(
                        text = { Text(tipo) },
                        onClick = {
                            tipoCarga = tipo
                            tipoCargaExpanded = false
                        },
                    )
                }
            }
        }
        OutlinedTextField(
            value = kmInicialText,
            onValueChange = { kmInicialText = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("KM inicial") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() },
            ),
        )
        Text(text = "Valor bruto estimado: ${formatBrl(valorBrutoEstimado)}")
        Spacer(modifier = Modifier.height(8.dp))
        errorMessage?.let { msg ->
            Text(text = msg)
        }
        Button(
            enabled = !saving,
            onClick = {
                errorMessage = null
                val origemValue = origem.trim()
                val destinoValue = destino.trim()
                val clienteValue = cliente.trim()
                val cnpjClienteValue = cnpjCliente.trim()
                val kmInicial = kmInicialText.replace(',', '.').toDoubleOrNull()

                if (origemValue.isEmpty() || destinoValue.isEmpty()) {
                    errorMessage = "Origem e destino são obrigatórios."
                    return@Button
                }
                if (numeroToneladas == null || !numeroToneladas.isFinite() || numeroToneladas <= 0.0) {
                    errorMessage = "Número de toneladas deve ser maior que 0."
                    return@Button
                }
                if (valorTonelada == null || !valorTonelada.isFinite() || valorTonelada <= 0.0) {
                    errorMessage = "Valor por tonelada deve ser maior que 0."
                    return@Button
                }
                if (kmInicial == null || !kmInicial.isFinite() || kmInicial < 0.0) {
                    errorMessage = "KM inicial deve ser maior ou igual a 0."
                    return@Button
                }
                if (clienteValue.isEmpty()) {
                    errorMessage = "Cliente é obrigatório."
                    return@Button
                }
                focusManager.clearFocus()
                saving = true
                viewModel.createViagem(
                    origem = origemValue,
                    destino = destinoValue,
                    numeroToneladas = numeroToneladas,
                    valorTonelada = valorTonelada,
                    cliente = clienteValue,
                    cnpjCliente = cnpjClienteValue,
                    tipoCarga = tipoCarga,
                    kmInicial = kmInicial,
                    onSuccess = {
                        saving = false
                        showSnackbar("Viagem criada")
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
                Text(if (saving) "Salvando..." else "Criar viagem")
            }
        }
    }
}
