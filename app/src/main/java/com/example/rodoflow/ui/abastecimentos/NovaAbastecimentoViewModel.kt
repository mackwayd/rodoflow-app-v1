package com.example.rodoflow.ui.abastecimentos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rodoflow.data.repository.ViagemRepository
import kotlinx.coroutines.launch

class NovaAbastecimentoViewModel(
    private val repository: ViagemRepository = ViagemRepository(),
) : ViewModel() {

    fun createAbastecimento(
        viagemId: String,
        litros: Double,
        valorTotal: Double,
        onSuccess: () -> Unit,
    ) {
        viewModelScope.launch {
            repository.createAbastecimento(
                viagemId = viagemId,
                litros = litros,
                valorTotal = valorTotal,
            )
            onSuccess()
        }
    }
}
