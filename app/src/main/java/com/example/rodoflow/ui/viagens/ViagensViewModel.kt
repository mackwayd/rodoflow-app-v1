package com.example.rodoflow.ui.viagens

import android.util.Log
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

    private val _loadFailed = MutableStateFlow(false)
    val loadFailed: StateFlow<Boolean> = _loadFailed.asStateFlow()

    private val _viagens = MutableStateFlow<List<Viagem>>(emptyList())
    val viagens: StateFlow<List<Viagem>> = _viagens.asStateFlow()

    fun loadViagens() {
        viewModelScope.launch {
            _loading.value = true
            _loadFailed.value = false
            try {
                _viagens.value = repository.getViagens(motoristaId = "motorista-1")
            } catch (e: Exception) {
                Log.e("VIAGENS_LOAD", "loadViagens falhou: $e", e)
                _loadFailed.value = true
            } finally {
                _loading.value = false
            }
        }
    }
}
