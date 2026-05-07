package com.example.rodoflow.ui.viagens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun NovaViagemScreen(
    onNavigateBack: () -> Unit,
    viewModel: NovaViagemViewModel = viewModel(),
) {
    var origem by remember { mutableStateOf("") }
    var destino by remember { mutableStateOf("") }
    var valorBrutoText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OutlinedTextField(
            value = origem,
            onValueChange = { origem = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Origem") },
        )
        OutlinedTextField(
            value = destino,
            onValueChange = { destino = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Destino") },
        )
        OutlinedTextField(
            value = valorBrutoText,
            onValueChange = { valorBrutoText = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Valor bruto") },
        )
        Spacer(modifier = Modifier.height(8.dp))
        errorMessage?.let { msg ->
            Text(text = msg)
        }
        Button(
            onClick = {
                errorMessage = null
                val origemValue = origem.trim()
                val destinoValue = destino.trim()
                val valorBruto = valorBrutoText.replace(',', '.').toDoubleOrNull()
                if (origemValue.isEmpty() || destinoValue.isEmpty()) {
                    errorMessage = "Origem e destino são obrigatórios."
                    return@Button
                }
                if (valorBruto == null || !valorBruto.isFinite() || valorBruto <= 0.0) {
                    errorMessage = "Informe um valor bruto válido."
                    return@Button
                }
                viewModel.createViagem(
                    origem = origemValue,
                    destino = destinoValue,
                    valorBruto = valorBruto,
                    onSuccess = onNavigateBack,
                    onError = { message -> errorMessage = message },
                )
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Criar Viagem")
        }
    }
}
