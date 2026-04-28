package com.example.rodoflow.ui.viagens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rodoflow.data.repository.ViagemRepository
import kotlinx.coroutines.launch

class NovaViagemViewModel(
    private val repository: ViagemRepository = ViagemRepository(),
) : ViewModel() {

    fun createViagem(
        origem: String,
        destino: String,
        valorBruto: Double,
        onSuccess: () -> Unit,
    ) {
        viewModelScope.launch {
            repository.createViagem(
                motoristaId = "user-1",
                caminhaoId = "cam-1",
                origem = origem,
                destino = destino,
                valorBruto = valorBruto,
            )
            onSuccess()
        }
    }
}
