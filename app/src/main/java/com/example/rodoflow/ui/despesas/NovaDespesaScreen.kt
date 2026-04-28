package com.example.rodoflow.ui.despesas

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
fun NovaDespesaScreen(
    viagemId: String,
    onNavigateBack: () -> Unit,
    viewModel: NovaDespesaViewModel = viewModel(),
) {
    var descricao by remember { mutableStateOf("") }
    var valor by remember { mutableStateOf("") }

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
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                val valorDouble = valor.replace(',', '.').toDoubleOrNull() ?: 0.0
                viewModel.createDespesa(
                    viagemId = viagemId,
                    descricao = descricao,
                    valor = valorDouble,
                    onSuccess = onNavigateBack,
                )
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = "Salvar Despesa")
        }
    }
}
