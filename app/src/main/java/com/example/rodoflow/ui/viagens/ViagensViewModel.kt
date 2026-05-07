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

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _viagens = MutableStateFlow<List<Viagem>>(emptyList())
    val viagens: StateFlow<List<Viagem>> = _viagens.asStateFlow()

    fun loadViagens() {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                _viagens.value = repository.getViagens(motoristaId = "motorista-1")
            } catch (e: Exception) {
                _error.value = e.message ?: "Não foi possível carregar as viagens."
            } finally {
                _loading.value = false
            }
        }
    }
}
