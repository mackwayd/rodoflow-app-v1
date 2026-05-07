package com.example.rodoflow.ui.despesas

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NovaDespesaScreen(
    viagemId: String,
    onNavigateBack: () -> Unit,
    viewModel: NovaDespesaViewModel = viewModel(),
) {
    val tiposDespesa = listOf("PEDAGIO", "MANUTENCAO", "ALIMENTACAO", "OUTROS")
    var descricao by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf("OUTROS") }
    var tipoExpanded by remember { mutableStateOf(false) }
    var valor by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Text(text = "Nova despesa")
        Text(text = "Viagem: $viagemId")
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = descricao,
            onValueChange = { descricao = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Descrição") },
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = valor,
            onValueChange = { valor = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Valor") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        )
        Spacer(modifier = Modifier.height(12.dp))
        ExposedDropdownMenuBox(
            expanded = tipoExpanded,
            onExpandedChange = { tipoExpanded = !tipoExpanded },
        ) {
            OutlinedTextField(
                value = tipo,
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
            )
            ExposedDropdownMenu(
                expanded = tipoExpanded,
                onDismissRequest = { tipoExpanded = false },
            ) {
                tiposDespesa.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
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
                viewModel.createDespesa(
                    descricao = descricaoValue,
                    valor = valorDouble,
                    tipo = tipoValue,
                    onSuccess = onNavigateBack,
                    onError = { message -> errorMessage = message },
                )
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = "Salvar Despesa")
        }
        errorMessage?.let { msg ->
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = msg)
        }
    }
}
