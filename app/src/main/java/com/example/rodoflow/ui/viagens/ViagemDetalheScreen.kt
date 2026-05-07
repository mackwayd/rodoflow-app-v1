package com.example.rodoflow.ui.viagens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ViagemDetalheScreen(
    viagemId: String,
    onNavigateNovaDespesa: (String) -> Unit = {},
    onNavigateNovoAbastecimento: (String) -> Unit = {},
    viewModel: ViagemDetalheViewModel = viewModel(),
) {
    val viagem by viewModel.viagem.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val notFound by viewModel.notFound.collectAsStateWithLifecycle()

    LaunchedEffect(viagemId) {
        viewModel.loadViagem(viagemId)
    }

    val moneyFormat = remember {
        NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR")).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }
    }

    val isAdmin = false

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        if (isLoading) {
            Text(text = "Carregando viagem...")
        } else if (notFound) {
            Text(text = "Viagem não encontrada")
        } else if (viagem != null) {
            val viagemAtual = viagem
            Text(text = "Origem: ${viagemAtual?.origem.orEmpty()}")
            Text(text = "Destino: ${viagemAtual?.destino.orEmpty()}")
            Text(text = "Valor bruto: ${moneyFormat.format(viagemAtual?.valorBruto ?: 0.0)}")
            Text(text = "Status: ${viagemAtual?.status.orEmpty()}")
            Text(text = "Status pagamento: ${viagemAtual?.statusPagamento.orEmpty()}")
            Button(onClick = { onNavigateNovaDespesa(viagemId) }) {
                Text(text = "+ Despesa")
            }
            Button(onClick = { onNavigateNovoAbastecimento(viagemId) }) {
                Text(text = "+ Abastecimento")
            }
            Button(onClick = { viewModel.finalizarViagem(viagemId) }) {
                Text(text = "Finalizar Viagem")
            }
            if (isAdmin) {
                Button(onClick = { viewModel.marcarComoPago(viagemId) }) {
                    Text(text = "Marcar como Pago")
                }
            }
        } else {
            Text(text = "Viagem não encontrada")
        }
    }
}
