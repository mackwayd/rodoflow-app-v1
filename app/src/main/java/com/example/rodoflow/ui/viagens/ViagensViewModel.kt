package com.example.rodoflow.ui.viagens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rodoflow.data.model.Viagem
import com.example.rodoflow.data.repository.ViagemRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ViagensViewModel(
    private val repository: ViagemRepository = ViagemRepository(),
) : ViewModel() {

    private val _viagens = MutableStateFlow<List<Viagem>>(emptyList())
    val viagens: StateFlow<List<Viagem>> = _viagens.asStateFlow()

    fun loadViagens() {
        viewModelScope.launch {
            _viagens.value = repository.getViagens(motoristaId = "user-1")
        }
    }
}
