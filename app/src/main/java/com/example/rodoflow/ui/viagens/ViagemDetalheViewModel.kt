package com.example.rodoflow.ui.viagens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rodoflow.data.model.Viagem
import com.example.rodoflow.data.repository.ViagemRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ViagemDetalheViewModel(
    private val repository: ViagemRepository = ViagemRepository(),
) : ViewModel() {

    private val _viagem = MutableStateFlow<Viagem?>(null)
    val viagem: StateFlow<Viagem?> = _viagem.asStateFlow()
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    private val _notFound = MutableStateFlow(false)
    val notFound: StateFlow<Boolean> = _notFound.asStateFlow()

    fun loadViagem(viagemId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _notFound.value = false
            _viagem.value = repository.getViagemById(viagemId)
            if (_viagem.value == null) {
                _notFound.value = true
            }
            _isLoading.value = false
        }
    }
}
