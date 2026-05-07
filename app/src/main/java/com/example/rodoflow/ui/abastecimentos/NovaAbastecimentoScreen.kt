package com.example.rodoflow.ui.abastecimentos

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
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

@Composable
fun NovaAbastecimentoScreen(
    viagemId: String,
    onNavigateBack: () -> Unit,
    viewModel: NovaAbastecimentoViewModel = viewModel(),
) {
    var litros by remember { mutableStateOf("") }
    var valorTotal by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Text(text = "Novo abastecimento")
        Text(text = "Viagem: $viagemId")
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = litros,
            onValueChange = { litros = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Litros") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = valorTotal,
            onValueChange = { valorTotal = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Valor total") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                errorMessage = null
                val litrosDouble = litros.replace(',', '.').toDoubleOrNull()
                val valorTotalDouble = valorTotal.replace(',', '.').toDoubleOrNull()
                if (litrosDouble == null || !litrosDouble.isFinite() || litrosDouble <= 0.0) {
                    errorMessage = "Informe uma quantidade de litros válida."
                    return@Button
                }
                if (valorTotalDouble == null || !valorTotalDouble.isFinite() || valorTotalDouble <= 0.0) {
                    errorMessage = "Informe um valor total válido."
                    return@Button
                }
                viewModel.createAbastecimento(
                    litros = litrosDouble,
                    valorTotal = valorTotalDouble,
                    onSuccess = onNavigateBack,
                    onError = { message -> errorMessage = message },
                )
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = "Salvar Abastecimento")
        }
        errorMessage?.let { msg ->
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = msg)
        }
    }
}
