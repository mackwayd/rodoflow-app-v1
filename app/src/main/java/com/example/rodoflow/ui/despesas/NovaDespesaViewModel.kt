package com.example.rodoflow.ui.despesas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rodoflow.data.repository.ViagemRepository
import kotlinx.coroutines.launch

class NovaDespesaViewModel(
    private val repository: ViagemRepository = ViagemRepository(),
) : ViewModel() {

    fun createDespesa(
        viagemId: String,
        descricao: String,
        valor: Double,
        onSuccess: () -> Unit,
    ) {
        viewModelScope.launch {
            repository.createDespesa(
                viagemId = viagemId,
                descricao = descricao,
                valor = valor,
            )
            onSuccess()
        }
    }
}
